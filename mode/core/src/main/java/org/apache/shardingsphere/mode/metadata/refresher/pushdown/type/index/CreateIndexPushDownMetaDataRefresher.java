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

package org.apache.shardingsphere.mode.metadata.refresher.pushdown.type.index;

import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.database.core.type.DatabaseType;
import org.apache.shardingsphere.infra.exception.core.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.exception.kernel.metadata.SchemaNotFoundException;
import org.apache.shardingsphere.infra.exception.kernel.metadata.TableNotFoundException;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereIndex;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereTable;
import org.apache.shardingsphere.infra.metadata.database.schema.util.IndexMetaDataUtils;
import org.apache.shardingsphere.mode.metadata.refresher.pushdown.PushDownMetaDataRefresher;
import org.apache.shardingsphere.mode.persist.service.MetaDataManagerPersistService;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.CreateIndexStatement;

import java.util.Collections;
import java.util.LinkedList;

/**
 * Create index push down meta data refresher.
 */
public final class CreateIndexPushDownMetaDataRefresher implements PushDownMetaDataRefresher<CreateIndexStatement> {
    
    @Override
    public void refresh(final MetaDataManagerPersistService metaDataManagerPersistService, final ShardingSphereDatabase database, final String logicDataSourceName,
                        final String schemaName, final DatabaseType databaseType, final CreateIndexStatement sqlStatement, final ConfigurationProperties props) {
        String indexName = null == sqlStatement.getIndex()
                ? IndexMetaDataUtils.getGeneratedLogicIndexName(sqlStatement.getColumns())
                : sqlStatement.getIndex().getIndexName().getIdentifier().getValue();
        String tableName = sqlStatement.getTable().getTableName().getIdentifier().getValue();
        ShardingSpherePreconditions.checkState(database.containsSchema(schemaName), () -> new SchemaNotFoundException(schemaName));
        ShardingSphereSchema schema = database.getSchema(schemaName);
        ShardingSpherePreconditions.checkState(schema.containsTable(tableName), () -> new TableNotFoundException(tableName));
        ShardingSphereTable table = schema.getTable(tableName);
        ShardingSphereTable newTable = new ShardingSphereTable(table.getName(), table.getAllColumns(), table.getAllIndexes(), table.getAllConstraints(), table.getType());
        newTable.putIndex(new ShardingSphereIndex(indexName, new LinkedList<>(), false));
        metaDataManagerPersistService.alterTables(database, schemaName, Collections.singleton(newTable));
    }
    
    @Override
    public Class<CreateIndexStatement> getType() {
        return CreateIndexStatement.class;
    }
}
