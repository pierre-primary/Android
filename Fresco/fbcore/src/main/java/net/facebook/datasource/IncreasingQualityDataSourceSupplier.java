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
import javax.annotation.concurrent.ThreadSafe;

import java.util.ArrayList;
import java.util.List;

import com.facebook.common.executors.CallerThreadExecutor;
import com.facebook.common.internal.Lists;
import com.facebook.common.internal.Objects;
import com.facebook.common.internal.Preconditions;
import com.facebook.common.internal.Supplier;

/**
 * {@link DataSource} supplier that provides a data source which forwards results of the underlying
 * data sources with the increasing quality.
 * <p/>
 * <p>Data sources are obtained in order. The first data source in array is considered to be of the
 * highest quality. The first data source to provide an result gets forwarded until one of the
 * higher quality data sources provides its final image at which point that data source gets
 * forwarded (and so on). That being said, only the first data source to provide an result is
 * streamed.
 * <p/>
 * <p>Outcome (success/failure) of the data source provided by this supplier is determined by the
 * outcome of the highest quality data source (the first data source in the array).
 */
@ThreadSafe
public class IncreasingQualityDataSourceSupplier<T> implements Supplier<DataSource<T>> {

    private final List<Supplier<DataSource<T>>> mDataSourceSuppliers;

    private IncreasingQualityDataSourceSupplier(List<Supplier<DataSource<T>>> dataSourceSuppliers) {
        Preconditions.checkArgument(!dataSourceSuppliers.isEmpty(), "List of suppliers is empty!");
        mDataSourceSuppliers = dataSourceSuppliers;
    }

    /**
     * Creates a new data source supplier with increasing-quality strategy.
     * <p>Note: for performance reasons the list doesn't get cloned, so the caller of this method
     * should not modify the list once passed in here.
     *
     * @param dataSourceSuppliers list of underlying suppliers
     */
    public static <T> IncreasingQualityDataSourceSupplier<T> create(
            List<Supplier<DataSource<T>>> dataSourceSuppliers) {
        return new IncreasingQualityDataSourceSupplier<T>(dataSourceSuppliers);
    }

    @Override
    public DataSource<T> get() {
        return new IncreasingQualityDataSource();
    }

