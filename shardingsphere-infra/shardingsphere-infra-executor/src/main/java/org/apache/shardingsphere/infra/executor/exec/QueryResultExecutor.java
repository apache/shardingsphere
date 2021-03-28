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

package org.apache.shardingsphere.infra.executor.exec;

import org.apache.shardingsphere.infra.exception.ShardingSphereException;
import org.apache.shardingsphere.infra.executor.exec.meta.Row;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.QueryResult;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.QueryResultMetaData;

import java.sql.SQLException;

/**
 * Wrapper <code>QueryResult</code> as <code>Executor</code>.
 */
public final class QueryResultExecutor extends AbstractExecutor {
    
    private QueryResult queryResult;
    
    public QueryResultExecutor(final QueryResult queryResult, final ExecContext execContext) {
        super(execContext);
        this.queryResult = queryResult;
    }
    
    @Override
    protected void executeInit() {
        
    }
    
    @Override
    public boolean executeMove() {
        try {
            return queryResult.next();
        } catch (SQLException sqlException) {
            throw new ShardingSphereException("move next error", sqlException);
        }
    }
    
    @Override
    public Row current() {
        QueryResultMetaData metaData = this.getMetaData();
        try {
            int columnCount = metaData.getColumnCount();
            Object[] rowVal = new Object[columnCount];
            for (int i = 0; i < rowVal.length; i++) {
                rowVal[i] = queryResult.getValue(i + 1, Object.class);
            }
            return new Row(rowVal);
        } catch (SQLException t) {
            throw new ShardingSphereException("load row error", t);
        }
    }
    
    @Override
    public QueryResultMetaData getMetaData() {
        return queryResult.getMetaData();
    }
    
    @Override
    public void close() {
        try {
            queryResult.close();
        } catch (SQLException ex) {
            throw new ShardingSphereException("close query result error", ex);
        }
    }
}
