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

package org.apache.shardingsphere.scaling.core.config;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.scaling.core.execute.engine.TaskExecuteEngine;

/**
 * ShardingSphere-Scaling context.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public final class ScalingContext {
    
    private static final ScalingContext INSTANCE = new ScalingContext();
    
    private ServerConfiguration serverConfig;
    
    private TaskExecuteEngine inventoryDumperExecuteEngine;
    
    private TaskExecuteEngine incrementalDumperExecuteEngine;
    
    private TaskExecuteEngine importerExecuteEngine;
    
    /**
     * Get instance of ShardingSphere-Scaling's context.
     *
     * @return instance of ShardingSphere-Scaling's context.
     */
    public static ScalingContext getInstance() {
        return INSTANCE;
    }
    
    /**
     * Initialize ShardingSphere-Scaling context.
     *
     * @param serverConfig server configuration
     */
    public void init(final ServerConfiguration serverConfig) {
        this.serverConfig = serverConfig;
        inventoryDumperExecuteEngine = TaskExecuteEngine.newFixedThreadInstance(serverConfig.getWorkerThread());
        incrementalDumperExecuteEngine = TaskExecuteEngine.newCachedThreadInstance();
        importerExecuteEngine = TaskExecuteEngine.newFixedThreadInstance(serverConfig.getWorkerThread());
    }
}
