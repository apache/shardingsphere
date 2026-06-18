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

package org.apache.shardingsphere.infra.executor.sql.execute.result.query.impl.driver.jdbc.type.memory;

import org.apache.shardingsphere.database.connector.core.resultset.ResultSetMapper;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.type.memory.row.MemoryQueryResultDataRow;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * Query result data row loader.
 */
public final class QueryResultDataRowLoader {
    
    private final ResultSetMapper resultSetMapper;
    
    public QueryResultDataRowLoader(final DatabaseType databaseType) {
        resultSetMapper = new ResultSetMapper(databaseType);
    }
    
    /**
     * Load query result data row.
     *
     * @param columnCount column count
     * @param resultSet result set
     * @return query result data row
     * @throws SQLException SQL exception
     */
    public Collection<MemoryQueryResultDataRow> load(final int columnCount, final ResultSet resultSet) throws SQLException {
        Collection<MemoryQueryResultDataRow> result = new LinkedList<>();
        while (resultSet.next()) {
            List<Object> rowData = new ArrayList<>(columnCount);
            for (int columnIndex = 1; columnIndex <= columnCount; columnIndex++) {
                Object rowValue = resultSetMapper.load(resultSet, columnIndex);
                rowData.add(resultSet.wasNull() ? null : rowValue);
            }
            result.add(new MemoryQueryResultDataRow(rowData));
        }
        return result;
    }
}
