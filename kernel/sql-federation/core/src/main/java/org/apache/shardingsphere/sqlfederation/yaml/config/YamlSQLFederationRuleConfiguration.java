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

package org.apache.shardingsphere.sqlfederation.yaml.config;

import lombok.Getter;
import lombok.Setter;
import org.apache.shardingsphere.infra.yaml.config.pojo.rule.YamlGlobalRuleConfiguration;
import org.apache.shardingsphere.mode.node.rule.tuple.annotation.RuleNodeTupleEntity;
import org.apache.shardingsphere.sqlfederation.config.SQLFederationRuleConfiguration;

/**
 * SQL federation rule configuration for YAML.
 */
@RuleNodeTupleEntity(value = "sql_federation", leaf = true)
@Getter
@Setter
public final class YamlSQLFederationRuleConfiguration implements YamlGlobalRuleConfiguration {
    
    private boolean sqlFederationEnabled;
    
    private boolean allQueryUseSQLFederation;
    
    private YamlSQLFederationExecutionPlanCacheRuleConfiguration executionPlanCache;
    
    @Override
    public Class<SQLFederationRuleConfiguration> getRuleConfigurationType() {
        return SQLFederationRuleConfiguration.class;
    }
}
