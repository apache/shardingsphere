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

package org.apache.shardingsphere.infra.hint;

import com.google.common.base.Splitter;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.math.BigInteger;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;

/**
 * SQL hint utility class.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SQLHintUtils {
    
    private static final String SQL_COMMENT_SUFFIX = "*/";
    
    private static final String SQL_HINT_SPLIT = ",";
    
    private static final String SQL_HINT_VALUE_SPLIT = "=";
    
    private static final String SQL_HINT_VALUE_COLLECTION_SPLIT = " ";
    
    private static final int SQL_HINT_VALUE_SIZE = 2;
    
    /**
     * Get SQL hint props.
     *
     * @param comment SQL comment
     * @return SQL hint props
     */
    public static Properties getSQLHintProps(final String comment) {
        Properties result = new Properties();
        String lowerCaseComment = comment.toLowerCase();
        int startIndex = lowerCaseComment.startsWith(SQLHintTokenEnum.SQL_START_HINT_TOKEN.getAlias().toLowerCase())
                ? lowerCaseComment.indexOf(SQLHintTokenEnum.SQL_HINT_TOKEN.getAlias())
                : lowerCaseComment.indexOf(SQLHintTokenEnum.SQL_HINT_TOKEN.getKey());
        if (startIndex < 0) {
            return result;
        }
        startIndex = startIndex + SQLHintTokenEnum.SQL_HINT_TOKEN.getKey().length();
        int endIndex = comment.endsWith(SQL_COMMENT_SUFFIX) ? comment.indexOf(SQL_COMMENT_SUFFIX) : comment.length();
        Collection<String> sqlHints = Splitter.on(SQL_HINT_SPLIT).trimResults().splitToList(comment.substring(startIndex, endIndex).trim());
        for (String each : sqlHints) {
            List<String> hintValues = Splitter.on(SQL_HINT_VALUE_SPLIT).trimResults().splitToList(each);
            if (SQL_HINT_VALUE_SIZE == hintValues.size()) {
                result.put(hintValues.get(0), convert(hintValues.get(1)));
            }
        }
        return result;
    }
    
    private static Object convert(final String value) {
        try {
            return new BigInteger(value);
        } catch (final NumberFormatException e) {
            return value;
        }
    }
    
    /**
     * Get splitter SQL hint Value.
     *
     * @param value SQL hint value
     * @return Splitter SQL hint value
     */
    public static Collection<String> getSplitterSQLHintValue(final String value) {
        return value.isEmpty() ? Collections.emptySet() : new HashSet<>(Splitter.on(SQLHintUtils.SQL_HINT_VALUE_COLLECTION_SPLIT).omitEmptyStrings().trimResults().splitToList(value));
    }
    
    /**
     * Extract SQL hint.
     *
     * @param sql SQL
     * @return Hint value context
     */
    public static Optional<HintValueContext> extractHint(final String sql) {
        if (!startWithHint(sql)) {
            return Optional.empty();
        }
        HintValueContext result = new HintValueContext();
        String hintText = sql.substring(0, sql.indexOf(SQL_COMMENT_SUFFIX) + 2);
        Properties hintProperties = SQLHintUtils.getSQLHintProps(hintText);
        if (containsPropertyKey(hintProperties, SQLHintPropertiesKey.DATASOURCE_NAME_KEY)) {
            result.setDataSourceName(getProperty(hintProperties, SQLHintPropertiesKey.DATASOURCE_NAME_KEY));
        }
        if (containsPropertyKey(hintProperties, SQLHintPropertiesKey.WRITE_ROUTE_ONLY_KEY)) {
            result.setWriteRouteOnly(Boolean.parseBoolean(getProperty(hintProperties, SQLHintPropertiesKey.WRITE_ROUTE_ONLY_KEY)));
        }
        if (containsPropertyKey(hintProperties, SQLHintPropertiesKey.USE_TRAFFIC_KEY)) {
            result.setUseTraffic(Boolean.parseBoolean(getProperty(hintProperties, SQLHintPropertiesKey.USE_TRAFFIC_KEY)));
        }
        if (containsPropertyKey(hintProperties, SQLHintPropertiesKey.SKIP_SQL_REWRITE_KEY)) {
            result.setSkipSQLRewrite(Boolean.parseBoolean(getProperty(hintProperties, SQLHintPropertiesKey.SKIP_SQL_REWRITE_KEY)));
        }
        if (containsPropertyKey(hintProperties, SQLHintPropertiesKey.DISABLE_AUDIT_NAMES_KEY)) {
            result.setDisableAuditNames(getProperty(hintProperties, SQLHintPropertiesKey.DISABLE_AUDIT_NAMES_KEY));
        }
        if (containsPropertyKey(hintProperties, SQLHintPropertiesKey.SHADOW_KEY)) {
            result.setShadow(Boolean.parseBoolean(getProperty(hintProperties, SQLHintPropertiesKey.SHADOW_KEY)));
        }
        for (Entry<Object, Object> entry : hintProperties.entrySet()) {
            Comparable<?> value = entry.getValue() instanceof Comparable ? (Comparable<?>) entry.getValue() : Objects.toString(entry.getValue());
            if (containsPropertyKey(Objects.toString(entry.getKey()), SQLHintPropertiesKey.SHARDING_DATABASE_VALUE_KEY)) {
                result.getShardingDatabaseValues().put(Objects.toString(entry.getKey()).toUpperCase(), value);
            }
            if (containsPropertyKey(Objects.toString(entry.getKey()), SQLHintPropertiesKey.SHARDING_TABLE_VALUE_KEY)) {
                result.getShardingTableValues().put(Objects.toString(entry.getKey()).toUpperCase(), value);
            }
        }
        return Optional.of(result);
    }
    
    private static boolean startWithHint(final String sql) {
        return null != sql && (sql.startsWith(SQLHintTokenEnum.SQL_START_HINT_TOKEN.getKey()) || sql.startsWith(SQLHintTokenEnum.SQL_START_HINT_TOKEN.getAlias()));
    }
    
    private static boolean containsPropertyKey(final Properties hintProperties, final SQLHintPropertiesKey sqlHintPropertiesKey) {
        return hintProperties.containsKey(sqlHintPropertiesKey.getKey()) || hintProperties.containsKey(sqlHintPropertiesKey.getAlias());
    }
    
    private static boolean containsPropertyKey(final String hintPropertyKey, final SQLHintPropertiesKey sqlHintPropertiesKey) {
        return hintPropertyKey.contains(sqlHintPropertiesKey.getKey()) || hintPropertyKey.contains(sqlHintPropertiesKey.getAlias());
    }
    
    private static String getProperty(final Properties hintProperties, final SQLHintPropertiesKey sqlHintPropertiesKey) {
        String result = hintProperties.getProperty(sqlHintPropertiesKey.getKey());
        return null == result ? hintProperties.getProperty(sqlHintPropertiesKey.getAlias()) : result;
    }
    
    /**
     * Remove SQL hint.
     *
     * @param sql SQL
     * @return SQL after remove hint
     */
    public static String removeHint(final String sql) {
        if (startWithHint(sql)) {
            return sql.substring(sql.indexOf(SQL_COMMENT_SUFFIX) + 2);
        }
        return sql;
    }
}
