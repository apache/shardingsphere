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
import org.apache.shardingsphere.metadata.persist.node.metadata.datasource.DataSourceNodeConverter;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * TODO Rename DatabaseMetaDataNode when metadata structure adjustment completed. #25485
 * New database meta data node.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class NewDatabaseMetaDataNode {
    
    private static final String ROOT_NODE = "metadata";
    
    private static final String RULE_NODE = "rules";
    
    private static final String ACTIVE_VERSION = "active_version";
    
    private static final String VERSIONS = "versions";
    
    /**
     * Get data Sources node.
     *
     * @param databaseName database name
     * @return data sources node
     */
    public static String getDataSourcesNode(final String databaseName) {
        return String.join("/", getMetaDataNodeNode(), DataSourceNodeConverter.getDataSourcesPath(databaseName));
    }
    
    /**
     * Get data Source node.
     *
     * @param databaseName database name
     * @param dataSourceName data source name
     * @param version version
     * @return data source node
     */
    public static String getDataSourceNode(final String databaseName, final String dataSourceName, final String version) {
        return String.join("/", getMetaDataNodeNode(), DataSourceNodeConverter.getDataSourcePath(databaseName, dataSourceName, version));
    }
    
    /**
     * Get data Source active version node.
     *
     * @param databaseName database name
     * @param dataSourceName data source name
     * @return data source active version node
     */
    public static String getDataSourceActiveVersionNode(final String databaseName, final String dataSourceName) {
        return String.join("/", getMetaDataNodeNode(), DataSourceNodeConverter.getActiveVersionPath(databaseName, dataSourceName));
    }
    
    /**
     * Get database rule active version node.
     *
     * @param databaseName database name
     * @param ruleName rule name
     * @param key key
     * @return database rule active version node
     */
    public static String getDatabaseRuleActiveVersionNode(final String databaseName, final String ruleName, final String key) {
        return String.join("/", getDatabaseRuleNode(databaseName, ruleName), key, ACTIVE_VERSION);
    }
    
    /**
     * Get database rule versions node.
     *
     * @param databaseName database name
     * @param ruleName rule name
     * @param key key
     * @return database rule versions node
     */
    public static String getDatabaseRuleVersionsNode(final String databaseName, final String ruleName, final String key) {
        return String.join("/", getDatabaseRuleNode(databaseName, ruleName), key, VERSIONS);
    }
    
    /**
     * Get database rule version node.
     *
     * @param databaseName database name
     * @param ruleName rule name
     * @param key key
     * @param version version
     * @return database rule next version
     */
    public static String getDatabaseRuleVersionNode(final String databaseName, final String ruleName, final String key, final String version) {
        return String.join("/", getDatabaseRuleNode(databaseName, ruleName), key, VERSIONS, version);
    }
    
    private static String getDatabaseRuleNode(final String databaseName, final String ruleName) {
        return String.join("/", getRulesNode(databaseName), ruleName);
    }
    
    /**
     * Get database rules node.
     *
     * @param databaseName database name
     * @return database rules node
     */
    public static String getRulesNode(final String databaseName) {
        return String.join("/", getMetaDataNodeNode(), databaseName, RULE_NODE);
    }
    
    /**
     * Get database name by node.
     *
     * @param path config path
     * @return database name
     */
    public static Optional<String> getDatabaseNameByNode(final String path) {
        Pattern pattern = Pattern.compile(getMetaDataNodeNode() + "/([\\w\\-]+)?", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(path);
        return matcher.find() ? Optional.of(matcher.group(1)) : Optional.empty();
    }
    
    private static String getMetaDataNodeNode() {
        return String.join("/", "", ROOT_NODE);
    }
}
