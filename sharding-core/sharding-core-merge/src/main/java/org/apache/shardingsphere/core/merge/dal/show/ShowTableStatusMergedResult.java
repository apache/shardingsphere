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

package org.apache.shardingsphere.core.merge.dal.show;

import org.apache.shardingsphere.core.execute.sql.execute.result.QueryResult;
import org.apache.shardingsphere.core.metadata.table.TableMetas;
import org.apache.shardingsphere.core.rule.ShardingRule;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Merged result for show table status.
 *
 * @author zhangliang
 */
public final class ShowTableStatusMergedResult extends LogicTablesMergedResult {
    
    private static final Map<String, Integer> LABEL_AND_INDEX_MAP = new HashMap<>(17, 1);
    
    static {
        LABEL_AND_INDEX_MAP.put("Name", 1);
        LABEL_AND_INDEX_MAP.put("Engine", 2);
        LABEL_AND_INDEX_MAP.put("Version", 3);
        LABEL_AND_INDEX_MAP.put("Row_format", 4);
        LABEL_AND_INDEX_MAP.put("Rows", 5);
        LABEL_AND_INDEX_MAP.put("Avg_row_length", 6);
        LABEL_AND_INDEX_MAP.put("Data_length", 7);
        LABEL_AND_INDEX_MAP.put("Max_data_length", 8);
        LABEL_AND_INDEX_MAP.put("Data_free", 9);
        LABEL_AND_INDEX_MAP.put("Auto_increment", 10);
        LABEL_AND_INDEX_MAP.put("Create_time", 11);
        LABEL_AND_INDEX_MAP.put("Update_time", 12);
        LABEL_AND_INDEX_MAP.put("Check_time", 13);
        LABEL_AND_INDEX_MAP.put("Collation", 14);
        LABEL_AND_INDEX_MAP.put("Checksum", 15);
        LABEL_AND_INDEX_MAP.put("Create_options", 16);
        LABEL_AND_INDEX_MAP.put("Comment", 17);
    }
    
    public ShowTableStatusMergedResult(final ShardingRule shardingRule, final List<QueryResult> queryResults, final TableMetas tableMetas) throws SQLException {
        super(LABEL_AND_INDEX_MAP, shardingRule, queryResults, tableMetas);
    }
}
