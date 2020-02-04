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
 * Merged result for show index.
 * 
 * @author chenqingyang
 */
public final class ShowIndexMergedResult extends LogicTablesMergedResult {
    
    private static final Map<String, Integer> LABEL_AND_INDEX_MAP = new HashMap<>(13, 1);
    
    static {
        LABEL_AND_INDEX_MAP.put("Table", 1);
        LABEL_AND_INDEX_MAP.put("Non_unique", 2);
        LABEL_AND_INDEX_MAP.put("Key_name", 3);
        LABEL_AND_INDEX_MAP.put("Seq_in_index", 4);
        LABEL_AND_INDEX_MAP.put("Column_name", 5);
        LABEL_AND_INDEX_MAP.put("Collation", 6);
        LABEL_AND_INDEX_MAP.put("Cardinality", 7);
        LABEL_AND_INDEX_MAP.put("Sub_part", 8);
        LABEL_AND_INDEX_MAP.put("Packed", 9);
        LABEL_AND_INDEX_MAP.put("Null", 10);
        LABEL_AND_INDEX_MAP.put("Index_type", 11);
        LABEL_AND_INDEX_MAP.put("Comment", 12);
        LABEL_AND_INDEX_MAP.put("Index_comment", 13);
    }
    
    public ShowIndexMergedResult(final ShardingRule shardingRule, final List<QueryResult> queryResults, final TableMetas tableMetas) throws SQLException {
        super(LABEL_AND_INDEX_MAP, shardingRule, queryResults, tableMetas);
    }
}
