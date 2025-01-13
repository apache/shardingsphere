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
 * View meta data path.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ViewMetaDataNodePath {
    
    private static final String ROOT_NODE = "/metadata";
    
    private static final String SCHEMAS_NODE = "schemas";
    
    private static final String VIEWS_NODE = "views";
    
    private static final String VERSIONS_NODE = "versions";
    
    private static final String ACTIVE_VERSION_NODE = "active_version";
    
    private static final String VIEWS_PATTERN = "/([\\w\\-]+)/schemas/([\\w\\-]+)/views";
    
    private static final String ACTIVE_VERSION_SUFFIX = "/([\\w\\-]+)/active_version";
    
    private static final String VIEW_SUFFIX = "/([\\w\\-]+)$";
    
    /**
     * Get meta data views path.
     *
     * @param databaseName database name
     * @param schemaName schema name
     * @return views path
     */
    public static String getMetaDataViewsPath(final String databaseName, final String schemaName) {
        return String.join("/", ROOT_NODE, databaseName, SCHEMAS_NODE, schemaName, VIEWS_NODE);
    }
    
    /**
     * Get view active version path.
     *
     * @param databaseName database name
     * @param schemaName schema name
     * @param viewName view name
     * @return view active version path
     */
    public static String getViewActiveVersionPath(final String databaseName, final String schemaName, final String viewName) {
        return String.join("/", ROOT_NODE, databaseName, SCHEMAS_NODE, schemaName, VIEWS_NODE, viewName, ACTIVE_VERSION_NODE);
    }
    
    /**
     * Get view versions path.
     *
     * @param databaseName database name
     * @param schemaName schema name
     * @param viewName view name
     * @return view versions path
     */
    public static String getViewVersionsPath(final String databaseName, final String schemaName, final String viewName) {
        return String.join("/", ROOT_NODE, databaseName, SCHEMAS_NODE, schemaName, VIEWS_NODE, viewName, VERSIONS_NODE);
    }
    
    /**
     * Get view version path.
     *
     * @param databaseName database name
     * @param schemaName schema name
     * @param viewName view name
     * @param version version
     * @return view version path
     */
    public static String getViewVersionPath(final String databaseName, final String schemaName, final String viewName, final String version) {
        return String.join("/", getViewVersionsPath(databaseName, schemaName, viewName), version);
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
        return String.join("/", ROOT_NODE, databaseName, SCHEMAS_NODE, schemaName, VIEWS_NODE, viewName);
    }
    
    /**
     * Get view name by active version path.
     *
     * @param path path
     * @return view name
     */
    public static Optional<String> getViewNameByActiveVersionPath(final String path) {
        Pattern pattern = Pattern.compile(ROOT_NODE + VIEWS_PATTERN + ACTIVE_VERSION_SUFFIX, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(path);
        return matcher.find() ? Optional.of(matcher.group(3)) : Optional.empty();
    }
    
    /**
     * Get view name.
     *
     * @param path path
     * @return view name
     */
    public static Optional<String> findViewName(final String path) {
        Pattern pattern = Pattern.compile(ROOT_NODE + VIEWS_PATTERN + "/([\\w\\-]+)$", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(path);
        return matcher.find() ? Optional.of(matcher.group(3)) : Optional.empty();
    }
    
    /**
     * Is view active version path.
     *
     * @param path path
     * @return true or false
     */
    public static boolean isViewActiveVersionPath(final String path) {
        return Pattern.compile(ROOT_NODE + VIEWS_PATTERN + ACTIVE_VERSION_SUFFIX, Pattern.CASE_INSENSITIVE).matcher(path).find();
    }
    
    /**
     * Is view path.
     *
     * @param path path
     * @return true or false
     */
    public static boolean isViewPath(final String path) {
        return Pattern.compile(ROOT_NODE + VIEWS_PATTERN + VIEW_SUFFIX, Pattern.CASE_INSENSITIVE).matcher(path).find();
    }
}
