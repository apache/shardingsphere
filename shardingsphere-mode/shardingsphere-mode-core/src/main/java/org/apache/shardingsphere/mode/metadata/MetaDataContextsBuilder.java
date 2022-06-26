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

package org.apache.shardingsphere.mode.metadata;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.config.RuleConfiguration;
import org.apache.shardingsphere.infra.config.database.DatabaseConfiguration;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.federation.optimizer.context.OptimizerContextFactory;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabasesFactory;
import org.apache.shardingsphere.infra.metadata.database.rule.ShardingSphereRuleMetaData;
import org.apache.shardingsphere.infra.rule.builder.global.GlobalRulesBuilder;
import org.apache.shardingsphere.mode.metadata.persist.MetaDataPersistService;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Map;

/**
 * Meta data contexts builder.
 */
@RequiredArgsConstructor
public final class MetaDataContextsBuilder {
    
    private final Map<String, DatabaseConfiguration> databaseConfigMap;
    
    private final Collection<RuleConfiguration> globalRuleConfigs;
    
    private final ConfigurationProperties props;
    
    /**
     * Build meta data contexts.
     * 
     * @param persistService meta data persist service
     * @exception SQLException SQL exception
     * @return meta data contexts
     */
    public MetaDataContexts build(final MetaDataPersistService persistService) throws SQLException {
        Map<String, ShardingSphereDatabase> databases = ShardingSphereDatabasesFactory.create(databaseConfigMap, props);
        ShardingSphereRuleMetaData globalMetaData = new ShardingSphereRuleMetaData(GlobalRulesBuilder.buildRules(globalRuleConfigs, databases));
        ShardingSphereMetaData metaData = new ShardingSphereMetaData(databases, globalMetaData, props);
        return new MetaDataContexts(persistService, metaData, OptimizerContextFactory.create(databases, globalMetaData));
    }
}
