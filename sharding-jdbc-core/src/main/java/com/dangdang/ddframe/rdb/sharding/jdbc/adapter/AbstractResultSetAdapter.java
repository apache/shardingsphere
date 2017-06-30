/*
 * Copyright 1999-2015 dangdang.com.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package com.dangdang.ddframe.rdb.sharding.jdbc.adapter;

import com.dangdang.ddframe.rdb.sharding.jdbc.unsupported.AbstractUnsupportedOperationResultSet;
import com.dangdang.ddframe.rdb.sharding.util.SQLUtil;
import com.google.common.base.Preconditions;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * 代理结果集适配器.
 * 
 * @author zhangliang
 */
@Slf4j
public abstract class AbstractResultSetAdapter extends AbstractUnsupportedOperationResultSet {
    
    @Getter
    private final List<ResultSet> resultSets;
    
    @Getter
    private final Map<String, Integer> columnLabelIndexMap;

    private boolean closed;
    
    public AbstractResultSetAdapter(final List<ResultSet> resultSets) throws SQLException {
        Preconditions.checkArgument(!resultSets.isEmpty());
        this.resultSets = resultSets;
        columnLabelIndexMap = generateColumnLabelIndexMap();
    }
    
    private Map<String, Integer> generateColumnLabelIndexMap() throws SQLException {
        ResultSetMetaData resultSetMetaData = resultSets.get(0).getMetaData();
        Map<String, Integer> result = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        for (int i = 1; i <= resultSetMetaData.getColumnCount(); i++) {
            result.put(SQLUtil.getExactlyValue(resultSetMetaData.getColumnLabel(i)), i);
        }
        return result;
    }
    
    @Override
    public final void close() throws SQLException {
        closed = true;
        Collection<SQLException> exceptions = new LinkedList<>();
        for (ResultSet each : resultSets) {
            try {
                each.close();
            } catch (final SQLException ex) {
                exceptions.add(ex);
            }
        }
        throwSQLExceptionIfNecessary(exceptions);
    }
    
    @Override
    public final boolean isClosed() throws SQLException {
        return closed;
    }
    
    @Override
    public final void setFetchDirection(final int direction) throws SQLException {
        for (ResultSet each : resultSets) {
            each.setFetchDirection(direction);
        }
    }
    
    @Override
    public final void setFetchSize(final int rows) throws SQLException {
        for (ResultSet each : resultSets) {
            each.setFetchSize(rows);
        }
    }
}
