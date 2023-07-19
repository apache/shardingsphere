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
import org.apache.commons.collections4.map.CaseInsensitiveMap;
import org.apache.shardingsphere.infra.binder.segment.select.projection.DerivedColumn;
import org.apache.shardingsphere.infra.binder.segment.select.projection.Projection;
import org.apache.shardingsphere.infra.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.statement.dml.SelectStatementContext;

import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * Utility class for {@link ShardingSphereResultSet}.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ShardingSphereResultSetUtils {
    
    /**
     * Create column label and index map.
     *
     * @param sqlStatementContext SQL statement context
     * @param selectContainsEnhancedTable select contains enhanced table
     * @param resultSetMetaData meta data of result set
     * @return column label and index map
     * @throws SQLException SQL exception
     */
    public static Map<String, Integer> createColumnLabelAndIndexMap(final SQLStatementContext sqlStatementContext, final boolean selectContainsEnhancedTable,
                                                                    final ResultSetMetaData resultSetMetaData) throws SQLException {
        if (selectContainsEnhancedTable && hasSelectExpandProjections(sqlStatementContext)) {
            return createColumnLabelAndIndexMapWithExpandProjections((SelectStatementContext) sqlStatementContext);
        }
        Map<String, Integer> result = new CaseInsensitiveMap<>(resultSetMetaData.getColumnCount(), 1);
        for (int columnIndex = resultSetMetaData.getColumnCount(); columnIndex > 0; columnIndex--) {
            result.put(resultSetMetaData.getColumnLabel(columnIndex), columnIndex);
        }
        return result;
    }
    
    private static Map<String, Integer> createColumnLabelAndIndexMapWithExpandProjections(final SelectStatementContext statementContext) {
        List<Projection> actualProjections = statementContext.getProjectionsContext().getExpandProjections();
        Map<String, Integer> result = new CaseInsensitiveMap<>(actualProjections.size(), 1F);
        for (int columnIndex = actualProjections.size(); columnIndex > 0; columnIndex--) {
            Projection projection = actualProjections.get(columnIndex - 1);
            result.put(DerivedColumn.isDerivedColumnName(projection.getColumnLabel()) ? projection.getColumnName() : projection.getColumnLabel(), columnIndex);
        }
        return result;
    }
    
    private static boolean hasSelectExpandProjections(final SQLStatementContext sqlStatementContext) {
        return sqlStatementContext instanceof SelectStatementContext && !((SelectStatementContext) sqlStatementContext).getProjectionsContext().getExpandProjections().isEmpty();
    }
}
