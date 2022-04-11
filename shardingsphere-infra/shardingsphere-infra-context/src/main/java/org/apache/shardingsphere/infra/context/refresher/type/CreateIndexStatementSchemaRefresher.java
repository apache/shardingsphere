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

import com.google.common.base.Strings;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.context.refresher.MetaDataRefresher;
import org.apache.shardingsphere.infra.eventbus.ShardingSphereEventBus;
import org.apache.shardingsphere.infra.federation.optimizer.context.planner.OptimizerPlannerContext;
import org.apache.shardingsphere.infra.federation.optimizer.metadata.FederationDatabaseMetaData;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.schema.event.SchemaAlteredEvent;
import org.apache.shardingsphere.infra.metadata.schema.model.IndexMetaData;
import org.apache.shardingsphere.infra.metadata.schema.util.IndexMetaDataUtil;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.CreateIndexStatement;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Map;

/**
 * Schema refresher for create index statement.
 */
public final class CreateIndexStatementSchemaRefresher implements MetaDataRefresher<CreateIndexStatement> {
    
    private static final String TYPE = CreateIndexStatement.class.getName();
    
    @Override
    public void refresh(final ShardingSphereMetaData schemaMetaData, final FederationDatabaseMetaData database, final Map<String, OptimizerPlannerContext> optimizerPlanners,
                        final Collection<String> logicDataSourceNames, final CreateIndexStatement sqlStatement, final ConfigurationProperties props) throws SQLException {
        String indexName = null != sqlStatement.getIndex() ? sqlStatement.getIndex().getIdentifier().getValue() : IndexMetaDataUtil.getGeneratedLogicIndexName(sqlStatement.getColumns());
        if (Strings.isNullOrEmpty(indexName)) {
            return;
        }
        String tableName = sqlStatement.getTable().getTableName().getIdentifier().getValue();
        schemaMetaData.getDefaultSchema().get(tableName).getIndexes().put(indexName, new IndexMetaData(indexName));
        SchemaAlteredEvent event = new SchemaAlteredEvent(schemaMetaData.getName());
        event.getAlteredTables().add(schemaMetaData.getDefaultSchema().get(tableName));
        ShardingSphereEventBus.getInstance().post(event);
    }
    
    @Override
    public String getType() {
        return TYPE;
    }
}
