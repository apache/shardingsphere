/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.shardingsphere.data.pipeline.core.execute;

import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AbstractPipelineLifecycleRunnableTest {
    
    @Test
    void assertRunning() {
        FixturePipelineLifecycleRunnable executor = new FixturePipelineLifecycleRunnable();
        assertFalse(executor.isRunning());
        executor.start();
        assertTrue(executor.isRunning());
        executor.stop();
        assertFalse(executor.isRunning());
    }
    
    @Test
    void assertStartRunOnce() {
        FixturePipelineLifecycleRunnable executor = new FixturePipelineLifecycleRunnable();
        executor.start();
        executor.start();
        assertThat(executor.runBlockingCount.get(), is(1));
    }
    
    @Test
    void assertStopRunOnce() {
        FixturePipelineLifecycleRunnable executor = new FixturePipelineLifecycleRunnable();
        executor.start();
        executor.stop();
        executor.stop();
        assertThat(executor.doStopCount.get(), is(1));
    }
    
    @Test
    void assertNoStopBeforeStarting() {
        FixturePipelineLifecycleRunnable executor = new FixturePipelineLifecycleRunnable();
        executor.stop();
        executor.stop();
        assertThat(executor.doStopCount.get(), is(0));
    }
    
    @Test
    void assertStopStart() {
        FixturePipelineLifecycleRunnable executor = new FixturePipelineLifecycleRunnable();
        executor.stop();
        executor.start();
        assertThat(executor.doStopCount.get(), is(0));
        assertThat(executor.runBlockingCount.get(), is(0));
    }
    
    private static final class FixturePipelineLifecycleRunnable extends AbstractPipelineLifecycleRunnable {
        
        private final AtomicInteger runBlockingCount = new AtomicInteger();
        
        private final AtomicInteger doStopCount = new AtomicInteger();
        
        @Override
        protected void runBlocking() {
            runBlockingCount.addAndGet(1);
        }
        
        @Override
        protected void doStop() {
            doStopCount.addAndGet(1);
        }
    }
}
