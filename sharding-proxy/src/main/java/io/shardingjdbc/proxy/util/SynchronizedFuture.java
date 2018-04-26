/*
 * Copyright 1999-2015 dangdang.com.
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

package io.shardingjdbc.proxy.util;

import com.google.common.collect.Lists;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * sync get multiple netty return.
 *
 * @author wangkai
 */
public class SynchronizedFuture<T> implements Future<List<T>> {
    
    private CountDownLatch latch;
    
    private List<T> responses;
    
    private long beginTime = System.currentTimeMillis();
    
    public SynchronizedFuture(int resultSize){
        latch = new CountDownLatch(resultSize);
        responses = Lists.newArrayListWithCapacity(resultSize);
    }
    
    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        return false;
    }
    @Override
    public boolean isCancelled() {
        return false;
    }
    @Override
    public boolean isDone() {
        if (null != responses && responses.size() > 0) {
            return true;
        }
        return false;
    }
    
    @Override
    public List<T> get() throws InterruptedException {
        latch.await();
        return this.responses;
    }
    
    @Override
    public List<T> get(long timeout, TimeUnit unit) {
        try {
            if (latch.await(timeout, unit)) {
                return this.responses;
            }
        } catch (InterruptedException e) {
            //TODO
        }
        return this.responses;
    }
    
    public void setResponse(T response) {
        this.responses.add(response);
        latch.countDown();
    }
    
    public long getBeginTime() {
        return beginTime;
    }
}