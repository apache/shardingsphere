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
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.resource.unit.StorageUnit;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereColumn;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereTable;
import org.apache.shardingsphere.infra.metadata.statistics.ShardingSphereRowData;

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
     * @param database ShardingSphere database
     * @param table table
     * @param selectedColumnNames selected column names
     * @param sql SQL
     * @return ShardingSphere row data
     * @throws SQLException sql exception
     */
    public static Collection<ShardingSphereRowData> collectRowData(final ShardingSphereDatabase database, final ShardingSphereTable table,
                                                                   final Collection<String> selectedColumnNames, final String sql) throws SQLException {
        if (isDifferentProtocolAndStorageType(database)) {
            return Collections.emptyList();
        }
        Collection<ShardingSphereRowData> result = new LinkedList<>();
        for (StorageUnit each : database.getResourceMetaData().getStorageUnits().values()) {
            try (
                    Connection connection = each.getDataSource().getConnection();
                    Statement statement = connection.createStatement();
                    ResultSet resultSet = statement.executeQuery(sql)) {
                result.addAll(getRows(table, selectedColumnNames, resultSet));
            }
        }
        return result;
    }
    
    private static boolean isDifferentProtocolAndStorageType(final ShardingSphereDatabase database) {
        return !database.getResourceMetaData().getStorageUnits().values().stream().allMatch(each -> each.getStorageType().equals(database.getProtocolType()));
    }
    
    private static Collection<ShardingSphereRowData> getRows(final ShardingSphereTable table, final Collection<String> selectedColumnNames, final ResultSet resultSet) throws SQLException {
        Collection<ShardingSphereRowData> result = new LinkedList<>();
        while (resultSet.next()) {
            result.add(new ShardingSphereRowData(getRow(table, selectedColumnNames, resultSet)));
        }
        return result;
    }
    
    private static List<Object> getRow(final ShardingSphereTable table, final Collection<String> selectedColumnNames, final ResultSet resultSet) throws SQLException {
        List<Object> result = new LinkedList<>();
        for (ShardingSphereColumn each : table.getColumnValues()) {
            result.add(selectedColumnNames.contains(each.getName()) ? convertValueIfNecessary(resultSet.getObject(each.getName()), each.getDataType()) : mockValue(each.getDataType()));
        }
        return result;
    }
    
    private static Object convertValueIfNecessary(final Object data, final int dataType) {
        if (null == data) {
            return null;
        }
        switch (dataType) {
            case Types.ARRAY:
                return data.toString();
            case Types.BIGINT:
                return Long.valueOf(data.toString());
            default:
                return data;
        }
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
}
