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
import org.apache.shardingsphere.infra.metadata.converter.RuleItemNodeConverter;
import org.apache.shardingsphere.infra.metadata.converter.RuleRootNodeConverter;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Sharding node converter.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ShardingNodeConverter {
    
    private static final String DEFAULT_STRATEGIES_NODE = "default_strategies";
    
    private static final String DEFAULT_DATABASE_STRATEGY_NODE = "default_database_strategy";
    
    private static final String DEFAULT_TABLE_STRATEGY_NODE = "default_table_strategy";
    
    private static final String DEFAULT_KEY_GENERATE_STRATEGY_NODE = "default_key_generate_strategy";
    
    private static final String DEFAULT_AUDIT_STRATEGY_NODE = "default_audit_strategy";
    
    private static final String DEFAULT_SHARDING_COLUMN_NODE = "default_sharding_column";
    
    private static final String SHARDING_CACHE_NODE = "sharding_cache";
    
    private static final String VERSIONS = "/versions/\\d+$";
    
    private static final String ACTIVE_VERSION = "/active_version$";
    
    private static final RuleRootNodeConverter ROOT_NODE_CONVERTER = new RuleRootNodeConverter("sharding");
    
    private static final RuleItemNodeConverter TABLE_NODE_CONVERTER = new RuleItemNodeConverter(ROOT_NODE_CONVERTER, "tables");
    
    private static final RuleItemNodeConverter AUTO_TABLE_NODE_CONVERTER = new RuleItemNodeConverter(ROOT_NODE_CONVERTER, "auto_tables");
    
    private static final RuleItemNodeConverter BINDING_TABLE_NODE_CONVERTER = new RuleItemNodeConverter(ROOT_NODE_CONVERTER, "binding_tables");
    
    private static final RuleItemNodeConverter ALGORITHM_NODE_CONVERTER = new RuleItemNodeConverter(ROOT_NODE_CONVERTER, "algorithms");
    
    private static final RuleItemNodeConverter KEY_GENERATOR_NODE_CONVERTER = new RuleItemNodeConverter(ROOT_NODE_CONVERTER, "key_generators");
    
    private static final RuleItemNodeConverter AUDITOR_NODE_CONVERTER = new RuleItemNodeConverter(ROOT_NODE_CONVERTER, "auditors");
    
    /**
     * Get rule root node converter.
     *
     * @return rule root node converter
     */
    public static RuleRootNodeConverter getRuleRootNodeConverter() {
        return ROOT_NODE_CONVERTER;
    }
    
    /**
     * Get table node converter.
     *
     * @return table node converter
     */
    public static RuleItemNodeConverter getTableNodeConverter() {
        return TABLE_NODE_CONVERTER;
    }
    
    /**
     * Get auto table node converter.
     *
     * @return auto table node converter
     */
    public static RuleItemNodeConverter getAutoTableNodeConverter() {
        return AUTO_TABLE_NODE_CONVERTER;
    }
    
    /**
     * Get binding table node converter.
     *
     * @return binding table node converter
     */
    public static RuleItemNodeConverter getBindingTableNodeConverter() {
        return BINDING_TABLE_NODE_CONVERTER;
    }
    
    /**
     * Get algorithm node converter.
     *
     * @return algorithm node converter
     */
    public static RuleItemNodeConverter getAlgorithmNodeConverter() {
        return ALGORITHM_NODE_CONVERTER;
    }
    
    /**
     * Get key generator node converter.
     *
     * @return key generator node converter
     */
    public static RuleItemNodeConverter getKeyGeneratorNodeConverter() {
        return KEY_GENERATOR_NODE_CONVERTER;
    }
    
    /**
     * Get auditor node converter.
     *
     * @return auditor node converter
     */
    public static RuleItemNodeConverter getAuditorNodeConverter() {
        return AUDITOR_NODE_CONVERTER;
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
     * Get sharding cache path.
     *
     * @return sharding cache path
     */
    public static String getShardingCachePath() {
        return String.join("/", SHARDING_CACHE_NODE);
    }
    
    /**
     * Is default database strategy path.
     *
     * @param rulePath rule path
     * @return true or false
     */
    public static boolean isDefaultDatabaseStrategyPath(final String rulePath) {
        Pattern pattern = Pattern.compile(ROOT_NODE_CONVERTER.getRuleNodePrefix() + "/" + DEFAULT_STRATEGIES_NODE + "/" + DEFAULT_DATABASE_STRATEGY_NODE + VERSIONS, Pattern.CASE_INSENSITIVE);
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
        Pattern pattern = Pattern.compile(ROOT_NODE_CONVERTER.getRuleNodePrefix() + "/" + DEFAULT_STRATEGIES_NODE + "/" + DEFAULT_TABLE_STRATEGY_NODE + VERSIONS, Pattern.CASE_INSENSITIVE);
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
        Pattern pattern = Pattern.compile(ROOT_NODE_CONVERTER.getRuleNodePrefix() + "/" + DEFAULT_STRATEGIES_NODE + "/" + DEFAULT_KEY_GENERATE_STRATEGY_NODE + VERSIONS, Pattern.CASE_INSENSITIVE);
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
        Pattern pattern = Pattern.compile(ROOT_NODE_CONVERTER.getRuleNodePrefix() + "/" + DEFAULT_STRATEGIES_NODE + "/" + DEFAULT_AUDIT_STRATEGY_NODE + VERSIONS, Pattern.CASE_INSENSITIVE);
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
        Pattern pattern = Pattern.compile(ROOT_NODE_CONVERTER.getRuleNodePrefix() + "/" + DEFAULT_STRATEGIES_NODE + "/" + DEFAULT_SHARDING_COLUMN_NODE + VERSIONS, Pattern.CASE_INSENSITIVE);
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
        Pattern pattern = Pattern.compile(ROOT_NODE_CONVERTER.getRuleNodePrefix() + "/" + SHARDING_CACHE_NODE + VERSIONS, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(rulePath);
        return matcher.find();
    }
    
    /**
     * Is sharding algorithm with active version path.
     *
     * @param activeVersionPath active version path
     * @return true or false
     */
    public static boolean isDefaultDatabaseStrategyWithActiveVersionPath(final String activeVersionPath) {
        Pattern pattern = Pattern.compile(ROOT_NODE_CONVERTER.getRuleNodePrefix() + "/" + DEFAULT_STRATEGIES_NODE + "/" + DEFAULT_DATABASE_STRATEGY_NODE + ACTIVE_VERSION, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(activeVersionPath);
        return matcher.find();
    }
    
    /**
     * Is default table strategy with active version path.
     *
     * @param activeVersionPath active version path
     * @return true or false
     */
    public static boolean isDefaultTableStrategyWithActiveVersionPath(final String activeVersionPath) {
        Pattern pattern = Pattern.compile(ROOT_NODE_CONVERTER.getRuleNodePrefix() + "/" + DEFAULT_STRATEGIES_NODE + "/" + DEFAULT_TABLE_STRATEGY_NODE + ACTIVE_VERSION, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(activeVersionPath);
        return matcher.find();
    }
    
    /**
     * Is default key generate strategy with active version path.
     *
     * @param activeVersionPath active version path
     * @return true or false
     */
    public static boolean isDefaultKeyGenerateStrategyWithActiveVersionPath(final String activeVersionPath) {
        Pattern pattern = Pattern.compile(
                ROOT_NODE_CONVERTER.getRuleNodePrefix() + "/" + DEFAULT_STRATEGIES_NODE + "/" + DEFAULT_KEY_GENERATE_STRATEGY_NODE + ACTIVE_VERSION, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(activeVersionPath);
        return matcher.find();
    }
    
    /**
     * Is default audit strategy with active version path.
     *
     * @param activeVersionPath active version path
     * @return true or false
     */
    public static boolean isDefaultAuditStrategyWithActiveVersionPath(final String activeVersionPath) {
        Pattern pattern = Pattern.compile(ROOT_NODE_CONVERTER.getRuleNodePrefix() + "/" + DEFAULT_STRATEGIES_NODE + "/" + DEFAULT_AUDIT_STRATEGY_NODE + ACTIVE_VERSION, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(activeVersionPath);
        return matcher.find();
    }
    
    /**
     * Is default sharding column with active version path.
     *
     * @param activeVersionPath active version path
     * @return true or false
     */
    public static boolean isDefaultShardingColumnWithActiveVersionPath(final String activeVersionPath) {
        Pattern pattern = Pattern.compile(ROOT_NODE_CONVERTER.getRuleNodePrefix() + "/" + DEFAULT_STRATEGIES_NODE + "/" + DEFAULT_SHARDING_COLUMN_NODE + ACTIVE_VERSION, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(activeVersionPath);
        return matcher.find();
    }
    
    /**
     * Is sharding cache with active version path.
     *
     * @param activeVersionPath active version path
     * @return true or false
     */
    public static boolean isShardingCacheWithActiveVersionPath(final String activeVersionPath) {
        Pattern pattern = Pattern.compile(ROOT_NODE_CONVERTER.getRuleNodePrefix() + "/" + SHARDING_CACHE_NODE + ACTIVE_VERSION, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(activeVersionPath);
        return matcher.find();
    }
}
