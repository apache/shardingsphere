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

package org.apache.shardingsphere.mcp.tool.model.workflow;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Index plan.
 */
@RequiredArgsConstructor
@Getter
public final class IndexPlan {
    
    private final String indexName;
    
    private final String columnName;
    
    private final String reason;
    
    private final String sql;
    
    /**
     * Convert to map.
     *
     * @return map representation
     */
    public Map<String, Object> toMap() {
        Map<String, Object> result = new LinkedHashMap<>(4, 1F);
        result.put("index_name", indexName);
        result.put("column_name", columnName);
        result.put("reason", reason);
        result.put("sql", sql);
        return result;
    }
}
