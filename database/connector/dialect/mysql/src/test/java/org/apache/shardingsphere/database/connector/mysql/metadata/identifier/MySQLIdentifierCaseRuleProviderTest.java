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

package org.apache.shardingsphere.database.connector.mysql.metadata.identifier;

import org.apache.shardingsphere.database.connector.core.metadata.database.enums.QuoteCharacter;
import org.apache.shardingsphere.database.connector.core.metadata.identifier.IdentifierCaseRule;
import org.apache.shardingsphere.database.connector.core.metadata.identifier.IdentifierCaseRuleProvider;
import org.apache.shardingsphere.database.connector.core.metadata.identifier.IdentifierCaseRuleProviderContext;
import org.apache.shardingsphere.database.connector.core.metadata.identifier.IdentifierScope;
import org.apache.shardingsphere.database.connector.core.metadata.identifier.LookupMode;
import org.apache.shardingsphere.database.connector.core.spi.DatabaseTypedSPILoader;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import javax.sql.DataSource;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MySQLIdentifierCaseRuleProviderTest {
    
    private static final DatabaseType DATABASE_TYPE = TypedSPILoader.getService(DatabaseType.class, "MySQL");
    
    private final IdentifierCaseRuleProvider provider = DatabaseTypedSPILoader.getService(IdentifierCaseRuleProvider.class, DATABASE_TYPE);
    
    @Test
    void assertProvideWithNullContext() {
        NullPointerException actual = assertThrows(NullPointerException.class, () -> provider.provide(null));
        assertThat(actual.getMessage(), is("context cannot be null."));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("provideArguments")
    void assertProvide(final String name, final IdentifierCaseRuleProviderContext context, final LookupMode expectedQuotedLookupMode,
                       final LookupMode expectedUnquotedLookupMode, final Boolean expectedMatch) {
        IdentifierCaseRule actual = provider.provide(context).map(ruleSet -> ruleSet.getRule(IdentifierScope.TABLE)).orElse(null);
        assertLookupMode(actual, QuoteCharacter.BACK_QUOTE, expectedQuotedLookupMode);
        assertLookupMode(actual, QuoteCharacter.NONE, expectedUnquotedLookupMode);
        assertMatch(actual, expectedMatch);
    }
    
    @Test
    void assertProvideWithQuotedTableName() throws SQLException {
        IdentifierCaseRule actual = provider.provide(new IdentifierCaseRuleProviderContext(DATABASE_TYPE, mockDataSource(true, 1)))
                .map(ruleSet -> ruleSet.getRule(IdentifierScope.TABLE)).orElseThrow(AssertionError::new);
        assertThat(actual.getLookupMode(QuoteCharacter.BACK_QUOTE), is(LookupMode.NORMALIZED));
        assertThat(actual.matches("t_mask", "T_MASK", QuoteCharacter.BACK_QUOTE), is(Boolean.TRUE));
    }
    
    private void assertMatch(final IdentifierCaseRule actual, final Boolean expected) {
        if (null == expected) {
            assertNull(actual);
        } else {
            assertThat(actual.matches("foo", "FOO", QuoteCharacter.NONE), is(expected));
        }
    }
    
    private void assertLookupMode(final IdentifierCaseRule actual, final QuoteCharacter quoteCharacter, final LookupMode expected) {
        if (null == expected) {
            assertNull(actual);
        } else {
            assertThat(actual.getLookupMode(quoteCharacter), is(expected));
        }
    }
    
    private static Stream<Arguments> provideArguments() throws SQLException {
        return Stream.of(
                Arguments.of("null_data_source", new IdentifierCaseRuleProviderContext(DATABASE_TYPE, null), null, null, null),
                Arguments.of("lower_case_table_names_0", new IdentifierCaseRuleProviderContext(DATABASE_TYPE, mockDataSource(true, 0)), null, null, null),
                Arguments.of("lower_case_table_names_1", new IdentifierCaseRuleProviderContext(DATABASE_TYPE, mockDataSource(true, 1)),
                        LookupMode.NORMALIZED, LookupMode.NORMALIZED, Boolean.TRUE),
                Arguments.of("lower_case_table_names_2", new IdentifierCaseRuleProviderContext(DATABASE_TYPE, mockDataSource(true, 2)),
                        LookupMode.NORMALIZED, LookupMode.NORMALIZED, Boolean.TRUE),
                Arguments.of("no_result_row", new IdentifierCaseRuleProviderContext(DATABASE_TYPE, mockDataSource(false, 0)), null, null, null),
                Arguments.of("sql_exception", new IdentifierCaseRuleProviderContext(DATABASE_TYPE, mockFailingDataSource()), null, null, null));
    }
    
    private static DataSource mockDataSource(final boolean hasResultSetRow, final int lowerCaseTableNames) throws SQLException {
        return (DataSource) Proxy.newProxyInstance(DataSource.class.getClassLoader(), new Class[]{DataSource.class},
                (proxy, method, args) -> handleDataSourceMethod(method.getName(), method.getReturnType(), hasResultSetRow, lowerCaseTableNames));
    }
    
    private static DataSource mockFailingDataSource() throws SQLException {
        return (DataSource) Proxy.newProxyInstance(DataSource.class.getClassLoader(), new Class[]{DataSource.class},
                (proxy, method, args) -> handleFailingDataSourceMethod(method.getName(), method.getReturnType()));
    }
    
    private static Connection createConnection(final boolean hasResultSetRow, final int lowerCaseTableNames) {
        return (Connection) Proxy.newProxyInstance(Connection.class.getClassLoader(), new Class[]{Connection.class},
                (proxy, method, args) -> handleConnectionMethod(method.getName(), method.getReturnType(), hasResultSetRow, lowerCaseTableNames));
    }
    
    private static PreparedStatement createPreparedStatement(final boolean hasResultSetRow, final int lowerCaseTableNames) {
        return (PreparedStatement) Proxy.newProxyInstance(PreparedStatement.class.getClassLoader(), new Class[]{PreparedStatement.class},
                (proxy, method, args) -> handlePreparedStatementMethod(method.getName(), method.getReturnType(), hasResultSetRow, lowerCaseTableNames));
    }
    
    private static ResultSet createResultSet(final boolean hasResultSetRow, final int lowerCaseTableNames) {
        boolean[] nextInvoked = new boolean[1];
        return (ResultSet) Proxy.newProxyInstance(ResultSet.class.getClassLoader(), new Class[]{ResultSet.class},
                (proxy, method, args) -> handleResultSetMethod(method.getName(), method.getReturnType(), hasResultSetRow, lowerCaseTableNames, nextInvoked));
    }
    
    private static Object handleDataSourceMethod(final String methodName, final Class<?> returnType, final boolean hasResultSetRow,
                                                 final int lowerCaseTableNames) {
        switch (methodName) {
            case "getConnection":
                return createConnection(hasResultSetRow, lowerCaseTableNames);
            case "unwrap":
                return null;
            case "isWrapperFor":
                return false;
            default:
                return getDefaultValue(returnType);
        }
    }
    
    private static Object handleFailingDataSourceMethod(final String methodName, final Class<?> returnType) throws SQLException {
        if ("getConnection".equals(methodName)) {
            throw new SQLException("expected");
        }
        return "isWrapperFor".equals(methodName) ? false : getDefaultValue(returnType);
    }
    
    private static Object handleConnectionMethod(final String methodName, final Class<?> returnType, final boolean hasResultSetRow, final int lowerCaseTableNames) {
        switch (methodName) {
            case "prepareStatement":
                return createPreparedStatement(hasResultSetRow, lowerCaseTableNames);
            case "close":
                return null;
            case "isWrapperFor":
                return false;
            default:
                return getDefaultValue(returnType);
        }
    }
    
    private static Object handlePreparedStatementMethod(final String methodName, final Class<?> returnType,
                                                        final boolean hasResultSetRow, final int lowerCaseTableNames) {
        switch (methodName) {
            case "executeQuery":
                return createResultSet(hasResultSetRow, lowerCaseTableNames);
            case "close":
                return null;
            case "isWrapperFor":
                return false;
            default:
                return getDefaultValue(returnType);
        }
    }
    
    private static Object handleResultSetMethod(final String methodName, final Class<?> returnType, final boolean hasResultSetRow,
                                                final int lowerCaseTableNames, final boolean[] nextInvoked) {
        switch (methodName) {
            case "next":
                boolean result = hasResultSetRow && !nextInvoked[0];
                nextInvoked[0] = true;
                return result;
            case "getInt":
                return lowerCaseTableNames;
            case "close":
                return null;
            case "isWrapperFor":
                return false;
            default:
                return getDefaultValue(returnType);
        }
    }
    
    private static Object getDefaultValue(final Class<?> returnType) {
        if (!returnType.isPrimitive()) {
            return null;
        }
        if (boolean.class == returnType) {
            return false;
        }
        if (byte.class == returnType) {
            return (byte) 0;
        }
        if (short.class == returnType) {
            return (short) 0;
        }
        if (int.class == returnType) {
            return 0;
        }
        if (long.class == returnType) {
            return 0L;
        }
        if (float.class == returnType) {
            return 0F;
        }
        if (double.class == returnType) {
            return 0D;
        }
        if (char.class == returnType) {
            return '\0';
        }
        return null;
    }
}
