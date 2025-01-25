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

package org.apache.shardingsphere.mode.node.path.state;

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
    
    private static final String DATABASE_LISTENER_COORDINATOR_NODE = "database_listener_coordinator";
    
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
     * Get database listener coordinator node root path.
     *
     * @return database listener coordinator node root path
     */
    public static String getDatabaseListenerCoordinatorNodeRootPath() {
        return String.join("/", ROOT_NODE, DATABASE_LISTENER_COORDINATOR_NODE);
    }
    
    /**
     * Get database listener coordinator node path.
     *
     * @param databaseName database name
     * @return database listener coordinator node path
     */
    public static String getDatabaseListenerCoordinatorNodePath(final String databaseName) {
        return String.join("/", getDatabaseListenerCoordinatorNodeRootPath(), databaseName);
    }
    
    /**
     * Find database name by database listener coordinator node path.
     *
     * @param databaseListenerCoordinatorNodePath database listener coordinator node path
     * @return found database name
     */
    public static Optional<String> findDatabaseName(final String databaseListenerCoordinatorNodePath) {
        Pattern pattern = Pattern.compile(getDatabaseListenerCoordinatorNodePath(DATABASE_PATTERN) + "$", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(databaseListenerCoordinatorNodePath);
        return matcher.find() ? Optional.of(matcher.group(1)) : Optional.empty();
    }
}
