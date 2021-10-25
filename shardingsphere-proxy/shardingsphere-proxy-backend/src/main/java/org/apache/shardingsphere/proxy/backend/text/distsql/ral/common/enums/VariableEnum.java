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

package org.apache.shardingsphere.proxy.backend.text.distsql.ral.common.enums;

import org.apache.shardingsphere.proxy.backend.text.distsql.ral.common.exception.UnsupportedVariableException;

import java.util.Arrays;
import java.util.Collection;

/**
 * Variable enum.
 */
public enum VariableEnum {
    
    /**
     * Props variables.
     */
    MAX_CONNECTIONS_SIZE_PER_QUERY,
    
    KERNEL_EXECUTOR_SIZE,
    
    PROXY_FRONTEND_FLUSH_THRESHOLD,
    
    PROXY_OPENTRACING_ENABLED,
    
    PROXY_HINT_ENABLED,
    
    SQL_SHOW,
    
    CHECK_TABLE_METADATA_ENABLED,
    
    LOCK_WAIT_TIMEOUT_MILLISECONDS,
    
    SHOW_PROCESS_LIST_ENABLED,
    
    PROXY_BACKEND_QUERY_FETCH_SIZE,

    CHECK_DUPLICATE_TABLE_ENABLED,

    SQL_COMMENT_PARSE_ENABLED,

    PROXY_FRONTEND_EXECUTOR_SIZE,

    PROXY_BACKEND_EXECUTOR_SUITABLE,
    
    PROXY_FRONTEND_CONNECTION_LIMIT,
    
    /**
     * Other variables.
     */
    AGENT_PLUGINS_ENABLED, 
    
    CACHED_CONNECTIONS, 
    
    TRANSACTION_TYPE;
    
    /**
     * Returns the variable constant of the specified variable name.
     * @param variableName variable name
     * @return variable constant
     */
    public static VariableEnum getValueOf(final String variableName) {
        try {
            return valueOf(variableName.toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new UnsupportedVariableException(variableName);
        }
    }
    
    /**
     * Get variables classified as props.
     * @return collection of variable enum
     */
    public static Collection<VariableEnum> getPropsVariables() {
        return Arrays.asList(
                VariableEnum.MAX_CONNECTIONS_SIZE_PER_QUERY,
                VariableEnum.KERNEL_EXECUTOR_SIZE,
                VariableEnum.PROXY_FRONTEND_FLUSH_THRESHOLD,
                VariableEnum.PROXY_OPENTRACING_ENABLED,
                VariableEnum.PROXY_HINT_ENABLED,
                VariableEnum.SQL_SHOW,
                VariableEnum.CHECK_TABLE_METADATA_ENABLED,
                VariableEnum.LOCK_WAIT_TIMEOUT_MILLISECONDS,
                VariableEnum.SHOW_PROCESS_LIST_ENABLED,
                VariableEnum.PROXY_BACKEND_QUERY_FETCH_SIZE,
                VariableEnum.CHECK_DUPLICATE_TABLE_ENABLED,
                VariableEnum.SQL_COMMENT_PARSE_ENABLED,
                VariableEnum.PROXY_FRONTEND_EXECUTOR_SIZE,
                VariableEnum.PROXY_BACKEND_EXECUTOR_SUITABLE,
                VariableEnum.PROXY_FRONTEND_CONNECTION_LIMIT);
    }  
}
