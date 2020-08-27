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

import com.google.common.base.Strings;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.scaling.core.execute.engine.ShardingScalingExecuteEngine;
import org.apache.shardingsphere.scaling.core.spi.ElasticJobEntryLoader;

/**
 * Scaling context.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public final class ScalingContext {
    
    private static final ScalingContext INSTANCE = new ScalingContext();
    
    private ServerConfiguration serverConfiguration;
    
    private ShardingScalingExecuteEngine taskExecuteEngine;
    
    private ShardingScalingExecuteEngine importerExecuteEngine;
    
    /**
     * Get instance of Sharding-Scaling's context.
     *
     * @return instance of Sharding-Scaling's context.
     */
    public static ScalingContext getInstance() {
        return INSTANCE;
    }
    
    /**
     * Initialize Scaling context.
     *
     * @param serverConfiguration serverConfiguration
     */
    public void init(final ServerConfiguration serverConfiguration) {
        this.serverConfiguration = serverConfiguration;
        taskExecuteEngine = new ShardingScalingExecuteEngine(serverConfiguration.getWorkerThread());
        importerExecuteEngine = new ShardingScalingExecuteEngine(serverConfiguration.getWorkerThread());
        initElasticJobEntry(serverConfiguration);
    }
    
    private void initElasticJobEntry(final ServerConfiguration serverConfiguration) {
        if (!Strings.isNullOrEmpty(serverConfiguration.getName()) && null != serverConfiguration.getRegistryCenter()) {
            ElasticJobEntryLoader.init(serverConfiguration.getName(), serverConfiguration.getRegistryCenter());
        }
    }
}
