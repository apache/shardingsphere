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
import org.apache.shardingsphere.infra.metadata.model.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.model.physical.model.index.PhysicalIndexMetaData;
import org.apache.shardingsphere.infra.metadata.refresh.MetaDataRefreshStrategy;
import org.apache.shardingsphere.infra.metadata.refresh.TableMetaDataLoaderCallback;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.CreateIndexStatement;

import java.util.Collection;

/**
 * Create index statement meta data refresh strategy.
 */
public final class CreateIndexStatementMetaDataRefreshStrategy implements MetaDataRefreshStrategy<CreateIndexStatement> {
    
    @Override
    public void refreshMetaData(final ShardingSphereMetaData metaData, final DatabaseType databaseType, final Collection<String> routeDataSourceNames, 
                                final CreateIndexStatement sqlStatement, final TableMetaDataLoaderCallback callback) {
        if (null == sqlStatement.getIndex()) {
            return;
        }
        String indexName = sqlStatement.getIndex().getIdentifier().getValue();
        String tableName = sqlStatement.getTable().getTableName().getIdentifier().getValue();
        metaData.getSchemaMetaData().get(tableName).getIndexes().put(indexName, new PhysicalIndexMetaData(indexName));
    }
}
