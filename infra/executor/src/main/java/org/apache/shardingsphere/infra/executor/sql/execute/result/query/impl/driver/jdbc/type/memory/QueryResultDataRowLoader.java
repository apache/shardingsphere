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

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.database.connector.core.resultset.ResultSetMapper;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.annotation.HighFrequencyInvocation;
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
@RequiredArgsConstructor
public final class QueryResultDataRowLoader {
    
    private final DatabaseType databaseType;
    
    /**
     * Load query result data row.
     *
     * @param columnCount column count
     * @param resultSet result set
     * @return query result data row
     * @throws SQLException SQL exception
     */
    @HighFrequencyInvocation
    public Collection<MemoryQueryResultDataRow> load(final int columnCount, final ResultSet resultSet) throws SQLException {
        Collection<MemoryQueryResultDataRow> result = new LinkedList<>();
        if (!resultSet.next()) {
            return result;
        }
        ResultSetMapper resultSetMapper = new ResultSetMapper(databaseType);
        do {
            result.add(loadRow(columnCount, resultSet, resultSetMapper));
        } while (resultSet.next());
        return result;
    }
    
    private MemoryQueryResultDataRow loadRow(final int columnCount, final ResultSet resultSet, final ResultSetMapper resultSetMapper) throws SQLException {
        List<Object> rowData = new ArrayList<>(columnCount);
        for (int columnIndex = 1; columnIndex <= columnCount; columnIndex++) {
            Object rowValue = resultSetMapper.load(resultSet, columnIndex);
            rowData.add(resultSet.wasNull() ? null : rowValue);
        }
        return new MemoryQueryResultDataRow(rowData);
    }
}
