/*
 * Copyright (c) 2015-present, Facebook, Inc.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree. An additional grant
 * of patent rights can be found in the PATENTS file in the same directory.
 */

package com.facebook.common.executors;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;

/**
 * An {@link java.util.concurrent.ExecutorService} that is backed by a handler.
 */
public interface HandlerExecutorService extends ScheduledExecutorService {

    /**
     * Quit the handler
     */
    void quit();

    /**
     * Check if we are currently in the handler thread of this HandlerExecutorService.
     */
    boolean isHandlerThread();
}
