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

package org.apache.shardingsphere.infra.metadata.refresh.impl;

import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.refresh.MetaDataRefreshStrategy;
import org.apache.shardingsphere.infra.metadata.refresh.TableMetaDataLoaderCallback;
import org.apache.shardingsphere.sql.parser.binder.metadata.index.IndexMetaData;
import org.apache.shardingsphere.sql.parser.binder.statement.ddl.CreateIndexStatementContext;
import org.apache.shardingsphere.sql.parser.sql.statement.ddl.CreateIndexStatement;

import javax.sql.DataSource;
import java.util.Map;

/**
 * Create index statement meta data refresh strategy.
 */
public final class CreateIndexStatementMetaDataRefreshStrategy implements MetaDataRefreshStrategy<CreateIndexStatementContext> {
    
    @Override
    public void refreshMetaData(final ShardingSphereMetaData metaData, final DatabaseType databaseType,
                                final Map<String, DataSource> dataSourceMap, final CreateIndexStatementContext sqlStatementContext, final TableMetaDataLoaderCallback callback) {
        CreateIndexStatement createIndexStatement = sqlStatementContext.getSqlStatement();
        if (null == createIndexStatement.getIndex()) {
            return;
        }
        String indexName = createIndexStatement.getIndex().getIdentifier().getValue();
        String tableName = createIndexStatement.getTable().getTableName().getIdentifier().getValue();
        metaData.getSchema().getConfiguredSchemaMetaData().get(tableName).getIndexes().put(indexName, new IndexMetaData(indexName));
    }
}
