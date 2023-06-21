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

package org.apache.shardingsphere.encrypt.metadata.converter;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Encrypt node converter.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class EncryptNodeConverter {
    
    private static final String ROOT_NODE = "encrypt";
    
    private static final String TABLES_NODE = "tables";
    
    private static final String ENCRYPTORS_NODE = "encryptors";
    
    private static final String RULES_NODE_PREFIX = "/([\\w\\-]+)/([\\w\\-]+)/rules/";
    
    private static final String RULE_NAME_PATTERN = "/([\\w\\-]+)?";
    
    private static final String RULE_ACTIVE_VERSION = "/([\\w\\-]+)/active_version$";
    
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
     * Get encryptor path.
     *
     * @param encryptorName encryptor name
     * @return encryptor path
     */
    public static String getEncryptorPath(final String encryptorName) {
        return String.join("/", ENCRYPTORS_NODE, encryptorName);
    }
    
    /**
     * Is encrypt path.
     *
     * @param rulePath rule path
     * @return true or false
     */
    public static boolean isEncryptPath(final String rulePath) {
        Pattern pattern = Pattern.compile(RULES_NODE_PREFIX + ROOT_NODE + "\\.*", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(rulePath);
        return matcher.find();
    }
    
    /**
     * Is encrypt table path.
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
     * Is encryptor path.
     *
     * @param rulePath rule path
     * @return true or false
     */
    public static boolean isEncryptorPath(final String rulePath) {
        Pattern pattern = Pattern.compile(RULES_NODE_PREFIX + ROOT_NODE + "/" + ENCRYPTORS_NODE + "\\.*", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(rulePath);
        return matcher.find();
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
     *  Get encryptor name.
     *
     * @param rulePath rule path
     * @return encryptor name
     */
    public static Optional<String> getEncryptorName(final String rulePath) {
        Pattern pattern = Pattern.compile(RULES_NODE_PREFIX + ROOT_NODE + "/" + ENCRYPTORS_NODE + RULE_NAME_PATTERN, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(rulePath);
        return matcher.find() ? Optional.of(matcher.group(3)) : Optional.empty();
    }
    
    /**
     * Get encrypt table name by active version path.
     * 
     * @param activeVersionPath active version path
     * @return table name
     */
    public static Optional<String> getTableNameByActiveVersionPath(final String activeVersionPath) {
        Pattern pattern = Pattern.compile(RULES_NODE_PREFIX + ROOT_NODE + "/" + TABLES_NODE + RULE_ACTIVE_VERSION, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(activeVersionPath);
        return matcher.find() ? Optional.of(matcher.group(3)) : Optional.empty();
    }
    
    /**
     * Get encryptor name by active version path.
     *
     * @param activeVersionPath active version path
     * @return encryptor name
     */
    public static Optional<String> getEncryptorNameByActiveVersionPath(final String activeVersionPath) {
        Pattern pattern = Pattern.compile(RULES_NODE_PREFIX + ROOT_NODE + "/" + ENCRYPTORS_NODE + RULE_ACTIVE_VERSION, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(activeVersionPath);
        return matcher.find() ? Optional.of(matcher.group(3)) : Optional.empty();
    }
}
