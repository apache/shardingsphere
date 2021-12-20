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

package org.apache.shardingsphere.data.pipeline.scenario.rulealtered;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.data.pipeline.api.config.server.ServerConfiguration;
import org.apache.shardingsphere.data.pipeline.core.execute.ExecuteEngine;
import org.apache.shardingsphere.data.pipeline.spi.check.consistency.DataConsistencyCheckAlgorithm;
import org.apache.shardingsphere.data.pipeline.spi.rulealtered.RuleAlteredJobCompletionDetectAlgorithm;
import org.apache.shardingsphere.infra.config.algorithm.ShardingSphereAlgorithmFactory;
import org.apache.shardingsphere.spi.ShardingSphereServiceLoader;

/**
 * Rule altered context.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
// TODO extract Pipeline Context
public final class RuleAlteredContext {
    
    static {
        ShardingSphereServiceLoader.register(RuleAlteredJobCompletionDetectAlgorithm.class);
        ShardingSphereServiceLoader.register(DataConsistencyCheckAlgorithm.class);
    }
    
    private static final RuleAlteredContext INSTANCE = new RuleAlteredContext();
    
    private volatile ServerConfiguration serverConfig;
    
    private volatile RuleAlteredJobCompletionDetectAlgorithm clusterAutoSwitchAlgorithm;
    
    private volatile DataConsistencyCheckAlgorithm dataConsistencyCheckAlgorithm;
    
    private ExecuteEngine inventoryDumperExecuteEngine;
    
    private ExecuteEngine incrementalDumperExecuteEngine;
    
    private ExecuteEngine importerExecuteEngine;
    
    /**
     * Get instance of context.
     *
     * @return instance of context
     */
    public static RuleAlteredContext getInstance() {
        return INSTANCE;
    }
    
    /**
     * Initialize context.
     *
     * @param serverConfig server configuration
     */
    public void init(final ServerConfiguration serverConfig) {
        if (null != this.serverConfig) {
            return;
        }
        this.serverConfig = serverConfig;
        if (null != serverConfig.getClusterAutoSwitchAlgorithm()) {
            clusterAutoSwitchAlgorithm = ShardingSphereAlgorithmFactory.createAlgorithm(serverConfig.getClusterAutoSwitchAlgorithm(), RuleAlteredJobCompletionDetectAlgorithm.class);
        }
        if (null != serverConfig.getDataConsistencyCheckAlgorithm()) {
            dataConsistencyCheckAlgorithm = ShardingSphereAlgorithmFactory.createAlgorithm(serverConfig.getDataConsistencyCheckAlgorithm(), DataConsistencyCheckAlgorithm.class);
        }
        inventoryDumperExecuteEngine = ExecuteEngine.newFixedThreadInstance(serverConfig.getWorkerThread());
        incrementalDumperExecuteEngine = ExecuteEngine.newCachedThreadInstance();
        importerExecuteEngine = ExecuteEngine.newFixedThreadInstance(serverConfig.getWorkerThread());
    }
}
