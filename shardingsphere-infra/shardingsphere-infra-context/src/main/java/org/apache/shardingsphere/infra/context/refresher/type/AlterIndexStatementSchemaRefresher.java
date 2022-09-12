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

import com.google.common.base.Preconditions;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.context.refresher.MetaDataRefresher;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.schema.decorator.model.ShardingSphereIndex;
import org.apache.shardingsphere.infra.metadata.database.schema.decorator.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.database.schema.decorator.model.ShardingSphereTable;
import org.apache.shardingsphere.infra.metadata.database.schema.event.MetaDataRefreshedEvent;
import org.apache.shardingsphere.infra.metadata.database.schema.event.SchemaAlteredEvent;
import org.apache.shardingsphere.sql.parser.sql.common.segment.ddl.index.IndexSegment;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.AlterIndexStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.handler.ddl.AlterIndexStatementHandler;

import java.util.Collection;
import java.util.Optional;

/**
 * Schema refresher for alter index statement.
 */
public final class AlterIndexStatementSchemaRefresher implements MetaDataRefresher<AlterIndexStatement> {
    
    @Override
    public Optional<MetaDataRefreshedEvent> refresh(final ShardingSphereDatabase database, final Collection<String> logicDataSourceNames,
                                                    final String schemaName, final AlterIndexStatement sqlStatement, final ConfigurationProperties props) {
        Optional<IndexSegment> renameIndex = AlterIndexStatementHandler.getRenameIndexSegment(sqlStatement);
        if (!sqlStatement.getIndex().isPresent() || !renameIndex.isPresent()) {
            return Optional.empty();
        }
        String actualSchemaName = sqlStatement.getIndex().get().getOwner().map(optional -> optional.getIdentifier().getValue().toLowerCase()).orElse(schemaName);
        String indexName = sqlStatement.getIndex().get().getIndexName().getIdentifier().getValue();
        Optional<String> logicTableName = findLogicTableName(database.getSchema(actualSchemaName), indexName);
        if (logicTableName.isPresent()) {
            ShardingSphereTable table = database.getSchema(actualSchemaName).getTable(logicTableName.get());
            Preconditions.checkNotNull(table, "Can not get the table '%s' metadata!", logicTableName.get());
            table.getIndexes().remove(indexName);
            String renameIndexName = renameIndex.get().getIndexName().getIdentifier().getValue();
            table.getIndexes().put(renameIndexName, new ShardingSphereIndex(renameIndexName));
            SchemaAlteredEvent event = new SchemaAlteredEvent(database.getName(), actualSchemaName);
            event.getAlteredTables().add(table);
            return Optional.of(event);
        }
        return Optional.empty();
    }
    
    private Optional<String> findLogicTableName(final ShardingSphereSchema schema, final String indexName) {
        return schema.getAllTableNames().stream().filter(each -> schema.getTable(each).getIndexes().containsKey(indexName)).findFirst();
    }
    
    @Override
    public String getType() {
        return AlterIndexStatement.class.getName();
    }
}
