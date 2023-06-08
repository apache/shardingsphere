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

package org.apache.shardingsphere.sharding.metadata.converter;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Sharding node converter.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ShardingNodeConverter {
    
    private static final String ROOT_NODE = "sharding";
    
    private static final String TABLES_NODE = "tables";
    
    private static final String AUTO_TABLES_NODE = "auto_tables";
    
    private static final String BINDING_TABLES_NODE = "binding_tables";
    
    private static final String BROADCAST_TABLES_NODE = "broadcast_tables";
    
    private static final String DEFAULT_STRATEGIES_NODE = "default_strategies";
    
    private static final String DEFAULT_DATABASE_STRATEGY_NODE = "default_database_strategy";
    
    private static final String DEFAULT_TABLE_STRATEGY_NODE = "default_table_strategy";
    
    private static final String DEFAULT_KEY_GENERATE_STRATEGY_NODE = "default_key_generate_strategy";
    
    private static final String DEFAULT_AUDIT_STRATEGY_NODE = "default_audit_strategy";
    
    private static final String DEFAULT_SHARDING_COLUMN_NODE = "default_sharding_column";
    
    private static final String SHARDING_ALGORITHMS_NODE = "algorithms";
    
    private static final String KEY_GENERATORS_NODE = "key_generators";
    
    private static final String AUDITORS_NODE = "auditors";
    
    private static final String SHARDING_CACHE_NODE = "sharding_cache";
    
    private static final String RULES_NODE_PREFIX = "/([\\w\\-]+)/([\\w\\-]+)/rules/";
    
    private static final String RULE_NAME_PATTERN = "/([\\w\\-]+)?";
    
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
     * Get auto table name path.
     *
     * @param tableName table name
     * @return auto table name path
     */
    public static String getAutoTableNamePath(final String tableName) {
        return String.join("/", AUTO_TABLES_NODE, tableName);
    }
    
    /**
     * Get binding table name path.
     *
     * @param tableName table name
     * @return binding table name path
     */
    public static String getBindingTableNamePath(final String tableName) {
        return String.join("/", BINDING_TABLES_NODE, tableName);
    }
    
    /**
     * Get broadcast tables path.
     *
     * @return broadcast tables path
     */
    public static String getBroadcastTablesPath() {
        return String.join("/", BROADCAST_TABLES_NODE);
    }
    
    /**
     * Get default database strategy path.
     *
     * @return default database strategy path
     */
    public static String getDefaultDatabaseStrategyPath() {
        return String.join("/", DEFAULT_STRATEGIES_NODE, DEFAULT_DATABASE_STRATEGY_NODE);
    }
    
    /**
     * Get default table strategy path.
     *
     * @return default table strategy path
     */
    public static String getDefaultTableStrategyPath() {
        return String.join("/", DEFAULT_STRATEGIES_NODE, DEFAULT_TABLE_STRATEGY_NODE);
    }
    
    /**
     * Get default key generate strategy path.
     *
     * @return default key generate path
     */
    public static String getDefaultKeyGenerateStrategyPath() {
        return String.join("/", DEFAULT_STRATEGIES_NODE, DEFAULT_KEY_GENERATE_STRATEGY_NODE);
    }
    
    /**
     * Get default audit strategy path.
     *
     * @return default audit strategy path
     */
    public static String getDefaultAuditStrategyPath() {
        return String.join("/", DEFAULT_STRATEGIES_NODE, DEFAULT_AUDIT_STRATEGY_NODE);
    }
    
    /**
     * Get default sharding column path.
     *
     * @return default sharding column path
     */
    public static String getDefaultShardingColumnPath() {
        return String.join("/", DEFAULT_STRATEGIES_NODE, DEFAULT_SHARDING_COLUMN_NODE);
    }
    
    /**
     * Get sharding algorithm path.
     *
     * @param shardingAlgorithmName sharding algorithm name
     * @return sharding algorithm path
     */
    public static String getShardingAlgorithmPath(final String shardingAlgorithmName) {
        return String.join("/", SHARDING_ALGORITHMS_NODE, shardingAlgorithmName);
    }
    
    /**
     * Get key generator path.
     *
     * @param keyGeneratorName key generator name
     * @return key generator path
     */
    public static String getKeyGeneratorPath(final String keyGeneratorName) {
        return String.join("/", KEY_GENERATORS_NODE, keyGeneratorName);
    }
    
    /**
     * Get auditor path.
     *
     * @param auditorName auditor name
     * @return auditor path
     */
    public static String getAuditorPath(final String auditorName) {
        return String.join("/", AUDITORS_NODE, auditorName);
    }
    
    /**
     * Get sharding cache path.
     *
     * @return sharding cache path
     */
    public static String getShardingCachePath() {
        return String.join("/", SHARDING_CACHE_NODE);
    }
    
    /**
     * Is sharding path.
     *
     * @param rulePath rule path
     * @return true or false
     */
    public static boolean isShardingPath(final String rulePath) {
        Pattern pattern = Pattern.compile(RULES_NODE_PREFIX + ROOT_NODE + "\\.*", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(rulePath);
        return matcher.find();
    }
    
    /**
     * Is sharding table path.
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
     * Is sharding auto table path.
     *
     * @param rulePath rule path
     * @return true or false
     */
    public static boolean isAutoTablePath(final String rulePath) {
        Pattern pattern = Pattern.compile(RULES_NODE_PREFIX + ROOT_NODE + "/" + AUTO_TABLES_NODE + "\\.*", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(rulePath);
        return matcher.find();
    }
    
    /**
     * Is binding table path.
     *
     * @param rulePath rule path
     * @return true or false
     */
    public static boolean isBindingTablePath(final String rulePath) {
        Pattern pattern = Pattern.compile(RULES_NODE_PREFIX + ROOT_NODE + "/" + BINDING_TABLES_NODE + "\\.*", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(rulePath);
        return matcher.find();
    }
    
    /**
     * Is broadcast table path.
     *
     * @param rulePath rule path
     * @return true or false
     */
    public static boolean isBroadcastTablePath(final String rulePath) {
        Pattern pattern = Pattern.compile(RULES_NODE_PREFIX + ROOT_NODE + "/" + BROADCAST_TABLES_NODE + "\\.*", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(rulePath);
        return matcher.find();
    }
    
    /**
     * Is default database strategy path.
     *
     * @param rulePath rule path
     * @return true or false
     */
    public static boolean isDefaultDatabaseStrategyPath(final String rulePath) {
        Pattern pattern = Pattern.compile(RULES_NODE_PREFIX + ROOT_NODE + "/" + DEFAULT_STRATEGIES_NODE + "/" + DEFAULT_DATABASE_STRATEGY_NODE + "$", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(rulePath);
        return matcher.find();
    }
    
    /**
     * Is default table strategy path.
     *
     * @param rulePath rule path
     * @return true or false
     */
    public static boolean isDefaultTableStrategyPath(final String rulePath) {
        Pattern pattern = Pattern.compile(RULES_NODE_PREFIX + ROOT_NODE + "/" + DEFAULT_STRATEGIES_NODE + "/" + DEFAULT_TABLE_STRATEGY_NODE + "$", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(rulePath);
        return matcher.find();
    }
    
    /**
     * Is default key generate strategy path.
     *
     * @param rulePath rule path
     * @return true or false
     */
    public static boolean isDefaultKeyGenerateStrategyPath(final String rulePath) {
        Pattern pattern = Pattern.compile(RULES_NODE_PREFIX + ROOT_NODE + "/" + DEFAULT_STRATEGIES_NODE + "/" + DEFAULT_KEY_GENERATE_STRATEGY_NODE + "$", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(rulePath);
        return matcher.find();
    }
    
    /**
     * Is default audit strategy path.
     *
     * @param rulePath rule path
     * @return true or false
     */
    public static boolean isDefaultAuditStrategyPath(final String rulePath) {
        Pattern pattern = Pattern.compile(RULES_NODE_PREFIX + ROOT_NODE + "/" + DEFAULT_STRATEGIES_NODE + "/" + DEFAULT_AUDIT_STRATEGY_NODE + "$", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(rulePath);
        return matcher.find();
    }
    
    /**
     * Is default sharding column path.
     *
     * @param rulePath rule path
     * @return true or false
     */
    public static boolean isDefaultShardingColumnPath(final String rulePath) {
        Pattern pattern = Pattern.compile(RULES_NODE_PREFIX + ROOT_NODE + "/" + DEFAULT_STRATEGIES_NODE + "/" + DEFAULT_SHARDING_COLUMN_NODE + "$", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(rulePath);
        return matcher.find();
    }
    
    /**
     * Is sharding algorithm path.
     *
     * @param rulePath rule path
     * @return true or false
     */
    public static boolean isShardingAlgorithmPath(final String rulePath) {
        Pattern pattern = Pattern.compile(RULES_NODE_PREFIX + ROOT_NODE + "/" + SHARDING_ALGORITHMS_NODE + "\\.*", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(rulePath);
        return matcher.find();
    }
    
    /**
     * Is key generator path.
     *
     * @param rulePath rule path
     * @return true or false
     */
    public static boolean isKeyGeneratorPath(final String rulePath) {
        Pattern pattern = Pattern.compile(RULES_NODE_PREFIX + ROOT_NODE + "/" + KEY_GENERATORS_NODE + "\\.*", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(rulePath);
        return matcher.find();
    }
    
    /**
     * Is sharding auditor path.
     *
     * @param rulePath rule path
     * @return true or false
     */
    public static boolean isAuditorPath(final String rulePath) {
        Pattern pattern = Pattern.compile(RULES_NODE_PREFIX + ROOT_NODE + "/" + AUDITORS_NODE + "\\.*", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(rulePath);
        return matcher.find();
    }
    
    /**
     * Is sharding cache path.
     *
     * @param rulePath rule path
     * @return true or false
     */
    public static boolean isShardingCachePath(final String rulePath) {
        Pattern pattern = Pattern.compile(RULES_NODE_PREFIX + ROOT_NODE + "/" + SHARDING_CACHE_NODE + "$", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(rulePath);
        return matcher.find();
    }
    
    /**
     * Get sharding table name.
     *
     * @param rulePath rule path
     * @return sharding table name
     */
    public static Optional<String> getTableName(final String rulePath) {
        Pattern pattern = Pattern.compile(RULES_NODE_PREFIX + ROOT_NODE + "/" + TABLES_NODE + RULE_NAME_PATTERN, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(rulePath);
        return matcher.find() ? Optional.of(matcher.group(3)) : Optional.empty();
    }
    
    /**
     * Get auto table name.
     *
     * @param rulePath rule path
     * @return auto table name
     */
    public static Optional<String> getAutoTableName(final String rulePath) {
        Pattern pattern = Pattern.compile(RULES_NODE_PREFIX + ROOT_NODE + "/" + AUTO_TABLES_NODE + RULE_NAME_PATTERN, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(rulePath);
        return matcher.find() ? Optional.of(matcher.group(3)) : Optional.empty();
    }
    
    /**
     * Get binding table name.
     *
     * @param rulePath rule path
     * @return binding table name
     */
    public static Optional<String> getBindingTableName(final String rulePath) {
        Pattern pattern = Pattern.compile(RULES_NODE_PREFIX + ROOT_NODE + "/" + BINDING_TABLES_NODE + RULE_NAME_PATTERN, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(rulePath);
        return matcher.find() ? Optional.of(matcher.group(3)) : Optional.empty();
    }
    
    /**
     * Get broadcast name.
     *
     * @param rulePath rule path
     * @return broadcast name
     */
    public static Optional<String> getBroadcastTableName(final String rulePath) {
        Pattern pattern = Pattern.compile(RULES_NODE_PREFIX + ROOT_NODE + "/" + BROADCAST_TABLES_NODE + RULE_NAME_PATTERN, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(rulePath);
        return matcher.find() ? Optional.of(matcher.group(3)) : Optional.empty();
    }
    
    /**
     * Get sharding algorithm name.
     *
     * @param rulePath rule path
     * @return sharding algorithm name
     */
    public static Optional<String> getShardingAlgorithmName(final String rulePath) {
        Pattern pattern = Pattern.compile(RULES_NODE_PREFIX + ROOT_NODE + "/" + SHARDING_ALGORITHMS_NODE + RULE_NAME_PATTERN, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(rulePath);
        return matcher.find() ? Optional.of(matcher.group(3)) : Optional.empty();
    }
    
    /**
     * Get key generator name.
     *
     * @param rulePath rule path
     * @return key generator name
     */
    public static Optional<String> getKeyGeneratorName(final String rulePath) {
        Pattern pattern = Pattern.compile(RULES_NODE_PREFIX + ROOT_NODE + "/" + KEY_GENERATORS_NODE + RULE_NAME_PATTERN, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(rulePath);
        return matcher.find() ? Optional.of(matcher.group(3)) : Optional.empty();
    }
    
    /**
     * Get auditor name.
     *
     * @param rulePath rule path
     * @return auditor name
     */
    public static Optional<String> getAuditorName(final String rulePath) {
        Pattern pattern = Pattern.compile(RULES_NODE_PREFIX + ROOT_NODE + "/" + AUDITORS_NODE + RULE_NAME_PATTERN, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(rulePath);
        return matcher.find() ? Optional.of(matcher.group(3)) : Optional.empty();
    }
}
