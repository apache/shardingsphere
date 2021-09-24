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

import org.apache.shardingsphere.infra.config.properties.ConfigurationProperties;
import org.apache.shardingsphere.infra.eventbus.ShardingSphereEventBus;
import org.apache.shardingsphere.infra.metadata.MetaDataRefresher;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.mapper.SQLStatementEventMapper;
import org.apache.shardingsphere.infra.metadata.mapper.SQLStatementEventMapperFactory;
import org.apache.shardingsphere.infra.metadata.schema.builder.SchemaBuilderMaterials;
import org.apache.shardingsphere.infra.metadata.schema.refresher.SchemaRefresher;
import org.apache.shardingsphere.infra.metadata.schema.refresher.event.SchemaAlteredEvent;
import org.apache.shardingsphere.infra.optimize.core.metadata.FederationSchemaMetaData;
import org.apache.shardingsphere.infra.optimize.core.metadata.refresher.FederationMetaDataRefresher;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Optional;

/**
 * Meta data refresh engine.
 */
public final class MetaDataRefreshEngine {
    
    private final ShardingSphereMetaData schemaMetaData;
    
    private final FederationSchemaMetaData federationMetaData;
    
    private final SchemaBuilderMaterials materials;
    
    public MetaDataRefreshEngine(final ShardingSphereMetaData schemaMetaData, final FederationSchemaMetaData federationMetaData, final ConfigurationProperties props) {
        this.schemaMetaData = schemaMetaData;
        this.federationMetaData = federationMetaData;
        materials = new SchemaBuilderMaterials(schemaMetaData.getResource().getDatabaseType(), schemaMetaData.getResource().getDataSources(), schemaMetaData.getRuleMetaData().getRules(), props);
    }
    
    /**
     * Refresh.
     *
     * @param sqlStatement SQL statement
     * @param logicDataSourceNames logic data source names
     * @throws SQLException SQL exception
     */
    public void refresh(final SQLStatement sqlStatement, final Collection<String> logicDataSourceNames) throws SQLException {
        Collection<MetaDataRefresher> metaDataRefreshers = MetaDataRefresherFactory.newInstance(sqlStatement);
        if (!metaDataRefreshers.isEmpty()) {
            refresh(sqlStatement, logicDataSourceNames, metaDataRefreshers);
        }
        Optional<SQLStatementEventMapper> sqlStatementEventMapper = SQLStatementEventMapperFactory.newInstance(sqlStatement);
        if (sqlStatementEventMapper.isPresent()) {
            ShardingSphereEventBus.getInstance().post(sqlStatementEventMapper.get().map(sqlStatement));
            // TODO Subscribe and handle DCLStatementEvent
        }
    }
    
    @SuppressWarnings({"unchecked", "rawtypes"})
    private void refresh(final SQLStatement sqlStatement, final Collection<String> logicDataSourceNames, final Collection<MetaDataRefresher> refreshers) throws SQLException {
        for (MetaDataRefresher each : refreshers) {
            if (each instanceof SchemaRefresher) {
                ((SchemaRefresher) each).refresh(schemaMetaData, logicDataSourceNames, sqlStatement, materials.getProps());
            }
            if (each instanceof FederationMetaDataRefresher) {
                ((FederationMetaDataRefresher) each).refresh(federationMetaData, logicDataSourceNames, sqlStatement, materials);
            }
        }
        ShardingSphereEventBus.getInstance().post(new SchemaAlteredEvent(schemaMetaData.getName(), schemaMetaData.getSchema()));
    }
}
