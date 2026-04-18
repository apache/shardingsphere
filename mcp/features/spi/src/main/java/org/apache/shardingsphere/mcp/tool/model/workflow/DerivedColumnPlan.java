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
import lombok.Setter;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Derived column plan.
 */
@Getter
@Setter
public final class DerivedColumnPlan {
    
    private String logicalColumn;
    
    private String cipherColumnName;
    
    private String assistedQueryColumnName;
    
    private String likeQueryColumnName;
    
    private boolean cipherColumnRequired;
    
    private boolean assistedQueryColumnRequired;
    
    private boolean likeQueryColumnRequired;
    
    private String dataTypeStrategy = "shardingsphere-default";
    
    private final List<Map<String, String>> nameCollisions = new LinkedList<>();
    
    /**
     * Convert to map.
     *
     * @return map representation
     */
    public Map<String, Object> toMap() {
        Map<String, Object> result = new LinkedHashMap<>(12, 1F);
        result.put("logical_column", logicalColumn);
        result.put("cipher_column_name", cipherColumnName);
        result.put("assisted_query_column_name", assistedQueryColumnName);
        result.put("like_query_column_name", likeQueryColumnName);
        result.put("cipher_column_required", cipherColumnRequired);
        result.put("assisted_query_column_required", assistedQueryColumnRequired);
        result.put("like_query_column_required", likeQueryColumnRequired);
        result.put("data_type_strategy", dataTypeStrategy);
        result.put("name_collisions", nameCollisions);
        return result;
    }
}
