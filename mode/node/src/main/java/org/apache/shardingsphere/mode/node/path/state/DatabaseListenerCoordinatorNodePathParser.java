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
import org.apache.shardingsphere.mode.node.path.NewNodePathGenerator;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Database listener coordinator node path parser.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DatabaseListenerCoordinatorNodePathParser {
    
    private static final String DATABASE_PATTERN = "(\\w+)";
    
    /**
     * Find database name by database listener coordinator node path.
     *
     * @param databaseListenerCoordinatorNodePath database listener coordinator node path
     * @return found database name
     */
    public static Optional<String> findDatabaseName(final String databaseListenerCoordinatorNodePath) {
        Pattern pattern = Pattern.compile(NewNodePathGenerator.generatePath(new DatabaseListenerCoordinatorNodePath(DATABASE_PATTERN), false) + "$", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(databaseListenerCoordinatorNodePath);
        return matcher.find() ? Optional.of(matcher.group(1)) : Optional.empty();
    }
}
