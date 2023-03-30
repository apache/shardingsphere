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

package org.apache.shardingsphere.proxy.backend.handler.distsql.ral.queryable.executor;

import org.apache.shardingsphere.distsql.handler.ral.query.QueryableRALExecutor;
import org.apache.shardingsphere.distsql.parser.statement.ral.QueryableRALStatement;
import org.apache.shardingsphere.infra.merge.result.impl.local.LocalDataQueryResultRow;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;

import java.util.Collection;

/**
 * Connection session required queryable RAL executor.
 * 
 * @param <T> type of SQL statement
 */
public interface ConnectionSessionRequiredQueryableRALExecutor<T extends QueryableRALStatement> extends QueryableRALExecutor<T> {
    
    /**
     * Get query result rows.
     *
     * @param metaData ShardingSphere meta data
     * @param connectionSession connectionSession connection session
     * @param sqlStatement SQL statement
     * @return query result rows
     */
    Collection<LocalDataQueryResultRow> getRows(ShardingSphereMetaData metaData, ConnectionSession connectionSession, T sqlStatement);
}
