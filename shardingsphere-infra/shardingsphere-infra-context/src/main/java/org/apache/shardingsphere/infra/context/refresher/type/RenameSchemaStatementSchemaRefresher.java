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
import org.apache.shardingsphere.infra.metadata.schema.event.AlterSchemaEvent;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.AlterSchemaStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.opengauss.ddl.OpenGaussAlterSchemaStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.ddl.PostgreSQLAlterSchemaStatement;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;

/**
 * Schema refresher for rename schema statement.
 */
public final class RenameSchemaStatementSchemaRefresher implements MetaDataRefresher<AlterSchemaStatement> {
    
    private static final String TYPE = AlterSchemaStatement.class.getName();
    @Override
    public void refresh(ShardingSphereMetaData metaData, FederationDatabaseMetaData database, Map<String, OptimizerPlannerContext> optimizerPlanners,
                        Collection<String> logicDataSourceNames, String schemaName, AlterSchemaStatement sqlStatement, ConfigurationProperties props) throws SQLException {
        Optional<String> renameSchemaName = sqlStatement instanceof PostgreSQLAlterSchemaStatement ?
                ((PostgreSQLAlterSchemaStatement) sqlStatement).getRenameSchema() : ((OpenGaussAlterSchemaStatement) sqlStatement).getRenameSchema();
        if (!renameSchemaName.isPresent()) {
            return;
        }
        Optional<FederationSchemaMetaData> schemaMetadata = database.getSchemaMetadata(sqlStatement.getSchemaName());
        if (schemaMetadata.isPresent()) {
            AlterSchemaEvent event = new AlterSchemaEvent(metaData.getDatabaseName(), sqlStatement.getSchemaName(), renameSchemaName.get(),
                    new ShardingSphereSchema(metaData.getSchemaByName(sqlStatement.getSchemaName()).getTables()));
            metaData.getSchemas().remove(sqlStatement.getSchemaName());
            database.remove(sqlStatement.getSchemaName());
            database.put(renameSchemaName.get(), schemaMetadata.get());
            optimizerPlanners.put(database.getName(), OptimizerPlannerContextFactory.create(database));
            ShardingSphereEventBus.getInstance().post(event);
            // TODO Maybe need to refresh tables for SingleTableRule
        }
    }
    
    @Override
    public String getType() {
        return TYPE;
    }
}
