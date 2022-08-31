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

package org.apache.shardingsphere.infra.metadata.database.schema.loader.common;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.metadata.database.schema.loader.adapter.MetaDataLoaderConnectionAdapter;
import org.apache.shardingsphere.infra.metadata.database.schema.loader.model.ViewMetaData;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

/**
 * View meta data loader.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ViewMetaDataLoader {
    
    /**
     * Load view meta data.
     *
     * @param dataSource data source
     * @param databaseType database type
     * @return table meta data
     * @throws SQLException SQL exception
     */
    public static Optional<ViewMetaData> load(final DataSource dataSource, final DatabaseType databaseType) throws SQLException {
        try (MetaDataLoaderConnectionAdapter connectionAdapter = new MetaDataLoaderConnectionAdapter(databaseType, dataSource.getConnection())) {
            return Optional.ofNullable(load(connectionAdapter));
        }
    }
    
    private static ViewMetaData load(final Connection connection) throws SQLException {
        try (ResultSet resultSet = connection.getMetaData().getTables(connection.getCatalog(), connection.getSchema(), null, new String[]{"VIEW"})) {
            while (resultSet.next()) {
                String tableName = resultSet.getString("TABLE_NAME");
                String viewDefinition = resultSet.getString("VIEW_DEFINITION");
                if (null != tableName && null != viewDefinition) {
                    return new ViewMetaData(tableName, viewDefinition);
                }
            }
        }
        return null;
    }
}
