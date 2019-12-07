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

package org.apache.shardingsphere.core.merge.dql.common;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.core.execute.sql.execute.result.QueryResult;
import org.apache.shardingsphere.core.merge.MergedResult;
import org.apache.shardingsphere.core.metadata.table.TableMetas;
import org.apache.shardingsphere.core.rule.ShardingRule;
import org.apache.shardingsphere.sql.parser.relation.statement.SQLStatementContext;

import java.io.InputStream;
import java.io.Reader;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.sql.SQLXML;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;

/**
 * Memory merged result.
 *
 * @author zhangliang
 */
@RequiredArgsConstructor
public abstract class MemoryMergedResult implements MergedResult {
    
    private final Iterator<MemoryQueryResultRow> memoryResultSetRows;
    
    private MemoryQueryResultRow currentResultSetRow;
    
    private boolean wasNull;
    
    protected MemoryMergedResult(final ShardingRule shardingRule, 
                                 final TableMetas tableMetas, final SQLStatementContext sqlStatementContext, final List<QueryResult> queryResults) throws SQLException {
        List<MemoryQueryResultRow> memoryQueryResultRowList = init(shardingRule, tableMetas, sqlStatementContext, queryResults);
        memoryResultSetRows = memoryQueryResultRowList.iterator();
        if (!memoryQueryResultRowList.isEmpty()) {
            currentResultSetRow = memoryQueryResultRowList.get(0);
        }
    }
    
    protected abstract List<MemoryQueryResultRow> init(ShardingRule shardingRule, TableMetas tableMetas, 
                                                       SQLStatementContext sqlStatementContext, List<QueryResult> queryResults) throws SQLException;
    
    @Override
    public final boolean next() {
        if (memoryResultSetRows.hasNext()) {
            currentResultSetRow = memoryResultSetRows.next();
            return true;
        }
        return false;
    }
    
    @Override
    public final Object getValue(final int columnIndex, final Class<?> type) throws SQLException {
        if (Blob.class == type || Clob.class == type || Reader.class == type || InputStream.class == type || SQLXML.class == type) {
            throw new SQLFeatureNotSupportedException();
        }
        Object result = currentResultSetRow.getCell(columnIndex);
        wasNull = null == result;
        return result;
    }
    
    @Override
    public final Object getCalendarValue(final int columnIndex, final Class<?> type, final Calendar calendar) {
        // TODO implement with calendar
        Object result = currentResultSetRow.getCell(columnIndex);
        wasNull = null == result;
        return result;
    }
    
    @Override
    public final InputStream getInputStream(final int columnIndex, final String type) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }
    
    @Override
    public final boolean wasNull() {
        return wasNull;
    }
}
