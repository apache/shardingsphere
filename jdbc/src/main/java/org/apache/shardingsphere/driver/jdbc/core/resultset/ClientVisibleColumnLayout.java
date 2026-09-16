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

import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.exception.kernel.syntax.ColumnIndexOutOfRangeException;

import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Arrays;

/**
 * Client-visible column layout of a result set, computed once per result set metadata instance.
 *
 * <p>The layout holds the decision whether the expanded projections describe the client-visible columns and,
 * when they do not, how a client-visible ordinal maps to the index of the returned metadata. Column count, names
 * and labels reuse the layout, so reading a whole header row, e.g. for proxy query headers and MySQL
 * prepared-statement metadata, does not repeat the reconciliation of the returned metadata per accessor.</p>
 *
 * <p>When nothing can append derived columns, the client ordinal is the index of the returned metadata as-is
 * (passthrough) and the column count still reads the returned metadata, which keeps the translation out of the
 * way for callers that already know their column count.</p>
 */
final class ClientVisibleColumnLayout {
    
    private final boolean useExpandedProjections;
    
    private final boolean passthrough;
    
    private final int[] visibleColumnIndexes;
    
    private ClientVisibleColumnLayout(final boolean useExpandedProjections, final boolean passthrough, final int[] visibleColumnIndexes) {
        this.useExpandedProjections = useExpandedProjections;
        this.passthrough = passthrough;
        this.visibleColumnIndexes = visibleColumnIndexes;
    }
    
    static ClientVisibleColumnLayout create(final SQLStatementContext sqlStatementContext, final ResultSetMetaData resultSetMetaData) throws SQLException {
        if (ShardingSphereResultSetUtils.useExpandedProjections(sqlStatementContext, resultSetMetaData)) {
            return new ClientVisibleColumnLayout(true, false, new int[0]);
        }
        if (!ShardingSphereResultSetUtils.mayAppendDerivedColumns(sqlStatementContext)) {
            return new ClientVisibleColumnLayout(false, true, new int[0]);
        }
        int columnCount = resultSetMetaData.getColumnCount();
        int[] result = new int[columnCount];
        int visibleColumnCount = 0;
        for (int columnIndex = 1; columnIndex <= columnCount; columnIndex++) {
            if (!ShardingSphereResultSetUtils.isDerivedColumnLabel(resultSetMetaData.getColumnLabel(columnIndex))) {
                result[visibleColumnCount++] = columnIndex;
            }
        }
        return new ClientVisibleColumnLayout(false, false, Arrays.copyOf(result, visibleColumnCount));
    }
    
    boolean isUseExpandedProjections() {
        return useExpandedProjections;
    }
    
    boolean isPassthrough() {
        return passthrough;
    }
    
    int getVisibleColumnCount() {
        return visibleColumnIndexes.length;
    }
    
    int getVisibleColumnIndex(final int column) throws SQLException {
        if (column < 1 || column > visibleColumnIndexes.length) {
            throw new ColumnIndexOutOfRangeException(column).toSQLException();
        }
        return visibleColumnIndexes[column - 1];
    }
}
