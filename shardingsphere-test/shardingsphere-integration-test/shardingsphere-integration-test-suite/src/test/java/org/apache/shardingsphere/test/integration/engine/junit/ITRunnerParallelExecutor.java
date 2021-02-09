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

package org.apache.shardingsphere.test.integration.engine.junit;

import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.infra.executor.kernel.thread.ExecutorServiceManager;
import org.apache.shardingsphere.test.integration.engine.param.domain.ParameterizedWrapper;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * IT runner parallel executor.
 */
@Slf4j
@Getter
public class ITRunnerParallelExecutor implements ITRunnerExecutor {
    
    private final ExecutorServiceManager executorServiceManager;
    
    @SneakyThrows
    public ITRunnerParallelExecutor() {
        executorServiceManager = new ExecutorServiceManager(Runtime.getRuntime().availableProcessors() * 2 - 1);
    }
    
    @Override
    public void execute(final ParameterizedWrapper parameterizedWrapper, final Runnable childStatement) {
        executorServiceManager.getExecutorService().submit(childStatement);
    }
    
    @Override
    public void finished() {
        if (null != executorServiceManager) {
            try {
                ExecutorService executorService = executorServiceManager.getExecutorService();
                executorService.shutdown();
                executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
            } catch (InterruptedException e) {
                e.printStackTrace(System.err);
            }
        }
    }
}
