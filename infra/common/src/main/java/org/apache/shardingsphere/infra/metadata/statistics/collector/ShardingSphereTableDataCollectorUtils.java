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

package org.apache.shardingsphere.infra.metadata.statistics.collector;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.metadata.statistics.ShardingSphereRowData;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereColumn;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereTable;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Table data collector utility class.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ShardingSphereTableDataCollectorUtils {
    
    /**
     * Collect row data.
     *
     * @param shardingSphereDatabase ShardingSphere database
     * @param sql sql
     * @param table table
     * @param selectedColumnNames selected column names
     * @return ShardingSphere row data
     * @throws SQLException sql exception
     */
    public static Collection<ShardingSphereRowData> collectRowData(final ShardingSphereDatabase shardingSphereDatabase, final String sql, final ShardingSphereTable table,
                                                                   final Collection<String> selectedColumnNames) throws SQLException {
        if (isProtocolTypeAndStorageTypeDifferent(shardingSphereDatabase)) {
            return Collections.emptyList();
        }
        Collection<ShardingSphereRowData> result = new LinkedList<>();
        for (DataSource each : shardingSphereDatabase.getResourceMetaData().getDataSources().values()) {
            try (
                    Connection connection = each.getConnection();
                    Statement statement = connection.createStatement();
                    ResultSet resultSet = statement.executeQuery(sql)) {
                result.addAll(getRows(resultSet, table, selectedColumnNames));
            }
        }
        return result;
    }
    
    private static boolean isProtocolTypeAndStorageTypeDifferent(final ShardingSphereDatabase shardingSphereDatabase) {
        return !shardingSphereDatabase.getResourceMetaData().getStorageTypes().values().stream().allMatch(each -> each.getType().equals(shardingSphereDatabase.getProtocolType().getType()));
    }
    
    private static Collection<ShardingSphereRowData> getRows(final ResultSet resultSet, final ShardingSphereTable table, final Collection<String> selectedColumnNames) throws SQLException {
        Collection<ShardingSphereRowData> result = new LinkedList<>();
        while (resultSet.next()) {
            result.add(new ShardingSphereRowData(getRow(table, resultSet, selectedColumnNames)));
        }
        return result;
    }
    
    private static List<Object> getRow(final ShardingSphereTable table, final ResultSet resultSet, final Collection<String> selectedColumnNames) throws SQLException {
        List<Object> result = new LinkedList<>();
        for (ShardingSphereColumn each : table.getColumnValues()) {
            if (selectedColumnNames.contains(each.getName())) {
                result.add(convertIfNecessary(resultSet.getObject(each.getName()), each.getDataType()));
            } else {
                result.add(mockValue(each.getDataType()));
            }
        }
        return result;
    }
    
    private static Object mockValue(final int dataType) {
        switch (dataType) {
            case Types.BIGINT:
                return 0L;
            case Types.VARCHAR:
            case Types.CHAR:
            case Types.OTHER:
            case Types.ARRAY:
                return "";
            case Types.INTEGER:
            case Types.SMALLINT:
                return 0;
            case Types.REAL:
                return Float.valueOf("0");
            case Types.BIT:
                return false;
            default:
                return null;
        }
    }
    
    private static Object convertIfNecessary(final Object data, final int dataType) {
        if (Types.ARRAY == dataType) {
            return null == data ? null : data.toString();
        }
        if (Types.BIGINT == dataType) {
            return null == data ? null : Long.valueOf(data.toString());
        }
        return data;
    }
}
