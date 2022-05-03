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
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.schema.event.AlterSchemaEvent;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.AlterSchemaStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.handler.ddl.AlterSchemaStatementHandler;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;

/**
 * Schema refresher for alter schema statement.
 */
public final class AlterSchemaStatementSchemaRefresher implements MetaDataRefresher<AlterSchemaStatement> {
    
    private static final String TYPE = AlterSchemaStatement.class.getName();
    
    @Override
    public void refresh(final ShardingSphereMetaData metaData, final FederationDatabaseMetaData database, final Map<String, OptimizerPlannerContext> optimizerPlanners,
                        final Collection<String> logicDataSourceNames, final String schemaName, final AlterSchemaStatement sqlStatement, final ConfigurationProperties props) throws SQLException {
        Optional<String> renameSchemaName = AlterSchemaStatementHandler.getRenameSchema(sqlStatement);
        if (!renameSchemaName.isPresent()) {
            return;
        }
        String actualSchemaName = sqlStatement.getSchemaName();
        putSchemaMetaData(metaData, database, optimizerPlanners, actualSchemaName, renameSchemaName.get());
        removeSchemaMetaData(metaData, database, optimizerPlanners, actualSchemaName);
        AlterSchemaEvent event = new AlterSchemaEvent(metaData.getDatabaseName(), actualSchemaName, renameSchemaName.get(), metaData.getSchemaByName(renameSchemaName.get()));
        ShardingSphereEventBus.getInstance().post(event);
        // TODO Maybe need to refresh tables for SingleTableRule
    }
    
    private void removeSchemaMetaData(final ShardingSphereMetaData metaData, final FederationDatabaseMetaData database, 
                                      final Map<String, OptimizerPlannerContext> optimizerPlanners, final String schemaName) {
        metaData.getSchemas().remove(schemaName);
        database.remove(schemaName);
        optimizerPlanners.put(database.getName(), OptimizerPlannerContextFactory.create(database));
    }
    
    private void putSchemaMetaData(final ShardingSphereMetaData metaData, final FederationDatabaseMetaData database, 
                                   final Map<String, OptimizerPlannerContext> optimizerPlanners, final String schemaName, final String renameSchemaName) {
        metaData.getSchemas().put(renameSchemaName, metaData.getSchemaByName(schemaName));
        database.getSchemaMetadata(schemaName).ifPresent(optional -> database.put(renameSchemaName, optional));
        optimizerPlanners.put(database.getName(), OptimizerPlannerContextFactory.create(database));
    }
    
    @Override
    public String getType() {
        return TYPE;
    }
}
