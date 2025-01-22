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

package org.apache.shardingsphere.mode.metadata.refresher.type.index;

import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.database.core.type.DatabaseType;
import org.apache.shardingsphere.infra.exception.core.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.exception.kernel.metadata.SchemaNotFoundException;
import org.apache.shardingsphere.infra.exception.kernel.metadata.TableNotFoundException;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.schema.QualifiedTable;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereTable;
import org.apache.shardingsphere.infra.metadata.database.schema.pojo.AlterSchemaMetaDataPOJO;
import org.apache.shardingsphere.infra.metadata.database.schema.util.IndexMetaDataUtils;
import org.apache.shardingsphere.mode.metadata.refresher.MetaDataRefresher;
import org.apache.shardingsphere.mode.persist.service.MetaDataManagerPersistService;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.index.IndexSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.ddl.DropIndexStatement;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

/**
 * Schema refresher for drop index statement.
 */
public final class DropIndexStatementSchemaRefresher implements MetaDataRefresher<DropIndexStatement> {
    
    @Override
    public void refresh(final MetaDataManagerPersistService metaDataManagerPersistService, final ShardingSphereDatabase database, final Collection<String> logicDataSourceNames,
                        final String schemaName, final DatabaseType databaseType, final DropIndexStatement sqlStatement, final ConfigurationProperties props) {
        for (IndexSegment each : sqlStatement.getIndexes()) {
            String actualSchemaName = each.getOwner().map(optional -> optional.getIdentifier().getValue().toLowerCase()).orElse(schemaName);
            Optional<String> logicTableName = findLogicTableName(database, sqlStatement, Collections.singletonList(each));
            if (!logicTableName.isPresent()) {
                continue;
            }
            ShardingSpherePreconditions.checkState(database.containsSchema(actualSchemaName), () -> new SchemaNotFoundException(schemaName));
            ShardingSphereSchema schema = database.getSchema(actualSchemaName);
            ShardingSpherePreconditions.checkState(schema.containsTable(logicTableName.get()), () -> new TableNotFoundException(logicTableName.get()));
            ShardingSphereTable table = schema.getTable(logicTableName.get());
            ShardingSphereTable newTable = new ShardingSphereTable(table.getName(), table.getAllColumns(), table.getAllIndexes(), table.getAllConstraints(), table.getType());
            newTable.removeIndex(each.getIndexName().getIdentifier().getValue());
            AlterSchemaMetaDataPOJO alterSchemaMetaDataPOJO = new AlterSchemaMetaDataPOJO(database.getName(), actualSchemaName);
            alterSchemaMetaDataPOJO.getAlteredTables().add(newTable);
            metaDataManagerPersistService.alterSchemaMetaData(alterSchemaMetaDataPOJO);
        }
    }
    
    private Optional<String> findLogicTableName(final ShardingSphereDatabase database, final DropIndexStatement sqlStatement, final Collection<IndexSegment> indexSegments) {
        Optional<SimpleTableSegment> simpleTableSegment = sqlStatement.getSimpleTable();
        if (simpleTableSegment.isPresent()) {
            return Optional.of(simpleTableSegment.get().getTableName().getIdentifier().getValue());
        }
        Collection<QualifiedTable> tableNames = IndexMetaDataUtils.getTableNames(database, database.getProtocolType(), indexSegments);
        return tableNames.isEmpty() ? Optional.empty() : Optional.of(tableNames.iterator().next().getTableName());
    }
    
    @Override
    public Class<DropIndexStatement> getType() {
        return DropIndexStatement.class;
    }
}
