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

package org.apache.shardingsphere.metadata.persist.node;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * States node.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class StatesNode {
    
    private static final String ROOT_NODE = "states";
    
    private static final String CLUSTER_STATE_NODE = "cluster_state";
    
    private static final String LISTENER_ASSISTED_NODE = "listener_assisted";
    
    /**
     * Get cluster state node path.
     *
     * @return cluster state node path
     */
    public static String getClusterStateNodePath() {
        return String.join("/", "", ROOT_NODE, CLUSTER_STATE_NODE);
    }
    
    /**
     * Get listener assisted node path.
     *
     * @return listener assisted node path
     */
    public static String getListenerAssistedNodePath() {
        return String.join("/", "", ROOT_NODE, LISTENER_ASSISTED_NODE);
    }
    
    /**
     * Get database name by listener assisted node path.
     *
     * @param nodePath node path
     * @return database name
     */
    public static Optional<String> getDatabaseNameByListenerAssistedNodePath(final String nodePath) {
        Pattern pattern = Pattern.compile(getListenerAssistedNodePath() + "/(\\w+)$", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(nodePath);
        return matcher.find() ? Optional.of(matcher.group(1)) : Optional.empty();
    }
    
    /**
     * Get database name listener assisted node path.
     *
     * @param databaseName database name
     * @return database name listener assisted node path
     */
    public static String getDatabaseNameListenerAssistedNodePath(final String databaseName) {
        return String.join("/", "", ROOT_NODE, LISTENER_ASSISTED_NODE, databaseName);
    }
}
