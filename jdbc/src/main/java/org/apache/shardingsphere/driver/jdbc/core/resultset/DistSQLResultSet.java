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

package org.apache.shardingsphere.driver.jdbc.core.resultset;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.merge.result.impl.local.LocalDataQueryResultRow;

import javax.sql.rowset.CachedRowSet;
import javax.sql.rowset.RowSetMetaDataImpl;
import javax.sql.rowset.RowSetProvider;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Creates JDBC result sets for DistSQL local rows.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DistSQLResultSet {
    
    /**
     * Create a result set from DistSQL rows.
     *
     * @param columnNames column names
     * @param rows local rows
     * @param statement statement that produced the result set
     * @return JDBC result set
     * @throws SQLException SQL exception
     */
    public static ResultSet create(final Collection<String> columnNames, final Collection<LocalDataQueryResultRow> rows, final Statement statement) throws SQLException {
        RowSetMetaDataImpl metaData = new RowSetMetaDataImpl();
        metaData.setColumnCount(columnNames.size());
        int columnIndex = 1;
        for (String each : columnNames) {
            metaData.setColumnName(columnIndex, each);
            metaData.setColumnLabel(columnIndex, each);
            metaData.setColumnType(columnIndex, Types.CHAR);
            metaData.setColumnTypeName(columnIndex, "CHAR");
            metaData.setPrecision(columnIndex, 255);
            metaData.setNullable(columnIndex, ResultSetMetaData.columnNullable);
            columnIndex++;
        }
        CachedRowSet result = RowSetProvider.newFactory().createCachedRowSet();
        result.setMetaData(metaData);
        List<LocalDataQueryResultRow> orderedRows = new ArrayList<>(rows);
        for (int rowIndex = orderedRows.size() - 1; rowIndex >= 0; rowIndex--) {
            result.moveToInsertRow();
            for (int i = 1; i <= columnNames.size(); i++) {
                result.updateObject(i, orderedRows.get(rowIndex).getCell(i));
            }
            result.insertRow();
            result.moveToCurrentRow();
        }
        result.beforeFirst();
        result.setType(statement.getResultSetType());
        result.setConcurrency(statement.getResultSetConcurrency());
        return (ResultSet) Proxy.newProxyInstance(DistSQLResultSet.class.getClassLoader(), new Class<?>[]{ResultSet.class}, new LifecycleHandler(result, statement));
    }
    
    private static final class LifecycleHandler implements InvocationHandler {
        
        private final CachedRowSet delegate;
        
        private final Statement statement;
        
        private boolean closed;
        
        private LifecycleHandler(final CachedRowSet delegate, final Statement statement) {
            this.delegate = delegate;
            this.statement = statement;
        }
        
        @Override
        public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
            if ("equals".equals(method.getName())) {
                return proxy == args[0];
            }
            if ("hashCode".equals(method.getName())) {
                return System.identityHashCode(proxy);
            }
            if ("toString".equals(method.getName())) {
                return "DistSQLResultSet";
            }
            if ("isClosed".equals(method.getName())) {
                return closed;
            }
            if ("close".equals(method.getName())) {
                closed = true;
                delegate.close();
                return null;
            }
            if (closed) {
                throw new SQLException("Result set is closed.");
            }
            if ("getStatement".equals(method.getName())) {
                return statement;
            }
            try {
                return method.invoke(delegate, args);
            } catch (final InvocationTargetException ex) {
                throw ex.getCause();
            }
        }
    }
}
