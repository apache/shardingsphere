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

package org.apache.shardingsphere.migration.common.spi;

import org.apache.shardingsphere.infra.yaml.config.pojo.YamlRuleConfiguration;
import org.apache.shardingsphere.scaling.core.config.HandleConfiguration;
import org.apache.shardingsphere.scaling.core.config.JobConfiguration;
import org.apache.shardingsphere.scaling.core.config.RuleConfiguration;
import org.apache.shardingsphere.scaling.core.config.TaskConfiguration;
import org.apache.shardingsphere.spi.typed.TypedSPI;

import java.util.List;

/**
 * Rule job configuration preparer, SPI interface.
 */
public interface RuleJobConfigurationPreparer extends TypedSPI {
    
    /**
     * Get type.
     *
     * @return {@link YamlRuleConfiguration} implementation class full name
     */
    String getType();
    
    /**
     * Convert to handle configuration, used to build job configuration.
     *
     * @param ruleConfig rule configuration
     * @return handle configuration. It won't be used directly, but merge necessary configuration (e.g. shardingTables, logicTables) into final {@link HandleConfiguration}
     */
    HandleConfiguration convertToHandleConfig(RuleConfiguration ruleConfig);
    
    /**
     * Convert to task configurations, used by underlying scheduler.
     *
     * @param jobConfig job configuration
     * @return task configurations
     */
    List<TaskConfiguration> convertToTaskConfigs(JobConfiguration jobConfig);
}
