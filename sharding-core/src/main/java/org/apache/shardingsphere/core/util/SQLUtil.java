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

package org.apache.shardingsphere.core.util;

import com.google.common.base.CharMatcher;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import org.apache.shardingsphere.core.constant.DatabaseType;
import org.apache.shardingsphere.core.parsing.lexer.dialect.mysql.MySQLKeyword;
import org.apache.shardingsphere.core.parsing.lexer.token.DefaultKeyword;
import org.apache.shardingsphere.core.parsing.lexer.token.Symbol;

/**
 * SQL utility class.
 * 
 * @author gaohongtao
 */
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class SQLUtil {
    
    /**
     * Get exactly value for SQL expression.
     * 
     * <p>remove special char for SQL expression</p>
     * 
     * @param value SQL expression
     * @return exactly SQL expression
     */
    public static String getExactlyValue(final String value) {
        return null == value ? null : CharMatcher.anyOf("[]`'\"").removeFrom(value);
    }
    
    /**
     * Get exactly SQL expression.
     *
     * <p>remove space for SQL expression</p>
     *
     * @param value SQL expression
     * @return exactly SQL expression
     */
    public static String getExactlyExpression(final String value) {
        return null == value ? null : CharMatcher.anyOf(" ").removeFrom(value);
    }
    
    /**
     * Get original value for SQL expression.
     * 
     * @param value SQL expression
     * @param databaseType database type
     * @return original SQL expression
     */
    public static String getOriginalValue(final String value, final DatabaseType databaseType) {
        if (DatabaseType.MySQL != databaseType) {
            return value;
        }
        try {
            DefaultKeyword.valueOf(value.toUpperCase());
            return String.format("`%s`", value);
        } catch (final IllegalArgumentException ex) {
            return getOriginalValueForMySQLKeyword(value);
        }
    }
    
    private static String getOriginalValueForMySQLKeyword(final String value) {
        try {
            MySQLKeyword.valueOf(value.toUpperCase());
            return String.format("`%s`", value);
        } catch (final IllegalArgumentException ex) {
            return value;
        }
    }
    
    /**
     * Get name without schema.
     * 
     * @param value SQL expression
     * @return name without schema
     */
    public static String getNameWithoutSchema(final String value) {
        return value.contains(Symbol.DOT.getLiterals()) ? value.substring(value.lastIndexOf(Symbol.DOT.getLiterals()) + Symbol.DOT.getLiterals().length()) : value;
    }
}
