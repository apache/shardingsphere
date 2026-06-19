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
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereTable;
import org.apache.shardingsphere.mode.metadata.refresher.pushdown.PushDownMetaDataRefresher;
import org.apache.shardingsphere.mode.metadata.refresher.util.SchemaRefreshUtils;
import org.apache.shardingsphere.mode.metadata.refresher.util.TableRefreshUtils;
import org.apache.shardingsphere.mode.persist.service.MetaDataManagerPersistService;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.table.RenameTableDefinitionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.table.RenameTableStatement;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;

import java.util.Collection;
import java.util.LinkedList;

/**
 * Rename table push down meta data refresher.
 */
public final class RenameTablePushDownMetaDataRefresher implements PushDownMetaDataRefresher<RenameTableStatement> {
    
    @Override
    public void refresh(final MetaDataManagerPersistService metaDataManagerPersistService, final ShardingSphereDatabase database, final String logicDataSourceName,
                        final String schemaName, final DatabaseType databaseType, final RenameTableStatement sqlStatement, final ConfigurationProperties props) {
        String actualSchemaName = SchemaRefreshUtils.getActualSchemaName(database, new IdentifierValue(schemaName), props);
        Collection<ShardingSphereTable> alteredTables = new LinkedList<>();
        Collection<String> droppedTables = new LinkedList<>();
        for (RenameTableDefinitionSegment each : sqlStatement.getRenameTables()) {
            String toBeRenamedTableName = TableRefreshUtils.getActualTableName(database, actualSchemaName, each.getTable().getTableName().getIdentifier(), props);
            ShardingSphereTable toBeRenamedTable = database.getSchema(actualSchemaName).getTable(toBeRenamedTableName);
            alteredTables.add(new ShardingSphereTable(
                    TableRefreshUtils.getActualTableName(database, actualSchemaName, each.getRenameTable().getTableName().getIdentifier(), props),
                    toBeRenamedTable.getAllColumns(), toBeRenamedTable.getAllIndexes(), toBeRenamedTable.getAllConstraints()));
            droppedTables.add(toBeRenamedTableName);
        }
        metaDataManagerPersistService.alterTables(database, actualSchemaName, alteredTables);
        metaDataManagerPersistService.dropTables(database, actualSchemaName, droppedTables);
    }
    
    @Override
    public Class<RenameTableStatement> getType() {
        return RenameTableStatement.class;
    }
}
