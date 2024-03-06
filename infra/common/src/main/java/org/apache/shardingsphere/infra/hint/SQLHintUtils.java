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
     * Extract SQL hint.
     *
     * @param sql SQL
     * @return hint value context
     */
    public static HintValueContext extractHint(final String sql) {
        if (!startWithHint(sql, SQLHintTokenEnum.SQL_START_HINT_TOKEN)) {
            return new HintValueContext();
        }
        HintValueContext result = new HintValueContext();
        String hintText = sql.substring(0, sql.indexOf(SQL_COMMENT_SUFFIX) + 2);
        Properties hintProps = getSQLHintProps(hintText);
        if (containsPropertyKey(hintProps, SQLHintPropertiesKey.DATASOURCE_NAME_KEY)) {
            result.setDataSourceName(getProperty(hintProps, SQLHintPropertiesKey.DATASOURCE_NAME_KEY));
        }
        if (containsPropertyKey(hintProps, SQLHintPropertiesKey.WRITE_ROUTE_ONLY_KEY)) {
            result.setWriteRouteOnly(Boolean.parseBoolean(getProperty(hintProps, SQLHintPropertiesKey.WRITE_ROUTE_ONLY_KEY)));
        }
        if (containsPropertyKey(hintProps, SQLHintPropertiesKey.USE_TRAFFIC_KEY)) {
            result.setUseTraffic(Boolean.parseBoolean(getProperty(hintProps, SQLHintPropertiesKey.USE_TRAFFIC_KEY)));
        }
        if (containsPropertyKey(hintProps, SQLHintPropertiesKey.SKIP_SQL_REWRITE_KEY)) {
            result.setSkipSQLRewrite(Boolean.parseBoolean(getProperty(hintProps, SQLHintPropertiesKey.SKIP_SQL_REWRITE_KEY)));
        }
        if (containsPropertyKey(hintProps, SQLHintPropertiesKey.DISABLE_AUDIT_NAMES_KEY)) {
            String property = getProperty(hintProps, SQLHintPropertiesKey.DISABLE_AUDIT_NAMES_KEY);
            result.getDisableAuditNames().addAll(getSplitterSQLHintValue(property));
        }
        if (containsPropertyKey(hintProps, SQLHintPropertiesKey.SHADOW_KEY)) {
            result.setShadow(Boolean.parseBoolean(getProperty(hintProps, SQLHintPropertiesKey.SHADOW_KEY)));
        }
        for (Entry<Object, Object> entry : hintProps.entrySet()) {
            Comparable<?> value = entry.getValue() instanceof Comparable ? (Comparable<?>) entry.getValue() : Objects.toString(entry.getValue());
            if (containsPropertyKey(Objects.toString(entry.getKey()), SQLHintPropertiesKey.SHARDING_DATABASE_VALUE_KEY)) {
                result.getShardingDatabaseValues().put(Objects.toString(entry.getKey()).toUpperCase(), value);
            }
            if (containsPropertyKey(Objects.toString(entry.getKey()), SQLHintPropertiesKey.SHARDING_TABLE_VALUE_KEY)) {
                result.getShardingTableValues().put(Objects.toString(entry.getKey()).toUpperCase(), value);
            }
        }
        return result;
    }
    
    /**
     * Remove SQL hint.
     *
     * @param sql SQL
     * @return SQL after remove hint
     */
    public static String removeHint(final String sql) {
        return startWithHint(sql, SQLHintTokenEnum.SQL_START_HINT_TOKEN) ? sql.substring(sql.indexOf(SQL_COMMENT_SUFFIX) + 2).trim() : sql;
    }
    
    private static Properties getSQLHintProps(final String comment) {
        Properties result = new Properties();
        int startIndex = getStartIndex(comment, SQLHintTokenEnum.SQL_START_HINT_TOKEN, SQLHintTokenEnum.SQL_HINT_TOKEN);
        if (startIndex < 0) {
            return result;
        }
        int endIndex = comment.endsWith(SQL_COMMENT_SUFFIX) ? comment.indexOf(SQL_COMMENT_SUFFIX) : comment.length();
        Collection<String> sqlHints = Splitter.on(SQL_HINT_SPLIT).trimResults().splitToList(comment.substring(startIndex, endIndex).trim());
        for (String each : sqlHints) {
            List<String> hintValues = Splitter.on(SQL_HINT_VALUE_SPLIT).limit(SQL_HINT_VALUE_SIZE).trimResults().splitToList(each);
            if (SQL_HINT_VALUE_SIZE == hintValues.size()) {
                result.put(hintValues.get(0), convert(hintValues.get(1)));
            }
        }
        return result;
    }
    
    private static int getStartIndex(final String comment, final SQLHintTokenEnum sqlStartHintToken, final SQLHintTokenEnum sqlHintToken) {
        String lowerCaseComment = comment.toLowerCase();
        int result = lowerCaseComment.startsWith(sqlStartHintToken.getAlias().toLowerCase())
                ? lowerCaseComment.indexOf(sqlHintToken.getAlias())
                : lowerCaseComment.indexOf(sqlHintToken.getKey());
        if (result >= 0) {
            return result + sqlHintToken.getKey().length();
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
    
    private static boolean startWithHint(final String sql, final SQLHintTokenEnum sqlStartHintToken) {
        return null != sql && (sql.startsWith(sqlStartHintToken.getKey()) || sql.startsWith(sqlStartHintToken.getAlias()));
    }
    
    private static boolean containsPropertyKey(final Properties hintProps, final SQLHintPropertiesKey sqlHintPropsKey) {
        return hintProps.containsKey(sqlHintPropsKey.getKey()) || hintProps.containsKey(sqlHintPropsKey.getAlias());
    }
    
    private static boolean containsPropertyKey(final String hintPropKey, final SQLHintPropertiesKey sqlHintPropsKey) {
        return hintPropKey.contains(sqlHintPropsKey.getKey()) || hintPropKey.contains(sqlHintPropsKey.getAlias());
    }
    
    private static String getProperty(final Properties hintProps, final SQLHintPropertiesKey sqlHintPropsKey) {
        String result = hintProps.getProperty(sqlHintPropsKey.getKey());
        return null == result ? hintProps.getProperty(sqlHintPropsKey.getAlias()) : result;
    }
    
    private static Collection<String> getSplitterSQLHintValue(final String property) {
        return property.isEmpty() ? Collections.emptySet() : new HashSet<>(Splitter.on(SQLHintUtils.SQL_HINT_VALUE_COLLECTION_SPLIT).omitEmptyStrings().trimResults().splitToList(property));
    }
}
