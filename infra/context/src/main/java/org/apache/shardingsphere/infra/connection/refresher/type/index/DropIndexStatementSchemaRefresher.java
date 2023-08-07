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

package org.apache.shardingsphere.infra.connection.refresher.type.index;

import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.connection.refresher.MetaDataRefresher;
import org.apache.shardingsphere.infra.instance.mode.ModeContextManager;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.schema.QualifiedTable;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereTable;
import org.apache.shardingsphere.infra.metadata.database.schema.pojo.AlterSchemaMetaDataPOJO;
import org.apache.shardingsphere.infra.metadata.database.schema.util.IndexMetaDataUtils;
import org.apache.shardingsphere.sql.parser.sql.common.segment.ddl.index.IndexSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.DropIndexStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.handler.ddl.DropIndexStatementHandler;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

/**
 * Schema refresher for drop index statement.
 */
public final class DropIndexStatementSchemaRefresher implements MetaDataRefresher<DropIndexStatement> {
    
    @Override
    public void refresh(final ModeContextManager modeContextManager, final ShardingSphereDatabase database, final Collection<String> logicDataSourceNames,
                        final String schemaName, final DropIndexStatement sqlStatement, final ConfigurationProperties props) {
        for (IndexSegment each : sqlStatement.getIndexes()) {
            String actualSchemaName = each.getOwner().map(optional -> optional.getIdentifier().getValue().toLowerCase()).orElse(schemaName);
            Optional<String> logicTableName = findLogicTableName(database, sqlStatement, Collections.singletonList(each));
            if (!logicTableName.isPresent()) {
                continue;
            }
            AlterSchemaMetaDataPOJO alterSchemaMetaDataPOJO = new AlterSchemaMetaDataPOJO(database.getName(), actualSchemaName);
            ShardingSphereTable table = newShardingSphereTable(database.getSchema(actualSchemaName).getTable(logicTableName.get()));
            table.removeIndex(each.getIndexName().getIdentifier().getValue());
            alterSchemaMetaDataPOJO.getAlteredTables().add(table);
            modeContextManager.alterSchemaMetaData(alterSchemaMetaDataPOJO);
        }
    }
    
    private Optional<String> findLogicTableName(final ShardingSphereDatabase database, final DropIndexStatement sqlStatement, final Collection<IndexSegment> indexSegments) {
        Optional<SimpleTableSegment> simpleTableSegment = DropIndexStatementHandler.getSimpleTableSegment(sqlStatement);
        if (simpleTableSegment.isPresent()) {
            return Optional.of(simpleTableSegment.get().getTableName().getIdentifier().getValue());
        }
        Collection<QualifiedTable> tableNames = IndexMetaDataUtils.getTableNames(database, database.getProtocolType(), indexSegments);
        return tableNames.isEmpty() ? Optional.empty() : Optional.of(tableNames.iterator().next().getTableName());
    }
    
    private ShardingSphereTable newShardingSphereTable(final ShardingSphereTable table) {
        ShardingSphereTable result = new ShardingSphereTable(table.getName(), table.getColumnValues(), table.getIndexValues(), table.getConstraintValues());
        result.getColumnNames().addAll(table.getColumnNames());
        result.getVisibleColumns().addAll(table.getVisibleColumns());
        result.getPrimaryKeyColumns().addAll(table.getPrimaryKeyColumns());
        return result;
    }
    
    @Override
    public Class<DropIndexStatement> getType() {
        return DropIndexStatement.class;
    }
}
