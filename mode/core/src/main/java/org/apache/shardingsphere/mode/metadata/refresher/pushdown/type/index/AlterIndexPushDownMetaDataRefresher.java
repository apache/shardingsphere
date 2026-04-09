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

import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.exception.kernel.metadata.IndexNotFoundException;
import org.apache.shardingsphere.infra.exception.kernel.metadata.SchemaNotFoundException;
import org.apache.shardingsphere.infra.exception.kernel.metadata.TableNotFoundException;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereIndex;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereTable;
import org.apache.shardingsphere.mode.metadata.refresher.pushdown.PushDownMetaDataRefresher;
import org.apache.shardingsphere.mode.metadata.refresher.util.SchemaRefreshUtils;
import org.apache.shardingsphere.mode.metadata.refresher.util.TableRefreshUtils;
import org.apache.shardingsphere.mode.persist.service.MetaDataManagerPersistService;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.index.IndexSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.index.AlterIndexStatement;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;

import java.util.Collections;
import java.util.LinkedList;
import java.util.Optional;

/**
 * Alter index push down meta data refresher.
 */
public final class AlterIndexPushDownMetaDataRefresher implements PushDownMetaDataRefresher<AlterIndexStatement> {
    
    @Override
    public void refresh(final MetaDataManagerPersistService metaDataManagerPersistService, final ShardingSphereDatabase database, final String logicDataSourceName,
                        final String schemaName, final DatabaseType databaseType, final AlterIndexStatement sqlStatement, final ConfigurationProperties props) {
        Optional<IndexSegment> renameIndex = sqlStatement.getRenameIndex();
        if (!sqlStatement.getIndex().isPresent() || !renameIndex.isPresent()) {
            return;
        }
        IndexSegment indexSegment = sqlStatement.getIndex().get();
        String actualSchemaName = SchemaRefreshUtils.getActualSchemaName(database,
                indexSegment.getOwner().map(optional -> optional.getIdentifier()).orElse(new IdentifierValue(schemaName)), props);
        String actualTableName = TableRefreshUtils.findActualTableNameByIndex(database, actualSchemaName, indexSegment.getIndexName().getIdentifier(), props)
                .orElseThrow(() -> new IndexNotFoundException(indexSegment.getIndexName().getIdentifier().getValue(), actualSchemaName));
        String indexName = TableRefreshUtils.getActualIndexName(database, actualSchemaName, actualTableName, indexSegment.getIndexName().getIdentifier(), props);
        ShardingSpherePreconditions.checkState(database.containsSchema(actualSchemaName), () -> new SchemaNotFoundException(actualSchemaName));
        ShardingSphereSchema schema = database.getSchema(actualSchemaName);
        ShardingSpherePreconditions.checkState(schema.containsTable(actualTableName), () -> new TableNotFoundException(actualTableName));
        ShardingSphereTable table = schema.getTable(actualTableName);
        ShardingSphereTable newTable = new ShardingSphereTable(table.getName(), table.getAllColumns(), table.getAllIndexes(), table.getAllConstraints(), table.getType());
        newTable.removeIndex(indexName);
        String renameIndexName = TableRefreshUtils.getActualIndexName(database, actualSchemaName, actualTableName, renameIndex.get().getIndexName().getIdentifier(), props);
        newTable.putIndex(new ShardingSphereIndex(renameIndexName, new LinkedList<>(), false));
        metaDataManagerPersistService.alterTables(database, actualSchemaName, Collections.singleton(newTable));
    }
    
    @Override
    public Class<AlterIndexStatement> getType() {
        return AlterIndexStatement.class;
    }
}
