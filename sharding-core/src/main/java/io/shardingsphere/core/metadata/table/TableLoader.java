/*
 * Copyright 2016-2018 shardingsphere.io.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingsphere.core.metadata.table;

import lombok.RequiredArgsConstructor;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.LinkedList;

/**
 * Table loader.
 *
 * @author zhangliang
 */
@RequiredArgsConstructor
public final class TableLoader {
    
    private final TableMetaDataExecutorAdapter executorAdapter;
    
    /**
     * Get all table names.
     *
     * @param dataSourceName data source name
     * @return table names
     * @throws SQLException SQL exception
     */
    public Collection<String> getAllTableNames(final String dataSourceName) throws SQLException {
        Collection<String> result = new LinkedList<>();
        try (Connection connection = executorAdapter.getConnection(dataSourceName);
             ResultSet resultSet = connection.getMetaData().getTables(null, null, null, null)) {
            while (resultSet.next()) {
                result.add(resultSet.getString("TABLE_NAME"));
            }
        }
        return result;
    }
}
