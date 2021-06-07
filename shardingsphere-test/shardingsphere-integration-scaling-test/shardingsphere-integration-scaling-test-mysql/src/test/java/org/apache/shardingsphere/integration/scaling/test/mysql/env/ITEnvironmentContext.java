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

package org.apache.shardingsphere.integration.scaling.test.mysql.env;

import com.google.gson.Gson;
import lombok.Getter;
import org.apache.shardingsphere.integration.scaling.test.mysql.env.config.SourceConfiguration;
import org.apache.shardingsphere.integration.scaling.test.mysql.env.config.TargetConfiguration;
import org.apache.shardingsphere.scaling.core.config.JobConfiguration;
import org.apache.shardingsphere.scaling.core.config.RuleConfiguration;
import org.apache.shardingsphere.sharding.yaml.config.rule.YamlTableRuleConfiguration;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

/**
 * Integration test environment context.
 */
@Getter
public final class ITEnvironmentContext {
    
    public static final ITEnvironmentContext INSTANCE = new ITEnvironmentContext();
    
    private final DataSource sourceDataSource;
    
    private final DataSource targetDataSource;
    
    private final String scalingConfiguration;
    
    public ITEnvironmentContext() {
        Map<String, YamlTableRuleConfiguration> sourceTableRules = createSourceTableRules();
        scalingConfiguration = createScalingConfiguration(sourceTableRules);
        sourceDataSource = SourceConfiguration.createHostDataSource(sourceTableRules);
        targetDataSource = TargetConfiguration.createHostDataSource();
    }
    
    private Map<String, YamlTableRuleConfiguration> createSourceTableRules() {
        Map<String, YamlTableRuleConfiguration> result = new HashMap<>();
        YamlTableRuleConfiguration t1TableRule = new YamlTableRuleConfiguration();
        t1TableRule.setLogicTable("t1");
        t1TableRule.setActualDataNodes("ds_src.t1");
        result.put("t1", t1TableRule);
        return result;
    }
    
    private static String createScalingConfiguration(final Map<String, YamlTableRuleConfiguration> tableRules) {
        JobConfiguration jobConfiguration = new JobConfiguration();
        RuleConfiguration ruleConfiguration = new RuleConfiguration();
        ruleConfiguration.setSource(SourceConfiguration.getDockerConfiguration(tableRules).wrap());
        ruleConfiguration.setTarget(TargetConfiguration.getDockerConfiguration().wrap());
        jobConfiguration.setRuleConfig(ruleConfiguration);
        return new Gson().toJson(jobConfiguration);
    }
}
