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

package org.apache.shardingsphere.mcp.feature.encrypt.tool.model;

import lombok.Getter;
import lombok.Setter;
import org.apache.shardingsphere.mcp.tool.model.workflow.WorkflowRequest;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Encrypt workflow request.
 */
@Getter
@Setter
public final class EncryptWorkflowRequest extends WorkflowRequest {
    
    private Boolean allowIndexDDL;
    
    private Boolean requiresDecrypt;
    
    private Boolean requiresEqualityFilter;
    
    private Boolean requiresLikeQuery;
    
    private String assistedQueryAlgorithmType;
    
    private String likeQueryAlgorithmType;
    
    private String cipherColumnName;
    
    private String assistedQueryColumnName;
    
    private String likeQueryColumnName;
    
    private final Map<String, String> assistedQueryAlgorithmProperties = new LinkedHashMap<>(8, 1F);
    
    private final Map<String, String> likeQueryAlgorithmProperties = new LinkedHashMap<>(8, 1F);
    
    @Override
    public Map<String, String> getAlgorithmProperties(final String algorithmRole) {
        if ("assisted_query".equals(algorithmRole)) {
            return assistedQueryAlgorithmProperties;
        }
        if ("like_query".equals(algorithmRole)) {
            return likeQueryAlgorithmProperties;
        }
        return super.getAlgorithmProperties(algorithmRole);
    }
}
