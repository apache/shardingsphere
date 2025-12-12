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

package org.apache.shardingsphere.data.pipeline.postgresql.sqlbuilder.ddl;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.data.pipeline.postgresql.sqlbuilder.template.PostgreSQLPipelineFreemarkerManager;
import org.apache.shardingsphere.infra.exception.external.sql.type.wrapper.SQLWrapperException;

import java.sql.Array;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Abstract ddl adapter for PostgreSQL.
 */
@RequiredArgsConstructor
@Getter
public final class PostgreSQLDDLTemplateExecutor {
    
    private static final String SECURITY_LABEL_SPLIT = "=";
    
    private final Connection connection;
    
    private final int majorVersion;
    
    private final int minorVersion;
    
    /**
     * Execute by template.
     *
     * @param params parameters
     * @param path path
     * @return execute result
     * @throws SQLWrapperException SQL wrapper exception
     */
    public Collection<Map<String, Object>> executeByTemplate(final Map<String, Object> params, final String path) {
        try (
                Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery(PostgreSQLPipelineFreemarkerManager.getSQLByVersion(params, path, majorVersion, minorVersion))) {
            return getRows(resultSet);
        } catch (final SQLException ex) {
            throw new SQLWrapperException(ex);
        }
    }
    
    private Collection<Map<String, Object>> getRows(final ResultSet resultSet) throws SQLException {
        ResultSetMetaData metaData = resultSet.getMetaData();
        Collection<Map<String, Object>> result = new LinkedList<>();
        while (resultSet.next()) {
            int columnCount = metaData.getColumnCount();
            Map<String, Object> row = new LinkedHashMap<>(columnCount);
            for (int i = 1; i <= columnCount; i++) {
                row.put(metaData.getColumnName(i), resultSet.getObject(i));
            }
            result.add(row);
        }
        return result;
    }
    
    /**
     * Execute by template for single row.
     *
     * @param params parameters
     * @param path path
     * @return execute result
     * @throws SQLWrapperException SQL wrapper exception
     */
    public Map<String, Object> executeByTemplateForSingleRow(final Map<String, Object> params, final String path) {
        try (
                Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery(PostgreSQLPipelineFreemarkerManager.getSQLByVersion(params, path, majorVersion, minorVersion))) {
            return getSingleRow(resultSet);
        } catch (final SQLException ex) {
            throw new SQLWrapperException(ex);
        }
    }
    
    private Map<String, Object> getSingleRow(final ResultSet resultSet) throws SQLException {
        ResultSetMetaData metaData = resultSet.getMetaData();
        int columnCount = metaData.getColumnCount();
        Map<String, Object> result = new LinkedHashMap<>(columnCount);
        if (resultSet.next()) {
            for (int i = 1; i <= columnCount; i++) {
                result.put(metaData.getColumnName(i), resultSet.getObject(i));
            }
        }
        return result;
    }
    
    /**
     * Format security labels.
     *
     * @param data data
     * @throws SQLException SQL exception
     */
    public void formatSecurityLabels(final Map<String, Object> data) throws SQLException {
        if (null == data.get("seclabels")) {
            return;
        }
        Collection<Map<String, String>> formatLabels = new LinkedList<>();
        Collection<String> securityLabels = Arrays.stream((String[]) ((Array) data.get("seclabels")).getArray()).collect(Collectors.toList());
        for (String each : securityLabels) {
            Map<String, String> securityLabel = new LinkedHashMap<>(2, 1F);
            securityLabel.put("provider", each.substring(0, each.indexOf(SECURITY_LABEL_SPLIT)));
            securityLabel.put("label", each.substring(each.indexOf(SECURITY_LABEL_SPLIT) + 1));
            formatLabels.add(securityLabel);
        }
        data.put("seclabels", formatLabels);
    }
}
