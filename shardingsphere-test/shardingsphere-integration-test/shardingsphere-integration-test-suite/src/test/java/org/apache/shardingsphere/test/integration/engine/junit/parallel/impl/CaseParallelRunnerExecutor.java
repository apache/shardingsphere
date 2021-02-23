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

package org.apache.shardingsphere.test.integration.engine.junit.parallel.impl;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.shardingsphere.infra.executor.kernel.thread.ExecutorServiceManager;
import org.apache.shardingsphere.test.integration.engine.junit.parallel.ParallelRunnerExecutor;
import org.apache.shardingsphere.test.integration.engine.param.model.ParameterizedArray;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * Parallel runner executor with case.
 */
@RequiredArgsConstructor
public final class CaseParallelRunnerExecutor implements ParallelRunnerExecutor {
    
    private final ExecutorServiceManager executorServiceManager = new ExecutorServiceManager(Runtime.getRuntime().availableProcessors() * 2 - 1);
    
    private final List<Future<?>> caseTaskResults = new LinkedList<>();
    
    @Override
    public void execute(final ParameterizedArray parameterizedArray, final Runnable childStatement) {
        caseTaskResults.add(executorServiceManager.getExecutorService().submit(() -> childStatement.run()));
    }
    
    @SneakyThrows
    @Override
    public void finished() {
        caseTaskResults.forEach(future -> {
            try {
                future.get();
            } catch (InterruptedException ignore) {
            } catch (ExecutionException ignore) {
            }
        });
        executorServiceManager.getExecutorService().shutdownNow();
    }
}
