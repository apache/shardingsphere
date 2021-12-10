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

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.migration.common.spi.RuleJobConfigurationPreparer;
import org.apache.shardingsphere.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.spi.typed.TypedSPIRegistry;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
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
        ShardingSphereServiceLoader.register(RuleJobConfigurationPreparer.class);
    }
    
    private RuleConfiguration ruleConfig;
    
    private HandleConfiguration handleConfig = new HandleConfiguration();
    
    /**
     * Fill in properties.
     */
    public void fillInProperties() {
        HandleConfiguration handleConfig = getHandleConfig();
        if (null == handleConfig.getJobId()) {
            handleConfig.setJobId(System.nanoTime() - ThreadLocalRandom.current().nextLong(100_0000));
        }
        if (Strings.isNullOrEmpty(handleConfig.getDatabaseType())) {
            handleConfig.setDatabaseType(getRuleConfig().getSource().unwrap().getDatabaseType().getName());
        }
        if (null == handleConfig.getJobShardingItem()) {
            handleConfig.setJobShardingItem(0);
        }
        RuleConfiguration ruleConfig = getRuleConfig();
        if (null == handleConfig.getJobShardingDataNodes()) {
            List<HandleConfiguration> newHandleConfigs = new LinkedList<>();
            for (String each : ruleConfig.getChangedYamlRuleConfigClassNames()) {
                Optional<RuleJobConfigurationPreparer> preparerOptional = TypedSPIRegistry.findRegisteredService(RuleJobConfigurationPreparer.class, each, null);
                Preconditions.checkArgument(preparerOptional.isPresent(), "Could not find registered service for type '%s'", each);
                HandleConfiguration newHandleConfig = preparerOptional.get().convertToHandleConfig(ruleConfig);
                newHandleConfigs.add(newHandleConfig);
            }
            // TODO handle several rules changed or dataSources changed
            for (HandleConfiguration each : newHandleConfigs) {
                handleConfig.setJobShardingDataNodes(each.getJobShardingDataNodes());
                handleConfig.setLogicTables(each.getLogicTables());
                handleConfig.setTablesFirstDataNodes(each.getTablesFirstDataNodes());
            }
        }
    }
    
    /**
     * Split job configuration to task configurations.
     *
     * @return task configurations
     */
    public List<TaskConfiguration> convertToTaskConfigs() {
        RuleConfiguration ruleConfig = getRuleConfig();
        // TODO handle several rules changed or dataSources changed
        for (String each : ruleConfig.getChangedYamlRuleConfigClassNames()) {
            Optional<RuleJobConfigurationPreparer> preparerOptional = TypedSPIRegistry.findRegisteredService(RuleJobConfigurationPreparer.class, each, null);
            Preconditions.checkArgument(preparerOptional.isPresent(), "Could not find registered service for type '%s'", each);
            return preparerOptional.get().convertToTaskConfigs(this);
        }
        log.warn("return empty task configurations");
        return Collections.emptyList();
    }
}
