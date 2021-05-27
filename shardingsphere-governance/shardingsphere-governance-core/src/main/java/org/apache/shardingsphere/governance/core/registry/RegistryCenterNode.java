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

package org.apache.shardingsphere.governance.core.registry;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Collection;
import java.util.Collections;

/**
 * Registry center node.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class RegistryCenterNode {
    
    private static final String EXECUTION_NODES_NAME = "executionnodes";
    
    private static final String USERS_NODE = "users";
    
    private static final String COMMA_SEPARATOR = ",";
    
    private static final String PATH_SEPARATOR = "/";
    
    /**
     * Get users path.
     *
     * @return users path
     */
    public static String getUsersNode() {
        return getFullPath(USERS_NODE);
    }
    
    private static String getFullPath(final String node) {
        return Joiner.on(PATH_SEPARATOR).join("", node);
    }
    
    /**
     * Split schema name.
     *
     * @param schemaNames schema names
     * @return schema names
     */
    public static Collection<String> splitSchemaName(final String schemaNames) {
        return Strings.isNullOrEmpty(schemaNames) ? Collections.emptyList() : Splitter.on(COMMA_SEPARATOR).splitToList(schemaNames);
    }
    
    /**
     * Get execution nodes path.
     *
     * @return execution nodes path
     */
    public static String getExecutionNodesPath() {
        return Joiner.on("/").join("", EXECUTION_NODES_NAME);
    }
    
    /**
     * Get execution path.
     *
     * @param executionId execution id
     * @return execution path
     */
    public static String getExecutionPath(final String executionId) {
        return Joiner.on("/").join("", EXECUTION_NODES_NAME, executionId);
    }
}
