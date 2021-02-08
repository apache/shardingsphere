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

package org.apache.shardingsphere.infra.metadata.engine;

import org.apache.shardingsphere.infra.auth.Authentication;
import org.apache.shardingsphere.infra.config.properties.ConfigurationProperties;
import org.apache.shardingsphere.infra.eventbus.ShardingSphereEventBus;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.privilege.refresher.AuthenticationRefresher;
import org.apache.shardingsphere.infra.metadata.privilege.refresher.event.AuthenticationAlteredEvent;
import org.apache.shardingsphere.infra.metadata.schema.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.schema.builder.SchemaBuilderMaterials;
import org.apache.shardingsphere.infra.metadata.schema.refresher.SchemaRefresher;
import org.apache.shardingsphere.infra.metadata.schema.refresher.event.SchemaAlteredEvent;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Optional;

/**
 * Metadata refresh engine.
 */
public final class MetadataRefreshEngine {
    
    private final ShardingSphereMetaData metaData;
    
    private final Authentication authentication;
    
    private final SchemaBuilderMaterials materials;
    
    public MetadataRefreshEngine(final ShardingSphereMetaData metaData, final Authentication authentication, final ConfigurationProperties properties) {
        this.metaData = metaData;
        this.authentication = authentication;
        materials = new SchemaBuilderMaterials(metaData.getResource().getDatabaseType(), metaData.getResource().getDataSources(), metaData.getRuleMetaData().getRules(), properties);
    }
    
    /**
     * Refresh.
     *
     * @param sqlStatement sql statement
     * @param routeDataSourceNames route data source names
     * @throws SQLException sql exception
     */
    @SuppressWarnings("rawtypes")
    public void refresh(final SQLStatement sqlStatement, final Collection<String> routeDataSourceNames) throws SQLException {
        Optional<MetadataRefresher> metadataRefresher = MetadataRefresherFactory.newInstance(sqlStatement);
        if (metadataRefresher.isPresent()) {
            if (metadataRefresher.get() instanceof SchemaRefresher) {
                refreshSchema(sqlStatement, routeDataSourceNames, (SchemaRefresher) metadataRefresher.get());
            }
            if (metadataRefresher.get() instanceof AuthenticationRefresher) {
                refreshAuthentication(sqlStatement, (AuthenticationRefresher) metadataRefresher.get());
            }
        }
    }
    
    @SuppressWarnings({"unchecked", "rawtypes"})
    private void refreshSchema(final SQLStatement sqlStatement, final Collection<String> routeDataSourceNames, final SchemaRefresher refresher) throws SQLException {
        ShardingSphereSchema schema = metaData.getSchema();
        refresher.refresh(metaData.getSchema(), routeDataSourceNames, sqlStatement, materials);
        ShardingSphereEventBus.getInstance().post(new SchemaAlteredEvent(metaData.getName(), schema));
    }
    
    private void refreshAuthentication(final SQLStatement sqlStatement, final AuthenticationRefresher refresher) {
        refresher.refresh(authentication, sqlStatement, materials);
        // TODO :Subscribe and handle this event
        ShardingSphereEventBus.getInstance().post(new AuthenticationAlteredEvent(authentication));
    }
}
