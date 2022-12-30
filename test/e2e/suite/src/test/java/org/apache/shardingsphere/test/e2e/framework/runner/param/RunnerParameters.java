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

package org.apache.shardingsphere.test.e2e.framework.runner.param;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.shardingsphere.test.e2e.framework.param.model.E2ETestParameter;
import org.junit.runners.parameterized.BlockJUnit4ClassRunnerWithParameters;
import org.mockito.internal.configuration.plugins.Plugins;

/**
 * Runner parameters.
 */
@RequiredArgsConstructor
public final class RunnerParameters {
    
    private final Runnable childStatement;
    
    /**
     * Get test parameter.
     *
     * @return test parameter
     */
    @SneakyThrows(ReflectiveOperationException.class)
    public E2ETestParameter getTestParameter() {
        Object[] params = (Object[]) Plugins.getMemberAccessor().get(BlockJUnit4ClassRunnerWithParameters.class.getDeclaredField("parameters"), getRunner());
        return (E2ETestParameter) params[0];
    }
    
    @SneakyThrows(ReflectiveOperationException.class)
    private Object getRunner() {
        return Plugins.getMemberAccessor().get(childStatement.getClass().getDeclaredField("val$each"), childStatement);
    }
}
