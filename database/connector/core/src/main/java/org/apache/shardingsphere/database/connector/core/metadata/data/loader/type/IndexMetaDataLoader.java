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

package org.apache.shardingsphere.database.connector.core.metadata.data.loader.type;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.database.connector.core.metadata.data.model.IndexMetaData;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

/**
 * Index meta data loader.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class IndexMetaDataLoader {
    
    private static final String INDEX_NAME = "INDEX_NAME";
    
    private static final int ORACLE_VIEW_NOT_APPROPRIATE_VENDOR_CODE = 1702;
    
    /**
     * Load index meta data list.
     * In a few jdbc implementation(eg. oracle), return value of getIndexInfo contains a statistics record that not an index itself and INDEX_NAME is null.
     *
     * @param connection connection
     * @param table table name
     * @return index meta data list
     * @throws SQLException SQL exception
     */
    @SuppressWarnings("CollectionWithoutInitialCapacity")
    public static Collection<IndexMetaData> load(final Connection connection, final String table) throws SQLException {
        Map<String, IndexMetaData> result = new HashMap<>();
        try (ResultSet resultSet = connection.getMetaData().getIndexInfo(connection.getCatalog(), connection.getSchema(), table, false, false)) {
            while (resultSet.next()) {
                String indexName = resultSet.getString(INDEX_NAME);
                if (null == indexName) {
                    continue;
                }
                if (result.containsKey(indexName)) {
                    result.get(indexName).getColumns().add(resultSet.getString("COLUMN_NAME"));
                } else {
                    IndexMetaData indexMetaData = new IndexMetaData(indexName, new LinkedList<>(Collections.singleton(resultSet.getString("COLUMN_NAME"))));
                    indexMetaData.setUnique(!resultSet.getBoolean("NON_UNIQUE"));
                    result.put(indexName, indexMetaData);
                }
            }
        } catch (final SQLException ex) {
            if (ORACLE_VIEW_NOT_APPROPRIATE_VENDOR_CODE != ex.getErrorCode()) {
                throw ex;
            }
        }
        return result.values();
    }
}
