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

import com.cedarsoftware.util.CaseInsensitiveMap;
import com.google.common.base.Splitter;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.math.BigInteger;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;

/**
 * SQL hint utility class.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SQLHintUtils {
    
    private static final String SQL_COMMENT_PREFIX = "/*";
    
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
        if (!containsSQLHint(sql)) {
            return new HintValueContext();
        }
        HintValueContext result = new HintValueContext();
        int hintKeyValueBeginIndex = getHintKeyValueBeginIndex(sql);
        String hintKeyValueText = sql.substring(hintKeyValueBeginIndex, sql.indexOf(SQL_COMMENT_SUFFIX, hintKeyValueBeginIndex));
        Map<String, String> hintKeyValues = getSQLHintKeyValues(hintKeyValueText);
        if (containsHintKey(hintKeyValues, SQLHintPropertiesKey.DATASOURCE_NAME_KEY)) {
            result.setDataSourceName(getHintValue(hintKeyValues, SQLHintPropertiesKey.DATASOURCE_NAME_KEY));
        }
        if (containsHintKey(hintKeyValues, SQLHintPropertiesKey.WRITE_ROUTE_ONLY_KEY)) {
            result.setWriteRouteOnly(Boolean.parseBoolean(getHintValue(hintKeyValues, SQLHintPropertiesKey.WRITE_ROUTE_ONLY_KEY)));
        }
        if (containsHintKey(hintKeyValues, SQLHintPropertiesKey.SKIP_SQL_REWRITE_KEY)) {
            result.setSkipSQLRewrite(Boolean.parseBoolean(getHintValue(hintKeyValues, SQLHintPropertiesKey.SKIP_SQL_REWRITE_KEY)));
        }
        if (containsHintKey(hintKeyValues, SQLHintPropertiesKey.SKIP_METADATA_VALIDATE_KEY)) {
            result.setSkipMetadataValidate(Boolean.parseBoolean(getHintValue(hintKeyValues, SQLHintPropertiesKey.SKIP_METADATA_VALIDATE_KEY)));
        }
        if (containsHintKey(hintKeyValues, SQLHintPropertiesKey.DISABLE_AUDIT_NAMES_KEY)) {
            String property = getHintValue(hintKeyValues, SQLHintPropertiesKey.DISABLE_AUDIT_NAMES_KEY);
            result.getDisableAuditNames().addAll(getSplitterSQLHintValue(property));
        }
        if (containsHintKey(hintKeyValues, SQLHintPropertiesKey.SHADOW_KEY)) {
            result.setShadow(Boolean.parseBoolean(getHintValue(hintKeyValues, SQLHintPropertiesKey.SHADOW_KEY)));
        }
        for (Entry<String, String> entry : hintKeyValues.entrySet()) {
            Comparable<?> value = convert(entry.getValue());
            if (containsHintKey(Objects.toString(entry.getKey()), SQLHintPropertiesKey.SHARDING_DATABASE_VALUE_KEY)) {
                result.getShardingDatabaseValues().put(Objects.toString(entry.getKey()).toUpperCase(), value);
            }
            if (containsHintKey(Objects.toString(entry.getKey()), SQLHintPropertiesKey.SHARDING_TABLE_VALUE_KEY)) {
                result.getShardingTableValues().put(Objects.toString(entry.getKey()).toUpperCase(), value);
            }
        }
        return result;
    }
    
    private static int getHintKeyValueBeginIndex(final String sql) {
        int tokenBeginIndex = sql.contains(SQLHintTokenType.SQL_START_HINT_TOKEN.getKey()) ? sql.indexOf(SQLHintTokenType.SQL_START_HINT_TOKEN.getKey())
                : sql.indexOf(SQLHintTokenType.SQL_START_HINT_TOKEN.getAlias());
        return sql.indexOf(":", tokenBeginIndex) + 1;
    }
    
    private static boolean containsSQLHint(final String sql) {
        return (sql.contains(SQLHintTokenType.SQL_START_HINT_TOKEN.getKey()) || sql.contains(SQLHintTokenType.SQL_START_HINT_TOKEN.getAlias()))
                && sql.contains(SQL_COMMENT_PREFIX) && sql.contains(SQL_COMMENT_SUFFIX);
    }
    
    private static Map<String, String> getSQLHintKeyValues(final String hintKeyValueText) {
        Collection<String> sqlHints = Splitter.on(SQL_HINT_SPLIT).trimResults().splitToList(hintKeyValueText.trim());
        Map<String, String> result = new CaseInsensitiveMap<>(sqlHints.size(), 1F);
        for (String each : sqlHints) {
            List<String> hintValues = Splitter.on(SQL_HINT_VALUE_SPLIT).trimResults().splitToList(each);
            if (SQL_HINT_VALUE_SIZE == hintValues.size()) {
                result.put(hintValues.get(0), hintValues.get(1));
            }
        }
        return result;
    }
    
    private static Comparable<?> convert(final String value) {
        try {
            return new BigInteger(value);
        } catch (final NumberFormatException ignored) {
            return value;
        }
    }
    
    private static boolean containsHintKey(final Map<String, String> hintKeyValues, final SQLHintPropertiesKey sqlHintPropsKey) {
        return hintKeyValues.containsKey(sqlHintPropsKey.getKey()) || hintKeyValues.containsKey(sqlHintPropsKey.getAlias());
    }
    
    private static boolean containsHintKey(final String hintPropKey, final SQLHintPropertiesKey sqlHintPropsKey) {
        return hintPropKey.contains(sqlHintPropsKey.getKey()) || hintPropKey.contains(sqlHintPropsKey.getAlias());
    }
    
    private static String getHintValue(final Map<String, String> hintKeyValues, final SQLHintPropertiesKey sqlHintPropsKey) {
        String result = hintKeyValues.get(sqlHintPropsKey.getKey());
        return null == result ? hintKeyValues.get(sqlHintPropsKey.getAlias()) : result;
    }
    
    private static Collection<String> getSplitterSQLHintValue(final String property) {
        return property.isEmpty() ? Collections.emptySet() : new HashSet<>(Splitter.on(SQL_HINT_VALUE_COLLECTION_SPLIT).omitEmptyStrings().trimResults().splitToList(property));
    }
    
    /**
     * Remove SQL hint.
     *
     * @param sql SQL
     * @return SQL after remove hint
     */
    public static String removeHint(final String sql) {
        if (containsSQLHint(sql)) {
            int hintKeyValueBeginIndex = getHintKeyValueBeginIndex(sql);
            int sqlHintBeginIndex = sql.substring(0, hintKeyValueBeginIndex).lastIndexOf(SQL_COMMENT_PREFIX, hintKeyValueBeginIndex);
            int sqlHintEndIndex = sql.indexOf(SQL_COMMENT_SUFFIX, hintKeyValueBeginIndex) + SQL_COMMENT_SUFFIX.length();
            String removedHintSQL = sql.substring(0, sqlHintBeginIndex) + sql.substring(sqlHintEndIndex);
            return removedHintSQL.trim();
        }
        return sql;
    }
}
