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

package org.apache.shardingsphere.data.pipeline.postgresql.ddlgenerator;

import lombok.Getter;
import lombok.SneakyThrows;
import org.apache.shardingsphere.data.pipeline.postgresql.util.PostgreSQLPipelineFreemarkerManager;

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
 * Abstract postgres ddl adapter.
 */
@Getter
public abstract class AbstractPostgresDDLAdapter {
    
    private static final String SECURITY_LABEL_SPLIT = "=";
    
    private final Connection connection;
    
    private final int majorVersion;
    
    private final int minorVersion;
    
    protected AbstractPostgresDDLAdapter(final Connection connection, final int majorVersion, final int minorVersion) {
        this.connection = connection;
        this.majorVersion = majorVersion;
        this.minorVersion = minorVersion;
    }
    
    @SneakyThrows(SQLException.class)
    protected Collection<Map<String, Object>> executeByTemplate(final Map<String, Object> params, final String path) {
        try (
                Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery(PostgreSQLPipelineFreemarkerManager.getSQLByVersion(params, path, majorVersion, minorVersion))) {
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
    
    protected void formatSecurityLabels(final Map<String, Object> data) throws SQLException {
        if (null == data.get("seclabels")) {
            return;
        }
        Collection<Map<String, String>> formatLabels = new LinkedList<>();
        Collection<String> securityLabels = Arrays.stream((String[]) ((Array) data.get("seclabels")).getArray()).collect(Collectors.toList());
        for (String each : securityLabels) {
            Map<String, String> securityLabel = new LinkedHashMap<>();
            securityLabel.put("provider", each.substring(0, each.indexOf(SECURITY_LABEL_SPLIT)));
            securityLabel.put("label", each.substring(each.indexOf(SECURITY_LABEL_SPLIT) + 1));
            formatLabels.add(securityLabel);
        }
        data.put("seclabels", formatLabels);
    }
}
