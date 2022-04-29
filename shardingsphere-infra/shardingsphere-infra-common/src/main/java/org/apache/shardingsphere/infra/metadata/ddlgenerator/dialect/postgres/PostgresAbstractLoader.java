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

package org.apache.shardingsphere.infra.metadata.ddlgenerator.dialect.postgres;

import lombok.Getter;
import lombok.SneakyThrows;
import org.apache.shardingsphere.infra.metadata.ddlgenerator.util.FreemarkerManager;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;

/**
 * Postgres abstract loader.
 */
@Getter
public abstract class PostgresAbstractLoader {
    
    private final Connection connection;
    
    protected PostgresAbstractLoader(final Connection connection) {
        this.connection = connection;
    }
    
    @SneakyThrows
    protected Collection<Map<String, Object>> executeByTemplate(final Map<String, Object> param, final String path) {
        try (
                Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery(FreemarkerManager.getSqlFromTemplate(param, path))) {
            return getRows(resultSet);
        }
    }
    
    protected Collection<Map<String, Object>> getRows(final ResultSet resultSet) throws SQLException {
        ResultSetMetaData metaData = resultSet.getMetaData();
        Collection<Map<String, Object>> result = new LinkedList<>();
        while (resultSet.next()) {
            Map<String, Object> row = new LinkedHashMap<>();
            for (int i = 1; i <= metaData.getColumnCount(); i++) {
                row.put(metaData.getColumnName(i), resultSet.getObject(i));
            }
            result.add(row);
        }
        return result;
    }
}
