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

package org.apache.shardingsphere.shadow.metadata.converter;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Shadow node converter.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ShadowNodeConverter {
    
    private static final String ROOT_NODE = "shadow";
    
    private static final String DATA_SOURCES_NODE = "data_sources";
    
    private static final String TABLES_NODE = "tables";
    
    private static final String ALGORITHMS_NODE = "algorithms";
    
    private static final String DEFAULT_ALGORITHM_NAME = "default_algorithm_name";
    
    private static final String RULES_NODE_PREFIX = "/([\\w\\-]+)/([\\w\\-]+)/rules/";
    
    private static final String RULE_NAME_PATTERN = "/([\\w\\-]+)?";
    
    /**
     * Get data source path.
     *
     * @param dataSourceName data source name
     * @return data source path
     */
    public static String getDataSourcePath(final String dataSourceName) {
        return String.join("/", DATA_SOURCES_NODE, dataSourceName);
    }
    
    /**
     * Get table name path.
     *
     * @param tableName table name
     * @return table name path
     */
    public static String getTableNamePath(final String tableName) {
        return String.join("/", TABLES_NODE, tableName);
    }
    
    /**
     * Get shadow algorithm path.
     *
     * @param shadowAlgorithmName shadow algorithm name
     * @return shadow algorithm path
     */
    public static String getShadowAlgorithmPath(final String shadowAlgorithmName) {
        return String.join("/", ALGORITHMS_NODE, shadowAlgorithmName);
    }
    
    /**
     * Get default shadow algorithm path.
     *
     * @return default shadow algorithm path
     */
    public static String getDefaultShadowAlgorithmPath() {
        return String.join("/", DEFAULT_ALGORITHM_NAME);
    }
    
    /**
     * Is shadow path.
     *
     * @param rulePath rule path
     * @return true or false
     */
    public static boolean isShadowPath(final String rulePath) {
        Pattern pattern = Pattern.compile(RULES_NODE_PREFIX + ROOT_NODE + "\\.*", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(rulePath);
        return matcher.find();
    }
    
    /**
     * Is shadow data sources path.
     *
     * @param rulePath rule path
     * @return true or false
     */
    public static boolean isDataSourcePath(final String rulePath) {
        Pattern pattern = Pattern.compile(RULES_NODE_PREFIX + ROOT_NODE + "/" + DATA_SOURCES_NODE + "\\.*", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(rulePath);
        return matcher.find();
    }
    
    /**
     * Is shadow table path.
     *
     * @param rulePath rule path
     * @return true or false
     */
    public static boolean isTablePath(final String rulePath) {
        Pattern pattern = Pattern.compile(RULES_NODE_PREFIX + ROOT_NODE + "/" + TABLES_NODE + "\\.*", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(rulePath);
        return matcher.find();
    }
    
    /**
     * Is shadow algorithm path.
     *
     * @param rulePath rule path
     * @return true or false
     */
    public static boolean isAlgorithmPath(final String rulePath) {
        Pattern pattern = Pattern.compile(RULES_NODE_PREFIX + ROOT_NODE + "/" + ALGORITHMS_NODE + "\\.*", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(rulePath);
        return matcher.find();
    }
    
    /**
     * Is default algorithm name path.
     *
     * @param rulePath rule path
     * @return true or false
     */
    public static boolean isDefaultAlgorithmNamePath(final String rulePath) {
        Pattern pattern = Pattern.compile(RULES_NODE_PREFIX + ROOT_NODE + "/" + DEFAULT_ALGORITHM_NAME + "$", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(rulePath);
        return matcher.find();
    }
    
    /**
     * Get data source name.
     *
     * @param rulePath rule path
     * @return data source name
     */
    public static Optional<String> getDataSourceName(final String rulePath) {
        Pattern pattern = Pattern.compile(RULES_NODE_PREFIX + ROOT_NODE + "/" + DATA_SOURCES_NODE + RULE_NAME_PATTERN, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(rulePath);
        return matcher.find() ? Optional.of(matcher.group(3)) : Optional.empty();
    }
    
    /**
     * Get table name.
     *
     * @param rulePath rule path
     * @return table name
     */
    public static Optional<String> getTableName(final String rulePath) {
        Pattern pattern = Pattern.compile(RULES_NODE_PREFIX + ROOT_NODE + "/" + TABLES_NODE + RULE_NAME_PATTERN, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(rulePath);
        return matcher.find() ? Optional.of(matcher.group(3)) : Optional.empty();
    }
    
    /**
     * Get algorithm name.
     *
     * @param rulePath rule path
     * @return algorithm name
     */
    public static Optional<String> getAlgorithmName(final String rulePath) {
        Pattern pattern = Pattern.compile(RULES_NODE_PREFIX + ROOT_NODE + "/" + ALGORITHMS_NODE + RULE_NAME_PATTERN, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(rulePath);
        return matcher.find() ? Optional.of(matcher.group(3)) : Optional.empty();
    }
}
