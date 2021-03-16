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

package org.apache.shardingsphere.db.discovery.common.rule.biulder;

import lombok.Setter;
import org.apache.shardingsphere.db.discovery.common.algorithm.config.AlgorithmProvidedDatabaseDiscoveryRuleConfiguration;
import org.apache.shardingsphere.db.discovery.common.constant.DatabaseDiscoveryOrder;
import org.apache.shardingsphere.db.discovery.common.rule.DatabaseDiscoveryRule;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.rule.builder.ShardingSphereRuleBuilder;
import org.apache.shardingsphere.infra.rule.builder.aware.ResourceAware;

import javax.sql.DataSource;
import java.util.Map;

/**
 * Algorithm provided data base discovery rule builder.
 */
@Setter
public final class AlgorithmProvidedDatabaseDiscoveryRuleBuilder implements ShardingSphereRuleBuilder<DatabaseDiscoveryRule, AlgorithmProvidedDatabaseDiscoveryRuleConfiguration>, ResourceAware {
    
    private DatabaseType databaseType;
    
    private Map<String, DataSource> dataSourceMap;
    
    private String schemaName;
    
    @Override
    public DatabaseDiscoveryRule build(final AlgorithmProvidedDatabaseDiscoveryRuleConfiguration ruleConfig) {
        return new DatabaseDiscoveryRule(ruleConfig, databaseType, dataSourceMap, schemaName);
    }
    
    @Override
    public int getOrder() {
        return DatabaseDiscoveryOrder.ORDER + 1;
    }
    
    @Override
    public Class<AlgorithmProvidedDatabaseDiscoveryRuleConfiguration> getTypeClass() {
        return AlgorithmProvidedDatabaseDiscoveryRuleConfiguration.class;
    }
}
