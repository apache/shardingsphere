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

package org.apache.shardingsphere.mode.node.path.metadata.database;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.mode.node.path.NodePathPattern;
import org.apache.shardingsphere.mode.node.path.version.VersionNodePathParser;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Table node path parser.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class TableNodePathParser {
    
    private static final Pattern PATTERN = Pattern.compile(
            new TableNodePath(NodePathPattern.IDENTIFIER, NodePathPattern.IDENTIFIER).getPath(NodePathPattern.IDENTIFIER) + "$", Pattern.CASE_INSENSITIVE);
    
    private static final VersionNodePathParser VERSION_PARSER = new VersionNodePathParser(
            new TableNodePath(NodePathPattern.IDENTIFIER, NodePathPattern.IDENTIFIER).getPath(NodePathPattern.IDENTIFIER));
    
    /**
     * Find table name.
     *
     * @param path path
     * @return found table name
     */
    public static Optional<String> findTableName(final String path) {
        Matcher matcher = PATTERN.matcher(path);
        return matcher.find() ? Optional.of(matcher.group(3)) : Optional.empty();
    }
    
    /**
     * Is table path.
     *
     * @param path path
     * @return is table path or not
     */
    public static boolean isTablePath(final String path) {
        return findTableName(path).isPresent();
    }
    
    /**
     * Get table version pattern node path parser.
     *
     * @return table version node path parser
     */
    public static VersionNodePathParser getVersion() {
        return VERSION_PARSER;
    }
}
