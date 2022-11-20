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
 * SQL hint utils.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SQLHintUtils {
    
    private static final String SQL_COMMENT_SUFFIX = "*/";
    
    private static final String SQL_START_HINT_TOKEN = "/* SHARDINGSPHERE_HINT:";
    
    private static final String SQL_HINT_TOKEN = "shardingsphere_hint:";
    
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
        int startIndex = comment.toLowerCase().indexOf(SQL_HINT_TOKEN);
        if (startIndex < 0) {
            return result;
        }
        startIndex = startIndex + SQL_HINT_TOKEN.length();
        int endIndex = comment.endsWith(SQL_COMMENT_SUFFIX) ? comment.indexOf(SQL_COMMENT_SUFFIX) : comment.length();
        Collection<String> sqlHints = Splitter.on(SQL_HINT_SPLIT).trimResults().splitToList(comment.substring(startIndex, endIndex).trim());
        for (String each : sqlHints) {
            List<String> hintValues = Splitter.on(SQL_HINT_VALUE_SPLIT).trimResults().splitToList(each);
            if (SQL_HINT_VALUE_SIZE == hintValues.size()) {
                result.put(hintValues.get(0).toUpperCase(), convert(hintValues.get(1)));
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
    public static HintValueContext extractHint(final String sql) {
        HintValueContext result = new HintValueContext();
        if (!sql.startsWith(SQL_START_HINT_TOKEN)) {
            return result;
        }
        String hintText = sql.substring(0, sql.indexOf(SQL_COMMENT_SUFFIX) + 2);
        Properties hintProperties = SQLHintUtils.getSQLHintProps(hintText);
        if (hintProperties.containsKey(SQLHintPropertiesKey.WRITE_ROUTE_ONLY_KEY.getKey())) {
            result.setWriteRouteOnly(Boolean.valueOf(hintProperties.getProperty(SQLHintPropertiesKey.WRITE_ROUTE_ONLY_KEY.getKey())));
        }
        if (hintProperties.containsKey(SQLHintPropertiesKey.USE_TRAFFIC_KEY.getKey())) {
            result.setUseTraffic(Boolean.valueOf(hintProperties.getProperty(SQLHintPropertiesKey.USE_TRAFFIC_KEY.getKey())));
        }
        if (hintProperties.containsKey(SQLHintPropertiesKey.SKIP_ENCRYPT_REWRITE_KEY.getKey())) {
            result.setSkipEncryptRewrite(Boolean.valueOf(hintProperties.getProperty(SQLHintPropertiesKey.SKIP_ENCRYPT_REWRITE_KEY.getKey())));
        }
        if (hintProperties.containsKey(SQLHintPropertiesKey.DISABLE_AUDIT_NAMES_KEY.getKey())) {
            result.setDisableAuditNames(hintProperties.getProperty(SQLHintPropertiesKey.DISABLE_AUDIT_NAMES_KEY.getKey()));
        }
        if (hintProperties.containsKey(SQLHintPropertiesKey.SHADOW_KEY.getKey())) {
            result.setShadow(Boolean.valueOf(hintProperties.getProperty(SQLHintPropertiesKey.SHADOW_KEY.getKey())));
        }
        for (Entry<Object, Object> entry : hintProperties.entrySet()) {
            Comparable value = entry.getValue() instanceof Comparable ? (Comparable<?>) entry.getValue() : Objects.toString(entry.getValue());
            if (Objects.toString(entry.getKey()).contains(SQLHintPropertiesKey.SHARDING_DATABASE_VALUE_KEY.getKey())) {
                result.getShardingDatabaseValues().put(Objects.toString(entry.getKey()), value);
            }
            if (Objects.toString(entry.getKey()).contains(SQLHintPropertiesKey.SHARDING_TABLE_VALUE_KEY.getKey())) {
                result.getShardingDatabaseValues().put(Objects.toString(entry.getKey()), value);
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
        if (sql.startsWith(SQL_START_HINT_TOKEN)) {
            return sql.substring(sql.indexOf(SQL_COMMENT_SUFFIX) + 2);
        }
        return sql;
    }
}
