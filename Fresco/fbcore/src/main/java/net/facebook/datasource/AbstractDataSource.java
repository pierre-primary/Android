/*
 * Copyright (c) 2015-present, Facebook, Inc.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree. An additional grant
 * of patent rights can be found in the PATENTS file in the same directory.
 */

package com.facebook.datasource;

import javax.annotation.Nullable;
import javax.annotation.concurrent.GuardedBy;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executor;

import android.util.Pair;

import com.facebook.common.internal.Preconditions;

/**
 * An abstract implementation of {@link DataSource} interface.
 * <p/>
 * <p> It is highly recommended that other data sources extend this class as it takes care of the
 * state, as well as of notifying listeners when the state changes.
 * <p/>
 * <p> Subclasses should override {@link #closeResult(T result)} if results need clean up
 *
 * @param <T>
 */
public abstract class AbstractDataSource<T> implements DataSource<T> {
    /**
     * Describes state of data source
     */
    private enum DataSourceStatus {
        // data source has not finished yet
        IN_PROGRESS,

        // data source has finished with success
        SUCCESS,

        // data source has finished with failure
        FAILURE,
    }

    @GuardedBy("this")
    private DataSourceStatus mDataSourceStatus;
    @GuardedBy("this")
    private boolean mIsClosed;
    @GuardedBy("this")
    private
    @Nullable
    T mResult = null;
    @GuardedBy("this")
    private Throwable mFailureThrowable = null;
    private final ConcurrentLinkedQueue<Pair<DataSubscriber<T>, Executor>> mSubscribers;

    protected AbstractDataSource() {
        mIsClosed = false;
        mDataSourceStatus = DataSourceStatus.IN_PROGRESS;
        mSubscribers = new ConcurrentLinkedQueue<Pair<DataSubscriber<T>, Executor>>();
    }

    @Override
    public synchronized boolean isClosed() {
        return mIsClosed;
    }

    @Override
    public synchronized boolean isFinished() {
        return mDataSourceStatus != DataSourceStatus.IN_PROGRESS;
    }

    @Override
    public synchronized boolean hasResult() {
        return mResult != null;
    }

    @Override
    @Nullable
    public synchronized T getResult() {
        return mResult;
    }

    @Override
    public synchronized boolean hasFailed() {
        return mDataSourceStatus == DataSourceStatus.FAILURE;
    }

    @Override
    @Nullable
    public synchronized Throwable getFailureCause() {
        return mFailureThrowable;
    }

    @Override
    public boolean close() {
        T resultToClose;
        synchronized (this) {
            if (mIsClosed) {
                return false;
            }
            mIsClosed = true;
            resultToClose = mResult;
            mResult = null;
        }
        if (resultToClose != null) {
            closeResult(resultToClose);
        }
        if (!isFinished()) {
            notifyDataSubscribers();
        }
        synchronized (this) {
            mSubscribers.clear();
        }
        return true;
    }

    /**
     * Subclasses should override this method to close the result that is not needed anymore.
     * <p/>
     * <p> This method is called in two cases:
     * 1. to clear the result when data source gets closed
     * 2. to clear the previous result when a new result is set
     */
    protected void closeResult(@Nullable T result) {
        // default implementation does nothing
    }

    @Override
    public void subscribe(final DataSubscriber<T> dataSubscriber, final Executor executor) {
        Preconditions.checkNotNull(dataSubscriber);
        Preconditions.checkNotNull(executor);
        boolean shouldNotify;

        synchronized (this) {
            if (mIsClosed) {
                return;
            }

            if (mDataSourceStatus == DataSourceStatus.IN_PROGRESS) {
                mSubscribers.add(Pair.create(dataSubscriber, executor));
            }

            shouldNotify = hasResult() || isFinished() || wasCancelled();
        }

        if (shouldNotify) {
            notifyDataSubscriber(dataSubscriber, executor, hasFailed(), wasCancelled());
        }
    }

