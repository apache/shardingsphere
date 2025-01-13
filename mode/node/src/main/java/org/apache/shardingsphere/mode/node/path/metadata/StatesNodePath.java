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

package org.apache.shardingsphere.mode.node.path.metadata;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * States node path.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class StatesNodePath {
    
    private static final String ROOT_NODE = "/states";
    
    private static final String CLUSTER_STATE_NODE = "cluster_state";
    
    private static final String LISTENER_ASSISTED_NODE = "listener_assisted";
    
    private static final String DATABASE_PATTERN = "(\\w+)";
    
    /**
     * Get cluster state path.
     *
     * @return cluster state path
     */
    public static String getClusterStatePath() {
        return String.join("/", ROOT_NODE, CLUSTER_STATE_NODE);
    }
    
    /**
     * Get listener assisted node root path.
     *
     * @return listener assisted node root path
     */
    public static String getListenerAssistedNodeRootPath() {
        return String.join("/", ROOT_NODE, LISTENER_ASSISTED_NODE);
    }
    
    /**
     * Get database name listener assisted node path.
     *
     * @param databaseName database name
     * @return database name listener assisted node path
     */
    public static String getListenerAssistedNodePath(final String databaseName) {
        return String.join("/", getListenerAssistedNodeRootPath(), databaseName);
    }
    
    /**
     * Find database name by listener assisted node path.
     *
     * @param listenerAssistedNodePath listener assisted node path
     * @return found database name
     */
    public static Optional<String> findDatabaseName(final String listenerAssistedNodePath) {
        Pattern pattern = Pattern.compile(getListenerAssistedNodePath(DATABASE_PATTERN) + "$", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(listenerAssistedNodePath);
        return matcher.find() ? Optional.of(matcher.group(1)) : Optional.empty();
    }
}
