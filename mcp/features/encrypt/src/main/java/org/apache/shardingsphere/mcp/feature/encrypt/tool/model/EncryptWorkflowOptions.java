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

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Shared encrypt workflow options.
 */
@lombok.Getter
@lombok.Setter
public final class EncryptWorkflowOptions {
    
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
    
    Map<String, String> getAlgorithmProperties(final String algorithmRole) {
        if ("assisted_query".equals(algorithmRole)) {
            return assistedQueryAlgorithmProperties;
        }
        if ("like_query".equals(algorithmRole)) {
            return likeQueryAlgorithmProperties;
        }
        return Map.of();
    }
    
    EncryptWorkflowOptions copy() {
        EncryptWorkflowOptions result = new EncryptWorkflowOptions();
        copyTo(result);
        return result;
    }
    
    void copyTo(final EncryptWorkflowOptions target) {
        target.setAllowIndexDDL(allowIndexDDL);
        target.setRequiresDecrypt(requiresDecrypt);
        target.setRequiresEqualityFilter(requiresEqualityFilter);
        target.setRequiresLikeQuery(requiresLikeQuery);
        target.setAssistedQueryAlgorithmType(assistedQueryAlgorithmType);
        target.setLikeQueryAlgorithmType(likeQueryAlgorithmType);
        target.setCipherColumnName(cipherColumnName);
        target.setAssistedQueryColumnName(assistedQueryColumnName);
        target.setLikeQueryColumnName(likeQueryColumnName);
        target.getAssistedQueryAlgorithmProperties().clear();
        target.getAssistedQueryAlgorithmProperties().putAll(assistedQueryAlgorithmProperties);
        target.getLikeQueryAlgorithmProperties().clear();
        target.getLikeQueryAlgorithmProperties().putAll(likeQueryAlgorithmProperties);
    }
    
    void overlayTo(final EncryptWorkflowOptions target) {
        if (null != allowIndexDDL) {
            target.setAllowIndexDDL(allowIndexDDL);
        }
        if (null != requiresDecrypt) {
            target.setRequiresDecrypt(requiresDecrypt);
        }
        if (null != requiresEqualityFilter) {
            target.setRequiresEqualityFilter(requiresEqualityFilter);
        }
        if (null != requiresLikeQuery) {
            target.setRequiresLikeQuery(requiresLikeQuery);
        }
        if (hasText(assistedQueryAlgorithmType)) {
            target.setAssistedQueryAlgorithmType(assistedQueryAlgorithmType);
        }
        if (hasText(likeQueryAlgorithmType)) {
            target.setLikeQueryAlgorithmType(likeQueryAlgorithmType);
        }
        if (hasText(cipherColumnName)) {
            target.setCipherColumnName(cipherColumnName);
        }
        if (hasText(assistedQueryColumnName)) {
            target.setAssistedQueryColumnName(assistedQueryColumnName);
        }
        if (hasText(likeQueryColumnName)) {
            target.setLikeQueryColumnName(likeQueryColumnName);
        }
        target.getAssistedQueryAlgorithmProperties().putAll(assistedQueryAlgorithmProperties);
        target.getLikeQueryAlgorithmProperties().putAll(likeQueryAlgorithmProperties);
    }
    
    private static boolean hasText(final String value) {
        return null != value && !value.trim().isEmpty();
    }
}