    @Override
    public int hashCode() {
        return mDataSourceSuppliers.hashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof IncreasingQualityDataSourceSupplier)) {
            return false;
        }
        IncreasingQualityDataSourceSupplier that = (IncreasingQualityDataSourceSupplier) other;
        return Objects.equal(this.mDataSourceSuppliers, that.mDataSourceSuppliers);
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("list", mDataSourceSuppliers)
                .toString();
    }

    @ThreadSafe
    private class IncreasingQualityDataSource extends AbstractDataSource<T> {

        @GuardedBy("IncreasingQualityDataSource.this")
        private
        @Nullable
        ArrayList<DataSource<T>> mDataSources;
        @GuardedBy("IncreasingQualityDataSource.this")
        private int mIndexOfDataSourceWithResult;

        public IncreasingQualityDataSource() {
            final int n = mDataSourceSuppliers.size();
            mIndexOfDataSourceWithResult = n;
            mDataSources = Lists.newArrayListWithCapacity(n);
            for (int i = 0; i < n; i++) {
                DataSource<T> dataSource = mDataSourceSuppliers.get(i).get();
                mDataSources.add(dataSource);
                dataSource.subscribe(new InternalDataSubscriber(i), CallerThreadExecutor.getInstance());
                // there's no point in creating data sources of lower quality
                // if the data source of a higher quality has some result already
                if (dataSource.hasResult()) {
                    break;
                }
            }
        }

        @Nullable
        private synchronized DataSource<T> getDataSource(int i) {
            return (mDataSources != null && i < mDataSources.size()) ? mDataSources.get(i) : null;
        }

        @Nullable
        private synchronized DataSource<T> getAndClearDataSource(int i) {
            return (mDataSources != null && i < mDataSources.size()) ? mDataSources.set(i, null) : null;
        }

        @Nullable
        private synchronized DataSource<T> getDataSourceWithResult() {
            return getDataSource(mIndexOfDataSourceWithResult);
        }

        @Override
        @Nullable
        public synchronized T getResult() {
            DataSource<T> dataSourceWithResult = getDataSourceWithResult();
            return (dataSourceWithResult != null) ? dataSourceWithResult.getResult() : null;
        }

        @Override
        public synchronized boolean hasResult() {
            DataSource<T> dataSourceWithResult = getDataSourceWithResult();
            return (dataSourceWithResult != null) && dataSourceWithResult.hasResult();
        }

        @Override
        public boolean close() {
            ArrayList<DataSource<T>> dataSources;
            synchronized (IncreasingQualityDataSource.this) {
                // it's fine to call {@code super.close()} within a synchronized block because we don't
                // implement {@link #closeResult()}, but perform result closing ourselves.
                if (!super.close()) {
                    return false;
                }
                dataSources = mDataSources;
                mDataSources = null;
            }
            if (dataSources != null) {
                for (int i = 0; i < dataSources.size(); i++) {
                    closeSafely(dataSources.get(i));
                }
            }
            return true;
        }

        private void onDataSourceNewResult(int index, DataSource<T> dataSource) {
            maybeSetIndexOfDataSourceWithResult(index, dataSource, dataSource.isFinished());
            // If the data source with the new result is our {@code mIndexOfDataSourceWithResult},
            // we have to notify our subscribers about the new result.
            if (dataSource == getDataSourceWithResult()) {
                setResult(null, (index == 0) && dataSource.isFinished());
            }
        }

        private void onDataSourceFailed(int index, DataSource<T> dataSource) {
            closeSafely(tryGetAndClearDataSource(index, dataSource));
            if (index == 0) {
                setFailure(dataSource.getFailureCause());
            }
        }

        private void maybeSetIndexOfDataSourceWithResult(
                int index,
                DataSource<T> dataSource,
                boolean isFinished) {
            int oldIndexOfDataSourceWithResult;
            int newIndexOfDataSourceWithResult;
            synchronized (IncreasingQualityDataSource.this) {
                oldIndexOfDataSourceWithResult = mIndexOfDataSourceWithResult;
                newIndexOfDataSourceWithResult = mIndexOfDataSourceWithResult;
                if (dataSource != getDataSource(index) || index == mIndexOfDataSourceWithResult) {
                    return;
                }
                // If we didn't have any result so far, we got one now, so we'll set
                // {@code mIndexOfDataSourceWithResult} to point to the data source with result.
                // If we did have a result which came from another data source,
                // we'll only set {@code mIndexOfDataSourceWithResult} to point to the current data source
                // if it has finished (i.e. the new result is final), and is of higher quality.
                if (getDataSourceWithResult() == null ||
                        (isFinished && index < mIndexOfDataSourceWithResult)) {
                    newIndexOfDataSourceWithResult = index;
                    mIndexOfDataSourceWithResult = index;
                }
            }
            // close data sources of lower quality than the one with the result
            for (int i = oldIndexOfDataSourceWithResult; i > newIndexOfDataSourceWithResult; i--) {
                closeSafely(getAndClearDataSource(i));
            }
        }

        @Nullable
        private synchronized DataSource<T> tryGetAndClearDataSource(int i, DataSource<T> dataSource) {
            if (dataSource == getDataSourceWithResult()) {
                return null;
            }
            if (dataSource == getDataSource(i)) {
                return getAndClearDataSource(i);
            }
            return dataSource;
        }

        private void closeSafely(DataSource<T> dataSource) {
            if (dataSource != null) {
                dataSource.close();
            }
        }

        private class InternalDataSubscriber implements DataSubscriber<T> {
            private int mIndex;

            public InternalDataSubscriber(int index) {
                mIndex = index;
            }

            @Override
            public void onNewResult(DataSource<T> dataSource) {
                if (dataSource.hasResult()) {
                    IncreasingQualityDataSource.this.onDataSourceNewResult(mIndex, dataSource);
                } else if (dataSource.isFinished()) {
                    IncreasingQualityDataSource.this.onDataSourceFailed(mIndex, dataSource);
                }
            }

            @Override
            public void onFailure(DataSource<T> dataSource) {
                IncreasingQualityDataSource.this.onDataSourceFailed(mIndex, dataSource);
            }

            @Override
            public void onCancellation(DataSource<T> dataSource) {
            }
        }
    }
}
