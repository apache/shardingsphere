/*
 * Copyright 2016-2018 shardingsphere.io.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingsphere.shardingproxy.backend.netty.future;

import io.shardingsphere.core.merger.QueryResult;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * Synchronized future for get multiple netty returns.
 *
 * @author wangkai
 * @author linjiaqi
 */
@Slf4j
public final class SynchronizedFuture implements Future<List<QueryResult>> {
    
    private final CountDownLatch latch;
    
    private final List<QueryResult> responses;
    
    private boolean isDone;
    
    public SynchronizedFuture(final int resultSize) {
        latch = new CountDownLatch(resultSize);
        responses = new ArrayList<>(resultSize);
    }
    
    @Override
    public boolean cancel(final boolean mayInterruptIfRunning) {
        return false;
    }
    
    @Override
    public boolean isCancelled() {
        return false;
    }
    
    @Override
    public boolean isDone() {
        return isDone;
    }
    
    @Override
    public List<QueryResult> get() throws InterruptedException {
        latch.await();
        return responses;
    }
    
    /**
     * Get responses for waiting time.
     * 
     * @param timeout wait timeout
     * @param unit time unit
     * @return responses
     */
    @Override
    public List<QueryResult> get(final long timeout, final TimeUnit unit) {
        try {
            latch.await(timeout, unit);
            isDone = true;
        } catch (final InterruptedException ex) {
            log.error(ex.getMessage(), ex);
        }
        return responses;
    }
    
    /**
     * Set response and count down.
     * 
     * @param response SQL command result
     */
    public void setResponse(final QueryResult response) {
        responses.add(response);
        latch.countDown();
    }
}
