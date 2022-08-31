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
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.schema.QualifiedTable;
import org.apache.shardingsphere.infra.metadata.database.schema.decorator.model.ShardingSphereTable;
import org.apache.shardingsphere.infra.metadata.database.schema.event.DropIndexEvent;
import org.apache.shardingsphere.infra.metadata.database.schema.event.MetaDataRefreshedEvent;
import org.apache.shardingsphere.infra.metadata.database.schema.event.SchemaAlteredEvent;
import org.apache.shardingsphere.infra.metadata.database.schema.util.IndexMetaDataUtil;
import org.apache.shardingsphere.sql.parser.sql.common.segment.ddl.index.IndexSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.DropIndexStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.handler.ddl.DropIndexStatementHandler;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

/**
 * Schema refresher for drop index statement.
 */
public final class DropIndexStatementSchemaRefresher implements MetaDataRefresher<DropIndexStatement> {
    
    @Override
    public Optional<MetaDataRefreshedEvent> refresh(final ShardingSphereDatabase database, final Collection<String> logicDataSourceNames,
                                                    final String schemaName, final DropIndexStatement sqlStatement, final ConfigurationProperties props) throws SQLException {
        DropIndexEvent event = new DropIndexEvent();
        for (IndexSegment each : sqlStatement.getIndexes()) {
            String actualSchemaName = each.getOwner().map(optional -> optional.getIdentifier().getValue().toLowerCase()).orElse(schemaName);
            Optional<String> logicTableName = findLogicTableName(database, sqlStatement, Collections.singletonList(each));
            if (!logicTableName.isPresent()) {
                continue;
            }
            ShardingSphereTable table = database.getSchema(actualSchemaName).getTable(logicTableName.get());
            table.getIndexes().remove(each.getIndexName().getIdentifier().getValue());
            event.getSchemaAlteredEvents().add(buildSchemaAlteredEvent(database.getName(), actualSchemaName, table));
        }
        return Optional.of(event);
    }
    
    private Optional<String> findLogicTableName(final ShardingSphereDatabase database, final DropIndexStatement sqlStatement, final Collection<IndexSegment> indexSegments) {
        Optional<SimpleTableSegment> simpleTableSegment = DropIndexStatementHandler.getSimpleTableSegment(sqlStatement);
        if (simpleTableSegment.isPresent()) {
            return Optional.of(simpleTableSegment.get().getTableName().getIdentifier().getValue());
        }
        Collection<QualifiedTable> tableNames = IndexMetaDataUtil.getTableNames(database, database.getResource().getDatabaseType(), indexSegments);
        return tableNames.isEmpty() ? Optional.empty() : Optional.of(tableNames.iterator().next().getTableName());
    }
    
    private SchemaAlteredEvent buildSchemaAlteredEvent(final String databaseName, final String schemaName, final ShardingSphereTable table) {
        SchemaAlteredEvent result = new SchemaAlteredEvent(databaseName, schemaName);
        result.getAlteredTables().add(table);
        return result;
    }
    
    @Override
    public String getType() {
        return DropIndexStatement.class.getName();
    }
}
