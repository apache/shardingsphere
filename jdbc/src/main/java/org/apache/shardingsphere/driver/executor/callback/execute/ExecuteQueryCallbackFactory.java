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

package org.apache.shardingsphere.driver.executor.callback.execute;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.driver.executor.callback.execute.impl.PreparedStatementExecuteQueryCallback;
import org.apache.shardingsphere.driver.executor.callback.execute.impl.StatementExecuteQueryCallback;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.SQLExecutorExceptionHandler;
import org.apache.shardingsphere.infra.executor.sql.prepare.driver.jdbc.JDBCDriverType;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.session.query.QueryContext;

/**
 * Execute query callback factory.
 */
@RequiredArgsConstructor
public final class ExecuteQueryCallbackFactory {
    
    private final JDBCDriverType type;
    
    /**
     * Create new instance of execute query callback.
     * @param database database
     * @param queryContext query context
     * @return created instance
     */
    public ExecuteQueryCallback newInstance(final ShardingSphereDatabase database, final QueryContext queryContext) {
        return JDBCDriverType.STATEMENT == type
                ? new StatementExecuteQueryCallback(database.getProtocolType(), database.getResourceMetaData(),
                        queryContext.getSqlStatementContext().getSqlStatement(), SQLExecutorExceptionHandler.isExceptionThrown())
                : new PreparedStatementExecuteQueryCallback(database.getProtocolType(), database.getResourceMetaData(),
                        queryContext.getSqlStatementContext().getSqlStatement(), SQLExecutorExceptionHandler.isExceptionThrown());
    }
}
