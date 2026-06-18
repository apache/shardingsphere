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

package org.apache.shardingsphere.infra.merge.result.impl.memory;

import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.QueryResult;
import org.apache.shardingsphere.infra.merge.result.MergedResult;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;

import java.io.InputStream;
import java.io.Reader;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.sql.SQLXML;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

/**
 * Memory merged result.
 *
 * @param <T> type of rule
 */
public abstract class MemoryMergedResult<T extends ShardingSphereRule> implements MergedResult {
    
    private static final Collection<Class<?>> INVALID_MEMORY_TYPES = new HashSet<>(Arrays.asList(Blob.class, Clob.class, Reader.class, InputStream.class, SQLXML.class));
    
    private final Iterator<MemoryQueryResultRow> memoryResultSetRows;
    
    private MemoryQueryResultRow currentResultSetRow;
    
    private boolean wasNull;
    
    protected MemoryMergedResult(final T rule, final ShardingSphereSchema schema, final SQLStatementContext sqlStatementContext, final List<QueryResult> queryResults) throws SQLException {
        List<MemoryQueryResultRow> memoryQueryResultRows = init(rule, schema, sqlStatementContext, queryResults);
        memoryResultSetRows = memoryQueryResultRows.iterator();
        if (!memoryQueryResultRows.isEmpty()) {
            currentResultSetRow = memoryQueryResultRows.get(0);
        }
    }
    
    protected abstract List<MemoryQueryResultRow> init(T rule, ShardingSphereSchema schema, SQLStatementContext sqlStatementContext, List<QueryResult> queryResults) throws SQLException;
    
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
        ShardingSpherePreconditions.checkNotContains(INVALID_MEMORY_TYPES, type, () -> new SQLFeatureNotSupportedException(String.format("Get value from `%s`", type.getName())));
        Object result = currentResultSetRow.getCell(columnIndex);
        wasNull = null == result;
        return result;
    }
    
    @Override
    public final Object getCalendarValue(final int columnIndex, final Class<?> type, @SuppressWarnings("UseOfObsoleteDateTimeApi") final Calendar calendar) {
        // TODO implement with calendar
        Object result = currentResultSetRow.getCell(columnIndex);
        wasNull = null == result;
        return result;
    }
    
    @Override
    public final InputStream getInputStream(final int columnIndex, final String type) throws SQLException {
        throw new SQLFeatureNotSupportedException(String.format("Get input stream from `%s`", type));
    }
    
    @Override
    public Reader getCharacterStream(final int columnIndex) throws SQLException {
        throw new SQLFeatureNotSupportedException("Get Character stream");
    }
    
    @Override
    public final boolean wasNull() {
        return wasNull;
    }
}
