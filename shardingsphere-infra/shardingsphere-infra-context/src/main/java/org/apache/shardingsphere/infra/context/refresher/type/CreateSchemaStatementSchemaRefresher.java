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

package org.apache.shardingsphere.infra.context.refresher.type;

import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.context.refresher.MetaDataRefresher;
import org.apache.shardingsphere.infra.eventbus.ShardingSphereEventBus;
import org.apache.shardingsphere.infra.federation.optimizer.context.planner.OptimizerPlannerContext;
import org.apache.shardingsphere.infra.federation.optimizer.context.planner.OptimizerPlannerContextFactory;
import org.apache.shardingsphere.infra.federation.optimizer.metadata.FederationDatabaseMetaData;
import org.apache.shardingsphere.infra.federation.optimizer.metadata.FederationSchemaMetaData;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.schema.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.schema.event.AddSchemaEvent;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.CreateSchemaStatement;
import org.apache.shardingsphere.sql.parser.sql.common.value.identifier.IdentifierValue;
import org.apache.shardingsphere.sql.parser.sql.dialect.handler.ddl.CreateSchemaStatementHandler;

import java.sql.SQLException;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Schema refresher for create schema statement.
 */
public final class CreateSchemaStatementSchemaRefresher implements MetaDataRefresher<CreateSchemaStatement> {
    
    private static final String TYPE = CreateSchemaStatement.class.getName();
    
    @Override
    public void refresh(final ShardingSphereMetaData metaData, final FederationDatabaseMetaData database, final Map<String, OptimizerPlannerContext> optimizerPlanners,
                        final Collection<String> logicDataSourceNames, final String schemaName, final CreateSchemaStatement sqlStatement, final ConfigurationProperties props) throws SQLException {
        Optional<IdentifierValue> schema = sqlStatement.getSchemaName().isPresent() ? sqlStatement.getSchemaName() : CreateSchemaStatementHandler.getUsername(sqlStatement);
        if (!schema.isPresent()) {
            return;
        }
        String actualSchemaName = schema.get().getValue();
        metaData.getSchemas().put(actualSchemaName, new ShardingSphereSchema());
        database.putSchemaMetadata(actualSchemaName, new FederationSchemaMetaData(actualSchemaName, new LinkedHashMap<>()));
        optimizerPlanners.put(database.getName(), OptimizerPlannerContextFactory.create(database));
        AddSchemaEvent event = new AddSchemaEvent(metaData.getDatabaseName(), actualSchemaName);
        ShardingSphereEventBus.getInstance().post(event);
    }
    
    @Override
    public String getType() {
        return TYPE;
    }
}
