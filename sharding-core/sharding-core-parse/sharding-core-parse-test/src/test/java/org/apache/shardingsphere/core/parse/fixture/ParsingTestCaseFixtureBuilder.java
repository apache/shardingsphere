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

package org.apache.shardingsphere.core.parse.fixture;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.core.metadata.table.ColumnMetaData;
import org.apache.shardingsphere.core.metadata.table.ShardingTableMetaData;
import org.apache.shardingsphere.core.metadata.table.TableMetaData;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ParsingTestCaseFixtureBuilder {
    
    /**
     * Build sharding table meta data.
     * 
     * @return sharding table meta data
     */
    public static ShardingTableMetaData buildShardingTableMetaData() {
        Map<String, TableMetaData> tableMetaDataMap = new HashMap<>(3, 1);
        tableMetaDataMap.put("t_order",
                new TableMetaData(Arrays.asList(new ColumnMetaData("order_id", "int", true), new ColumnMetaData("user_id", "int", false), 
                        new ColumnMetaData("status", "int", false)), Collections.<String>emptySet()));
        tableMetaDataMap.put("t_order_item", new TableMetaData(Arrays.asList(new ColumnMetaData("item_id", "int", true), new ColumnMetaData("order_id", "int", false),
                new ColumnMetaData("user_id", "int", false), new ColumnMetaData("status", "varchar", false), 
                new ColumnMetaData("c_date", "timestamp", false)), Collections.<String>emptySet()));
        tableMetaDataMap.put("t_encrypt", new TableMetaData(Arrays.asList(new ColumnMetaData("id", "int", true), new ColumnMetaData("name", "varchar", false),
                new ColumnMetaData("mobile", "varchar", false), new ColumnMetaData("status", "int", false)), Collections.<String>emptySet()));
        return new ShardingTableMetaData(tableMetaDataMap);
    }
}
