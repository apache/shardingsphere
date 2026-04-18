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
 * Workflow request.
 */
@Getter
@Setter
public final class WorkflowRequest {
    
    private String planId;
    
    private String database;
    
    private String schema;
    
    private String table;
    
    private String column;
    
    private String featureType;
    
    private String operationType;
    
    private String naturalLanguageIntent;
    
    private String fieldSemantics;
    
    private String deliveryMode = "all-at-once";
    
    private String executionMode = "review-then-execute";
    
    private Boolean allowSampleData;
    
    private Boolean allowIndexDDL = true;
    
    private Boolean requiresDecrypt;
    
    private Boolean requiresEqualityFilter;
    
    private Boolean requiresLikeQuery;
    
    private String algorithmType;
    
    private String assistedQueryAlgorithmType;
    
    private String likeQueryAlgorithmType;
    
    private String cipherColumnName;
    
    private String assistedQueryColumnName;
    
    private String likeQueryColumnName;
    
    private final Map<String, String> primaryAlgorithmProperties = new LinkedHashMap<>(8, 1F);
    
    private final Map<String, String> assistedQueryAlgorithmProperties = new LinkedHashMap<>(8, 1F);
    
    private final Map<String, String> likeQueryAlgorithmProperties = new LinkedHashMap<>(8, 1F);
    
    private final List<String> approvedSteps = new LinkedList<>();
    
    /**
     * Get raw user request alias.
     *
     * @return natural-language request
     */
    public String getRawUserRequest() {
        return naturalLanguageIntent;
    }
    
    /**
     * Set raw user request alias.
     *
     * @param rawUserRequest natural-language request
     */
    public void setRawUserRequest(final String rawUserRequest) {
        naturalLanguageIntent = rawUserRequest;
    }
}
