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

package io.shardingsphere.proxy.util;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import com.google.common.collect.Lists;

import io.shardingsphere.core.merger.QueryResult;
import lombok.extern.slf4j.Slf4j;

/**
 * sync get multiple netty return.
 *
 * @author wangkai
 * @author linjiaqi
 */
@Slf4j
public class SynchronizedFuture<T> implements Future<List<QueryResult>> {
    private boolean merged;
    
    private CountDownLatch latch;
    
    private List<QueryResult> responses;
    
    public SynchronizedFuture(final int resultSize) {
        latch = new CountDownLatch(resultSize);
        responses = Lists.newArrayListWithCapacity(resultSize);
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
        return merged ? true : false;
    }
    
    @Override
    public List<QueryResult> get() throws InterruptedException {
        latch.await();
        return responses;
    }
    
    /**
     * wait for responses.
     * @param timeout wait timeout.
     * @param unit time unit
     * @return responses.
     */
    @Override
    public List<QueryResult> get(final long timeout, final TimeUnit unit) {
        try {
            latch.await(timeout, unit);
        } catch (InterruptedException e) {
            log.error(e.getMessage(), e);
        }
        return responses;
    }
    
    /**
     * set response and count down.
     * @param response sql command result.
     */
    public void setResponse(final QueryResult response) {
        responses.add(response);
        latch.countDown();
    }
}
