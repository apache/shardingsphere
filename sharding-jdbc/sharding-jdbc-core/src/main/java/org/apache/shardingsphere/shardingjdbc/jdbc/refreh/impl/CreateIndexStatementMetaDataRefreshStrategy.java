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

package org.apache.shardingsphere.shardingjdbc.jdbc.refreh.impl;

import org.apache.shardingsphere.shardingjdbc.jdbc.core.context.ShardingRuntimeContext;
import org.apache.shardingsphere.shardingjdbc.jdbc.refreh.MetaDataRefreshStrategy;
import org.apache.shardingsphere.sql.parser.binder.metadata.index.IndexMetaData;
import org.apache.shardingsphere.sql.parser.binder.statement.ddl.CreateIndexStatementContext;
import org.apache.shardingsphere.sql.parser.sql.statement.ddl.CreateIndexStatement;

/**
 * Create index statement meta data refresh strategy.
 */
public final class CreateIndexStatementMetaDataRefreshStrategy implements MetaDataRefreshStrategy<CreateIndexStatementContext> {
   
    @Override
    public void refreshMetaData(final ShardingRuntimeContext shardingRuntimeContext, final CreateIndexStatementContext sqlStatementContext) {
        CreateIndexStatement createIndexStatement = sqlStatementContext.getSqlStatement();
        if (null == createIndexStatement.getIndex()) {
            return;
        }
        String indexName = createIndexStatement.getIndex().getIdentifier().getValue();
        String tableName = createIndexStatement.getTable().getTableName().getIdentifier().getValue();
        shardingRuntimeContext.getMetaData().getSchema().get(tableName).getIndexes().put(indexName, new IndexMetaData(indexName));
    }
}
