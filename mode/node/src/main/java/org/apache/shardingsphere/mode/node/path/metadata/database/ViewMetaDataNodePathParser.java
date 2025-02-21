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
import org.apache.shardingsphere.mode.node.path.NewNodePathGenerator;
import org.apache.shardingsphere.mode.node.path.NodePathPattern;
import org.apache.shardingsphere.mode.node.path.version.VersionNodePathParser;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * View meta data path parser.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ViewMetaDataNodePathParser {
    
    private static final Pattern PATTERN = Pattern.compile(
            NewNodePathGenerator.generatePath(new ViewMetadataNodePath(NodePathPattern.IDENTIFIER, NodePathPattern.IDENTIFIER, NodePathPattern.IDENTIFIER), false) + "$", Pattern.CASE_INSENSITIVE);
    
    private static final VersionNodePathParser VERSION_PARSER = new VersionNodePathParser(
            NewNodePathGenerator.generatePath(new ViewMetadataNodePath(NodePathPattern.IDENTIFIER, NodePathPattern.IDENTIFIER, NodePathPattern.IDENTIFIER), false));
    
    /**
     * Get view name.
     *
     * @param path path
     * @return view name
     */
    public static Optional<String> findViewName(final String path) {
        Matcher matcher = PATTERN.matcher(path);
        return matcher.find() ? Optional.of(matcher.group(3)) : Optional.empty();
    }
    
    /**
     * Is view path.
     *
     * @param path path
     * @return is view path or not
     */
    public static boolean isViewPath(final String path) {
        return findViewName(path).isPresent();
    }
    
    /**
     * Get view version pattern node path parser.
     *
     * @return view version node path parser
     */
    public static VersionNodePathParser getVersion() {
        return VERSION_PARSER;
    }
}
