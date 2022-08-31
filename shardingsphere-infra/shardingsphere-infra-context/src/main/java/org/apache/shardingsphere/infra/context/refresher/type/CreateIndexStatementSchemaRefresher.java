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
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.schema.decorator.model.ShardingSphereIndex;
import org.apache.shardingsphere.infra.metadata.database.schema.event.MetaDataRefreshedEvent;
import org.apache.shardingsphere.infra.metadata.database.schema.event.SchemaAlteredEvent;
import org.apache.shardingsphere.infra.metadata.database.schema.util.IndexMetaDataUtil;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.CreateIndexStatement;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Optional;

/**
 * Schema refresher for create index statement.
 */
public final class CreateIndexStatementSchemaRefresher implements MetaDataRefresher<CreateIndexStatement> {
    
    @Override
    public Optional<MetaDataRefreshedEvent> refresh(final ShardingSphereDatabase database, final Collection<String> logicDataSourceNames,
                                                    final String schemaName, final CreateIndexStatement sqlStatement, final ConfigurationProperties props) throws SQLException {
        String indexName = null != sqlStatement.getIndex() ? sqlStatement.getIndex().getIndexName().getIdentifier().getValue()
                : IndexMetaDataUtil.getGeneratedLogicIndexName(sqlStatement.getColumns());
        if (Strings.isNullOrEmpty(indexName)) {
            return Optional.empty();
        }
        String tableName = sqlStatement.getTable().getTableName().getIdentifier().getValue();
        database.getSchema(schemaName).getTable(tableName).getIndexes().put(indexName, new ShardingSphereIndex(indexName));
        SchemaAlteredEvent event = new SchemaAlteredEvent(database.getName(), schemaName);
        event.getAlteredTables().add(database.getSchema(schemaName).getTable(tableName));
        return Optional.of(event);
    }
    
    @Override
    public String getType() {
        return CreateIndexStatement.class.getName();
    }
}
