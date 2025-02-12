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
import org.apache.shardingsphere.mode.node.path.NodePathPattern;
import org.apache.shardingsphere.mode.node.path.version.VersionNodePathGenerator;
import org.apache.shardingsphere.mode.node.path.version.VersionNodePathParser;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * View meta data path.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ViewMetaDataNodePath {
    
    private static final String VIEWS_NODE = "views";
    
    private static final VersionNodePathParser PARSER =
            new VersionNodePathParser(getViewPath(NodePathPattern.GROUPED_IDENTIFIER, NodePathPattern.GROUPED_IDENTIFIER, NodePathPattern.GROUPED_IDENTIFIER));
    
    /**
     * Get view root path.
     *
     * @param databaseName database name
     * @param schemaName schema name
     * @return view root path
     */
    public static String getViewRootPath(final String databaseName, final String schemaName) {
        return String.join("/", DatabaseMetaDataNodePath.getSchemaPath(databaseName, schemaName), VIEWS_NODE);
    }
    
    /**
     * Get view path.
     *
     * @param databaseName database name
     * @param schemaName schema name
     * @param viewName view name
     * @return view path
     */
    public static String getViewPath(final String databaseName, final String schemaName, final String viewName) {
        return String.join("/", getViewRootPath(databaseName, schemaName), viewName);
    }
    
    /**
     * Get view version node path generator.
     *
     * @param databaseName database name
     * @param schemaName schema name
     * @param viewName view name
     * @return view version node path generator
     */
    public static VersionNodePathGenerator getVersionNodePathGenerator(final String databaseName, final String schemaName, final String viewName) {
        return new VersionNodePathGenerator(getViewPath(databaseName, schemaName, viewName));
    }
    
    /**
     * Get view name.
     *
     * @param path path
     * @return view name
     */
    public static Optional<String> findViewName(final String path) {
        Pattern pattern = Pattern.compile(getViewPath(NodePathPattern.GROUPED_IDENTIFIER, NodePathPattern.GROUPED_IDENTIFIER, NodePathPattern.GROUPED_IDENTIFIER) + "$", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(path);
        return matcher.find() ? Optional.of(matcher.group(3)) : Optional.empty();
    }
    
    /**
     * Is view path.
     *
     * @param path path
     * @return true or false
     */
    public static boolean isViewPath(final String path) {
        return findViewName(path).isPresent();
    }
    
    /**
     * Get view version pattern node path parser.
     *
     * @return view version node path parser
     */
    public static VersionNodePathParser getVersionNodePathParser() {
        return PARSER;
    }
}
