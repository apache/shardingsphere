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

package org.apache.shardingsphere.mode.node.path.statistics;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.mode.node.path.NodePathGenerator;
import org.apache.shardingsphere.mode.node.path.NodePathPattern;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Statistics node path parser.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class StatisticsNodePathParser {
    
    private static final String UNIQUE_KEY_PATTERN = "(\\w+)";
    
    /**
     * Find database name.
     *
     * @param path path
     * @param containsChildPath whether contains child path
     * @return found database name
     */
    public static Optional<String> findDatabaseName(final String path, final boolean containsChildPath) {
        String endPattern = containsChildPath ? "?" : "$";
        Pattern pattern = Pattern.compile(NodePathGenerator.generatePath(new StatisticsDataNodePath(NodePathPattern.IDENTIFIER, null, null, null), true) + endPattern, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(path);
        return matcher.find() ? Optional.of(matcher.group(1)) : Optional.empty();
    }
    
    /**
     * Find schema name.
     *
     * @param path path
     * @param containsChildPath whether contains child path
     * @return found schema name
     */
    public static Optional<String> findSchemaName(final String path, final boolean containsChildPath) {
        String endPattern = containsChildPath ? "?" : "$";
        Pattern pattern = Pattern.compile(
                NodePathGenerator.generatePath(new StatisticsDataNodePath(NodePathPattern.IDENTIFIER, NodePathPattern.IDENTIFIER, null, null), true) + endPattern, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(path);
        return matcher.find() ? Optional.of(matcher.group(2)) : Optional.empty();
    }
    
    /**
     * Find table name.
     *
     * @param path path
     * @param containsChildPath whether contains child path
     * @return found table name
     */
    public static Optional<String> findTableName(final String path, final boolean containsChildPath) {
        String endPattern = containsChildPath ? "?" : "$";
        Pattern pattern = Pattern.compile(
                NodePathGenerator.generatePath(new StatisticsDataNodePath(NodePathPattern.IDENTIFIER, NodePathPattern.IDENTIFIER, NodePathPattern.IDENTIFIER, null), false) + endPattern,
                Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(path);
        return matcher.find() ? Optional.of(matcher.group(3)) : Optional.empty();
    }
    
    /**
     * Find row unique key.
     *
     * @param path path
     * @return found row unique key
     */
    public static Optional<String> findRowUniqueKey(final String path) {
        Pattern pattern = Pattern.compile(NodePathGenerator.generatePath(
                new StatisticsDataNodePath(NodePathPattern.IDENTIFIER, NodePathPattern.IDENTIFIER, NodePathPattern.IDENTIFIER, UNIQUE_KEY_PATTERN), false) + "$", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(path);
        return matcher.find() ? Optional.of(matcher.group(4)) : Optional.empty();
    }
}
