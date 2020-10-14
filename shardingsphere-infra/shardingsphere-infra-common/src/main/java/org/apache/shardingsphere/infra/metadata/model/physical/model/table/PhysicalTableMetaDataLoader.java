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

package org.apache.shardingsphere.infra.metadata.model.physical.model.table;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.metadata.model.physical.jdbc.MetaDataConnectionAdapter;
import org.apache.shardingsphere.infra.metadata.model.physical.jdbc.handler.impl.DatabaseSpecialHandlerFacade;
import org.apache.shardingsphere.infra.metadata.model.physical.model.column.PhysicalColumnMetaDataLoader;
import org.apache.shardingsphere.infra.metadata.model.physical.model.index.PhysicalIndexMetaDataLoader;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

/**
 * Physical table meta data loader.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class PhysicalTableMetaDataLoader {

    /**
     * Load table meta data.
     *
     * @param dataSource data source
     * @param tableNamePattern table name pattern
     * @param databaseType database type
     * @return table meta data
     * @throws SQLException SQL exception
     */
    public static Optional<PhysicalTableMetaData> load(final DataSource dataSource, final String tableNamePattern, final DatabaseType databaseType) throws SQLException {
        try (MetaDataConnectionAdapter connectionAdapter = new MetaDataConnectionAdapter(databaseType, dataSource.getConnection())) {
            String tableName = DatabaseSpecialHandlerFacade.getTableNamePattern(tableNamePattern, databaseType);
            return isTableExist(connectionAdapter, tableName)
                    ? Optional.of(new PhysicalTableMetaData(
                          PhysicalColumnMetaDataLoader.load(connectionAdapter, tableName, databaseType), PhysicalIndexMetaDataLoader.load(connectionAdapter, tableName)))
                    : Optional.empty();
        }
    }
    
    private static boolean isTableExist(final Connection connection, final String tableNamePattern) throws SQLException {
        try (ResultSet resultSet = connection.getMetaData().getTables(connection.getCatalog(), connection.getSchema(), tableNamePattern, null)) {
            return resultSet.next();
        }
    }
}
