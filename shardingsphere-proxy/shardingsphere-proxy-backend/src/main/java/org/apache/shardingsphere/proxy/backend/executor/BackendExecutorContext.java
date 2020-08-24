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

package org.apache.shardingsphere.proxy.backend.executor;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.config.properties.ConfigurationPropertyKey;
import org.apache.shardingsphere.infra.executor.kernel.ExecutorKernel;
import org.apache.shardingsphere.proxy.backend.schema.ProxySchemaContexts;

/**
 * Backend executor context.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public final class BackendExecutorContext {
    
    private static final BackendExecutorContext INSTANCE = new BackendExecutorContext();
    
    private final ExecutorKernel executorKernel = 
            new ExecutorKernel(ProxySchemaContexts.getInstance().getSchemaContexts().getProps().<Integer>getValue(ConfigurationPropertyKey.EXECUTOR_SIZE));
    
    /**
     * Get executor context instance.
     * 
     * @return instance of executor context
     */
    public static BackendExecutorContext getInstance() {
        return INSTANCE;
    }
}
