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

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.config.RuleConfiguration;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.federation.optimizer.context.OptimizerContextFactory;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.rule.ShardingSphereRuleMetaData;
import org.apache.shardingsphere.infra.rule.builder.global.GlobalRulesBuilder;
import org.apache.shardingsphere.mode.metadata.persist.MetaDataPersistService;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Map;

/**
 * Meta data contexts builder.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class MetaDataContextsBuilder {
    
    /**
     * Build meta data contexts.
     * 
     * @param databases databases
     * @param globalRuleConfigs global rule configurations
     * @param props properties
     *      
     * @param persistService meta data persist service
     * @exception SQLException SQL exception
     * @return meta data contexts
     */
    public static MetaDataContexts build(final Map<String, ShardingSphereDatabase> databases, final Collection<RuleConfiguration> globalRuleConfigs,
                                  final ConfigurationProperties props, final MetaDataPersistService persistService) throws SQLException {
        ShardingSphereRuleMetaData globalMetaData = new ShardingSphereRuleMetaData(GlobalRulesBuilder.buildRules(globalRuleConfigs, databases));
        ShardingSphereMetaData metaData = new ShardingSphereMetaData(databases, globalMetaData, props);
        return new MetaDataContexts(persistService, metaData, OptimizerContextFactory.create(databases, globalMetaData));
    }
}
