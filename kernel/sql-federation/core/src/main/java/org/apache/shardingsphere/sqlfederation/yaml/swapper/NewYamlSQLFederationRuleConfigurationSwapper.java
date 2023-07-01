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

package org.apache.shardingsphere.sqlfederation.yaml.swapper;

import org.apache.shardingsphere.infra.config.nodepath.GlobalNodePath;
import org.apache.shardingsphere.infra.util.yaml.YamlEngine;
import org.apache.shardingsphere.infra.util.yaml.datanode.YamlDataNode;
import org.apache.shardingsphere.infra.yaml.config.swapper.rule.NewYamlGlobalRuleConfigurationSwapper;
import org.apache.shardingsphere.sql.parser.api.CacheOption;
import org.apache.shardingsphere.sqlfederation.api.config.SQLFederationRuleConfiguration;
import org.apache.shardingsphere.sqlfederation.constant.SQLFederationOrder;
import org.apache.shardingsphere.sqlfederation.yaml.config.YamlSQLFederationRuleConfiguration;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

/**
 * TODO Rename YamlSQLTranslatorRuleConfigurationSwapper when metadata structure adjustment completed. #25485
 * YAML SQL federation rule configuration swapper.
 */
public final class NewYamlSQLFederationRuleConfigurationSwapper implements NewYamlGlobalRuleConfigurationSwapper<SQLFederationRuleConfiguration> {
    
    private final YamlSQLFederationExecutionPlanCacheConfigurationSwapper executionPlanCacheConfigSwapper = new YamlSQLFederationExecutionPlanCacheConfigurationSwapper();
    
    @Override
    public Collection<YamlDataNode> swapToDataNodes(final SQLFederationRuleConfiguration data) {
        return Collections.singletonList(new YamlDataNode(getRuleTagName().toLowerCase(), YamlEngine.marshal(swapToYamlConfiguration(data))));
    }
    
    private YamlSQLFederationRuleConfiguration swapToYamlConfiguration(final SQLFederationRuleConfiguration data) {
        YamlSQLFederationRuleConfiguration result = new YamlSQLFederationRuleConfiguration();
        result.setSqlFederationEnabled(data.isSqlFederationEnabled());
        result.setExecutionPlanCache(executionPlanCacheConfigSwapper.swapToYamlConfiguration(data.getExecutionPlanCache()));
        return result;
    }
    
    @Override
    public Optional<SQLFederationRuleConfiguration> swapToObject(final Collection<YamlDataNode> dataNodes) {
        for (YamlDataNode each : dataNodes) {
            Optional<String> version = GlobalNodePath.getVersion(getRuleTagName().toLowerCase(), each.getKey());
            if (!version.isPresent()) {
                continue;
            }
            return Optional.of(swapToObject(YamlEngine.unmarshal(each.getValue(), YamlSQLFederationRuleConfiguration.class)));
        }
        return Optional.empty();
    }
    
    private SQLFederationRuleConfiguration swapToObject(final YamlSQLFederationRuleConfiguration yamlConfig) {
        CacheOption executionPlanCacheConfig = executionPlanCacheConfigSwapper.swapToObject(yamlConfig.getExecutionPlanCache());
        return new SQLFederationRuleConfiguration(yamlConfig.isSqlFederationEnabled(), executionPlanCacheConfig);
    }
    
    @Override
    public Class<SQLFederationRuleConfiguration> getTypeClass() {
        return SQLFederationRuleConfiguration.class;
    }
    
    @Override
    public String getRuleTagName() {
        return "SQL_FEDERATION";
    }
    
    @Override
    public int getOrder() {
        return SQLFederationOrder.ORDER;
    }
}
