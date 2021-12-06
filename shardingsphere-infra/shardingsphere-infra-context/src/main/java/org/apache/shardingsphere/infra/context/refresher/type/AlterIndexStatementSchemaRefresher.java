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
import org.apache.shardingsphere.infra.config.properties.ConfigurationProperties;
import org.apache.shardingsphere.infra.context.refresher.MetaDataRefresher;
import org.apache.shardingsphere.infra.federation.optimizer.metadata.FederationSchemaMetaData;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.schema.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.schema.model.IndexMetaData;
import org.apache.shardingsphere.infra.metadata.schema.model.TableMetaData;
import org.apache.shardingsphere.sql.parser.sql.common.segment.ddl.index.IndexSegment;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.AlterIndexStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.handler.ddl.AlterIndexStatementHandler;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Optional;

/**
 * Schema refresher for alter index statement.
 */
public final class AlterIndexStatementSchemaRefresher implements MetaDataRefresher<AlterIndexStatement> {
    
    @Override
    public void refresh(final ShardingSphereMetaData schemaMetaData, final FederationSchemaMetaData schema, final Collection<String> logicDataSourceNames, final AlterIndexStatement sqlStatement, 
                        final ConfigurationProperties props) throws SQLException {
        Optional<IndexSegment> renameIndex = AlterIndexStatementHandler.getRenameIndexSegment(sqlStatement);
        if (!sqlStatement.getIndex().isPresent() || !renameIndex.isPresent()) {
            return;
        }
        String indexName = sqlStatement.getIndex().get().getIdentifier().getValue();
        Optional<String> logicTableName = findLogicTableName(schemaMetaData.getSchema(), indexName);
        if (logicTableName.isPresent()) {
            TableMetaData tableMetaData = schemaMetaData.getSchema().get(logicTableName.get());
            Preconditions.checkNotNull(tableMetaData, "Can not get the table '%s' metadata!", logicTableName.get());
            tableMetaData.getIndexes().remove(indexName);
            String renameIndexName = renameIndex.get().getIdentifier().getValue();
            tableMetaData.getIndexes().put(renameIndexName, new IndexMetaData(renameIndexName));
        }
    }
    
    private Optional<String> findLogicTableName(final ShardingSphereSchema schema, final String indexName) {
        return schema.getAllTableNames().stream().filter(each -> schema.get(each).getIndexes().containsKey(indexName)).findFirst();
    }
    
    @Override
    public String getType() {
        return AlterIndexStatement.class.getCanonicalName();
    }
}
