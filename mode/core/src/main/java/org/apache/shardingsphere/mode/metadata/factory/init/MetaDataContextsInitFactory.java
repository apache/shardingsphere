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

package org.apache.shardingsphere.mode.metadata.factory.init;

import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.config.rule.RuleConfiguration;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.resource.ResourceMetaData;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.infra.metadata.statistics.ShardingSphereStatistics;
import org.apache.shardingsphere.infra.metadata.statistics.builder.ShardingSphereStatisticsFactory;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.rule.builder.global.GlobalRulesBuilder;
import org.apache.shardingsphere.mode.manager.builder.ContextManagerBuilderParameter;
import org.apache.shardingsphere.mode.metadata.MetaDataContexts;
import org.apache.shardingsphere.mode.metadata.persist.MetaDataPersistFacade;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Map;

/**
 * Meta data contexts init factory.
 */
public abstract class MetaDataContextsInitFactory {
    
    /**
     * Create meta data contexts.
     *
     * @param param context manager builder parameter
     * @return created meta data contexts
     * @throws SQLException SQL exception
     */
    public abstract MetaDataContexts create(ContextManagerBuilderParameter param) throws SQLException;
    
    protected final MetaDataContexts create(final Collection<RuleConfiguration> globalRuleConfigs, final Map<String, DataSource> globalDataSources,
                                            final Collection<ShardingSphereDatabase> databases, final ConfigurationProperties props, final MetaDataPersistFacade persistFacade) {
        Collection<ShardingSphereRule> globalRules = GlobalRulesBuilder.buildRules(globalRuleConfigs, databases, props);
        ShardingSphereMetaData metaData = new ShardingSphereMetaData(databases, new ResourceMetaData(globalDataSources), new RuleMetaData(globalRules), props);
        ShardingSphereStatistics statistics = ShardingSphereStatisticsFactory.create(metaData, persistFacade.getStatisticsService().load(metaData));
        return new MetaDataContexts(metaData, statistics);
    }
}
