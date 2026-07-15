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

package org.apache.shardingsphere.mode.metadata.refresher.pushdown.type.table;

import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereIndex;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereTable;
import org.apache.shardingsphere.mode.metadata.refresher.pushdown.PushDownMetaDataRefresher;
import org.apache.shardingsphere.mode.metadata.refresher.util.TableRefreshUtils;
import org.apache.shardingsphere.mode.persist.service.MetaDataManagerPersistService;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.constraint.ConstraintDefinitionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.index.IndexSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.table.CreateTableStatement;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.stream.Collectors;

/**
 * Create table push down meta data refresher.
 */
public final class CreateTablePushDownMetaDataRefresher implements PushDownMetaDataRefresher<CreateTableStatement> {
    
    private final TableMetaDataRefresherLoader metaDataLoader = new TableMetaDataRefresherLoader();
    
    @Override
    public void refresh(final MetaDataManagerPersistService metaDataManagerPersistService, final ShardingSphereDatabase database, final String logicDataSourceName,
                        final String schemaName, final DatabaseType databaseType, final CreateTableStatement sqlStatement, final ConfigurationProperties props) throws SQLException {
        ShardingSphereTable loadedTable = metaDataLoader.loadCreatedTable(database, logicDataSourceName, schemaName, sqlStatement.getTable().getTableName().getIdentifier(), props,
                createRevisionCandidateSchemas(database, schemaName, sqlStatement, props));
        metaDataManagerPersistService.createTable(database, schemaName, loadedTable);
    }
    
    private Collection<ShardingSphereSchema> createRevisionCandidateSchemas(final ShardingSphereDatabase database, final String schemaName,
                                                                            final CreateTableStatement sqlStatement, final ConfigurationProperties props) {
        Collection<ShardingSphereIndex> indexes = createRevisionCandidateIndexes(sqlStatement);
        if (indexes.isEmpty()) {
            return database.getAllSchemas();
        }
        Collection<ShardingSphereSchema> result = new LinkedList<>(database.getAllSchemas());
        String tableName = TableRefreshUtils.getTableLoadCandidateName(database, sqlStatement.getTable().getTableName().getIdentifier());
        result.add(new ShardingSphereSchema(schemaName, database.getProtocolType(),
                Collections.singleton(new ShardingSphereTable(tableName, Collections.emptyList(), indexes, Collections.emptyList())), Collections.emptyList()));
        return result;
    }
    
    private Collection<ShardingSphereIndex> createRevisionCandidateIndexes(final CreateTableStatement sqlStatement) {
        Collection<ShardingSphereIndex> result = new LinkedList<>();
        for (ConstraintDefinitionSegment each : sqlStatement.getConstraintDefinitions()) {
            if (!each.getIndexName().isPresent()) {
                continue;
            }
            IndexSegment indexSegment = each.getIndexName().get();
            Collection<String> columns = each.getIndexColumns().stream().map(ColumnSegment::getIdentifier).map(IdentifierValue::getValue).collect(Collectors.toList());
            result.add(new ShardingSphereIndex(indexSegment.getIndexName().getIdentifier().getValue(), columns, each.isUniqueKey() || indexSegment.isUniqueKey()));
        }
        return result;
    }
    
    @Override
    public Class<CreateTableStatement> getType() {
        return CreateTableStatement.class;
    }
}
