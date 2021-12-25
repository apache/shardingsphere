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

package org.apache.shardingsphere.data.pipeline.api.config.rulealtered;

import com.google.common.base.Strings;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.data.pipeline.spi.rulealtered.RuleAlteredJobConfigurationPreparer;
import org.apache.shardingsphere.infra.config.datasource.jdbc.config.JDBCDataSourceConfiguration;
import org.apache.shardingsphere.infra.config.datasource.jdbc.config.JDBCDataSourceConfigurationFactory;
import org.apache.shardingsphere.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.spi.required.RequiredSPIRegistry;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Scaling job configuration.
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Slf4j
// TODO share for totally new scenario
public final class JobConfiguration {
    
    static {
        ShardingSphereServiceLoader.register(RuleAlteredJobConfigurationPreparer.class);
    }
    
    private WorkflowConfiguration workflowConfig;
    
    private RuleConfiguration ruleConfig;
    
    private HandleConfiguration handleConfig;
    
    public JobConfiguration(final WorkflowConfiguration workflowConfig, final RuleConfiguration ruleConfig) {
        this.workflowConfig = workflowConfig;
        this.ruleConfig = ruleConfig;
    }
    
    /**
     * Build handle configuration.
     */
    public void buildHandleConfig() {
        RuleConfiguration ruleConfig = getRuleConfig();
        HandleConfiguration handleConfig = getHandleConfig();
        if (null == handleConfig || null == handleConfig.getJobShardingDataNodes()) {
            // TODO singleton
            RuleAlteredJobConfigurationPreparer preparer = RequiredSPIRegistry.getRegisteredService(RuleAlteredJobConfigurationPreparer.class);
            handleConfig = preparer.createHandleConfig(ruleConfig);
            this.handleConfig = handleConfig;
        }
        if (null == handleConfig.getJobId()) {
            handleConfig.setJobId(System.nanoTime() - ThreadLocalRandom.current().nextLong(100_0000));
        }
        if (Strings.isNullOrEmpty(handleConfig.getSourceDatabaseType())) {
            JDBCDataSourceConfiguration sourceDataSourceConfig = JDBCDataSourceConfigurationFactory.newInstance(getRuleConfig().getSource().getType(), getRuleConfig().getSource().getParameter());
            handleConfig.setSourceDatabaseType(sourceDataSourceConfig.getDatabaseType().getName());
        }
        if (Strings.isNullOrEmpty(handleConfig.getTargetDatabaseType())) {
            JDBCDataSourceConfiguration targetDataSourceConfig = JDBCDataSourceConfigurationFactory.newInstance(getRuleConfig().getTarget().getType(), getRuleConfig().getTarget().getParameter());
            handleConfig.setTargetDatabaseType(targetDataSourceConfig.getDatabaseType().getName());
        }
        if (null == handleConfig.getJobShardingItem()) {
            handleConfig.setJobShardingItem(0);
        }
    }
    
    /**
     * Split job configuration to task configurations.
     *
     * @return task configurations
     */
    public List<TaskConfiguration> buildTaskConfigs() {
        RuleAlteredJobConfigurationPreparer preparer = RequiredSPIRegistry.getRegisteredService(RuleAlteredJobConfigurationPreparer.class);
        return preparer.createTaskConfigs(ruleConfig, handleConfig);
    }
}
