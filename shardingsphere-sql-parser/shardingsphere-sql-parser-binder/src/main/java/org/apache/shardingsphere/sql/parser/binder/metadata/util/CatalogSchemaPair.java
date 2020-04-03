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

package org.apache.shardingsphere.sql.parser.binder.metadata.util;

import com.google.common.base.Strings;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Catalog schema pair.
 */
@RequiredArgsConstructor
@Getter
public class CatalogSchemaPair {
    
    private static final String PG_CATALOG_SCHEMA_SQL = "SELECT current_database(), current_schema()";
    
    private final String catalog;
    
    private final String schema;
    
    /**
     * Create catalog schema pair from connection.
     *
     * @param connection connection
     * @param databaseType database type
     * @return catalog schema pair
     * @throws SQLException SQL exception
     */
    public static CatalogSchemaPair of(final Connection connection, final String databaseType) throws SQLException {
        String catalog = null;
        String schema = null;
        try {
            catalog = connection.getCatalog();
            schema = connection.getSchema();
        } catch (final SQLException ignore) {
        }
        if ((null == catalog || Strings.isNullOrEmpty(schema)) && "PostgreSQL".equals(databaseType)) {
            return getCatalogSchemaPair(connection);
        }
        return new CatalogSchemaPair(catalog, schema);
    }
    
    private static CatalogSchemaPair getCatalogSchemaPair(final Connection connection) throws SQLException {
        String catalog = null;
        String schema = null;
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(PG_CATALOG_SCHEMA_SQL)) {
            if (resultSet.next()) {
                catalog = resultSet.getString(1);
                schema = resultSet.getString(2);
            }
        }
        return new CatalogSchemaPair(catalog, schema);
    }
}
