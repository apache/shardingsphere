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
import org.apache.shardingsphere.mode.node.path.NodePathGenerator;
import org.apache.shardingsphere.mode.node.path.NodePathPattern;
import org.apache.shardingsphere.mode.node.path.metadata.database.TableMetadataNodePath;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Database node path parser.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DatabaseNodePathParser {
    
    private static final Pattern DATABASE_PATTERN = Pattern.compile(
            NodePathGenerator.toPath(new TableMetadataNodePath(NodePathPattern.IDENTIFIER, null, null), true) + "?", Pattern.CASE_INSENSITIVE);
    
    /**
     * Find database name.
     *
     * @param path path
     * @return found database name
     */
    public static Optional<String> findDatabaseName(final String path) {
        Matcher matcher = DATABASE_PATTERN.matcher(path);
        return matcher.find() ? Optional.of(matcher.group(1)) : Optional.empty();
    }
}
