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

package org.apache.shardingsphere.infra.context.refresher;

import org.apache.shardingsphere.infra.config.RuleConfiguration;
import org.apache.shardingsphere.infra.config.properties.ConfigurationProperties;
import org.apache.shardingsphere.infra.eventbus.ShardingSphereEventBus;
import org.apache.shardingsphere.infra.federation.optimizer.metadata.FederationSchemaMetaData;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.mapper.SQLStatementEventMapper;
import org.apache.shardingsphere.infra.metadata.mapper.SQLStatementEventMapperFactory;
import org.apache.shardingsphere.infra.metadata.schema.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.schema.loader.SchemaLoader;
import org.apache.shardingsphere.infra.metadata.schema.event.SchemaAlteredEvent;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.rule.builder.schema.SchemaRulesBuilder;
import org.apache.shardingsphere.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.spi.typed.TypedSPIRegistry;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

/**
 * Meta data refresh engine.
 */
public final class MetaDataRefreshEngine {
    
    static {
        ShardingSphereServiceLoader.register(MetaDataRefresher.class);
    }
    
    private final ShardingSphereMetaData schemaMetaData;
    
    private final FederationSchemaMetaData federationMetaData;
    
    private final ConfigurationProperties props;
    
    public MetaDataRefreshEngine(final ShardingSphereMetaData schemaMetaData, final FederationSchemaMetaData federationMetaData, final ConfigurationProperties props) {
        this.schemaMetaData = schemaMetaData;
        this.federationMetaData = federationMetaData;
        this.props = props;
    }
    
    /**
     * Refresh.
     *
     * @param sqlStatement SQL statement
     * @param logicDataSourceNames logic data source names
     * @throws SQLException SQL exception
     */
    public void refresh(final SQLStatement sqlStatement, final Collection<String> logicDataSourceNames) throws SQLException {
        Optional<MetaDataRefresher> schemaRefresher = TypedSPIRegistry.findRegisteredService(MetaDataRefresher.class, sqlStatement.getClass().getSuperclass().getCanonicalName(), null);
        if (schemaRefresher.isPresent()) {
            schemaRefresher.get().refresh(schemaMetaData, federationMetaData, logicDataSourceNames, sqlStatement, props);
            ShardingSphereEventBus.getInstance().post(new SchemaAlteredEvent(schemaMetaData.getName(), loadActualSchema(schemaMetaData)));
        }
        Optional<SQLStatementEventMapper> sqlStatementEventMapper = SQLStatementEventMapperFactory.newInstance(sqlStatement);
        if (sqlStatementEventMapper.isPresent()) {
            ShardingSphereEventBus.getInstance().post(sqlStatementEventMapper.get().map(sqlStatement));
            // TODO Subscribe and handle DCLStatementEvent
        }
    }
    
    private ShardingSphereSchema loadActualSchema(final ShardingSphereMetaData schemaMetaData) throws SQLException {
        Map<String, Map<String, DataSource>> dataSourcesMap = Collections.singletonMap(schemaMetaData.getName(), schemaMetaData.getResource().getDataSources());
        Map<String, Collection<RuleConfiguration>> schemaRuleConfigs = Collections.singletonMap(schemaMetaData.getName(), schemaMetaData.getRuleMetaData().getConfigurations());
        Map<String, Collection<ShardingSphereRule>> rules = SchemaRulesBuilder.buildRules(dataSourcesMap, schemaRuleConfigs, props.getProps());
        Map<String, ShardingSphereSchema> schemas = new SchemaLoader(dataSourcesMap, schemaRuleConfigs, rules, props.getProps()).load();
        return schemas.get(schemaMetaData.getName());
    }
}