    private void notifyDataSubscribers() {
        final boolean isFailure = hasFailed();
        final boolean isCancellation = wasCancelled();
        for (Pair<DataSubscriber<T>, Executor> pair : mSubscribers) {
            notifyDataSubscriber(pair.first, pair.second, isFailure, isCancellation);
        }
    }

    private void notifyDataSubscriber(
            final DataSubscriber<T> dataSubscriber,
            final Executor executor,
            final boolean isFailure,
            final boolean isCancellation) {
        executor.execute(
                new Runnable() {
                    @Override
                    public void run() {
                        if (isFailure) {
                            dataSubscriber.onFailure(AbstractDataSource.this);
                        } else if (isCancellation) {
                            dataSubscriber.onCancellation(AbstractDataSource.this);
                        } else {
                            dataSubscriber.onNewResult(AbstractDataSource.this);
                        }
                    }
                });
    }

    private synchronized boolean wasCancelled() {
        return isClosed() && !isFinished();
    }

    /**
     * Subclasses should invoke this method to set the result to {@code value}.
     * <p/>
     * <p> This method will return {@code true} if the value was successfully set, or
     * {@code false} if the data source has already been set, failed or closed.
     * <p/>
     * <p> If the value was successfully set and {@code isLast} is {@code true}, state of the
     * data source will be set to {@link com.facebook.datasource.AbstractDataSource.DataSourceStatus#SUCCESS}.
     * <p/>
     * <p> {@link #closeResult} will be called for the previous result if the new value was
     * successfully set, OR for the new result otherwise.
     * <p/>
     * <p> This will also notify the subscribers if the value was successfully set.
     * <p/>
     * <p> Do NOT call this method from a synchronized block as it invokes external code of the
     * subscribers.
     *
     * @param value  the value that was the result of the task.
     * @param isLast whether or not the value is last.
     * @return true if the value was successfully set.
     */
    protected boolean setResult(@Nullable T value, boolean isLast) {
        boolean result = setResultInternal(value, isLast);
        if (result) {
            notifyDataSubscribers();
        }
        return result;
    }

    /**
     * Subclasses should invoke this method to set the failure.
     * <p/>
     * <p/>
     * <p> This method will return {@code true} if the failure was successfully set, or
     * {@code false} if the data source has already been set, failed or closed.
     * <p/>
     * <p> If the failure was successfully set, state of the data source will be set to
     * {@link com.facebook.datasource.AbstractDataSource.DataSourceStatus#FAILURE}.
     * <p/>
     * <p> This will also notify the subscribers if the failure was successfully set.
     * <p/>
     * <p> Do NOT call this method from a synchronized block as it invokes external code of the
     * subscribers.
     *
     * @param throwable the failure cause to be set.
     * @return true if the failure was successfully set.
     */
    protected boolean setFailure(Throwable throwable) {
        boolean result = setFailureInternal(throwable);
        if (result) {
            notifyDataSubscribers();
        }
        return result;
    }

    private boolean setResultInternal(@Nullable T value, boolean isLast) {
        T resultToClose = null;
        try {
            synchronized (this) {
                if (mIsClosed || mDataSourceStatus != DataSourceStatus.IN_PROGRESS) {
                    resultToClose = value;
                    return false;
                } else {
                    if (isLast) {
                        mDataSourceStatus = DataSourceStatus.SUCCESS;
                    }
                    if (mResult != value) {
                        resultToClose = mResult;
                        mResult = value;
                    }
                    return true;
                }
            }
        } finally {
            if (resultToClose != null) {
                closeResult(resultToClose);
            }
        }
    }

    private synchronized boolean setFailureInternal(Throwable throwable) {
        if (mIsClosed || mDataSourceStatus != DataSourceStatus.IN_PROGRESS) {
            return false;
        } else {
            mDataSourceStatus = DataSourceStatus.FAILURE;
            mFailureThrowable = throwable;
            return true;
        }
    }
}
