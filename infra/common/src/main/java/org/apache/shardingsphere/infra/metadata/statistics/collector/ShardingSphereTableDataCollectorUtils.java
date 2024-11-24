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
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereColumn;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereTable;

import java.sql.Types;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Table data collector utility class.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ShardingSphereTableDataCollectorUtils {
    
    /**
     * Create row value.
     *
     * @param columnValues column values
     * @param table sharding sphere table
     * @return objects
     */
    public static List<Object> createRowValue(final Map<String, Object> columnValues, final ShardingSphereTable table) {
        List<Object> result = new LinkedList<>();
        for (ShardingSphereColumn each : table.getAllColumns()) {
            result.add(columnValues.getOrDefault(each.getName(), mockValue(each.getDataType())));
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
}
