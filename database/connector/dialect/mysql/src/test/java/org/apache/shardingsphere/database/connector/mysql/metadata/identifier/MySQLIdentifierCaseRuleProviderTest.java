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
import org.apache.shardingsphere.database.connector.core.metadata.identifier.IdentifierCaseRuleSet;
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
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isA;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MySQLIdentifierCaseRuleProviderTest {
    
    private final DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "MySQL");
    
    private final IdentifierCaseRuleProvider provider = DatabaseTypedSPILoader.getService(IdentifierCaseRuleProvider.class, databaseType);
    
    @Test
    void assertGetDatabaseType() {
        assertThat(provider, isA(MySQLIdentifierCaseRuleProvider.class));
        assertThat(provider.getDatabaseType(), is("MySQL"));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("provideInsensitiveArguments")
    void assertProvideWithInsensitiveMode(final String name, final int lowerCaseTableNames) throws SQLException {
        IdentifierCaseRule actual =
                provider.provide(new IdentifierCaseRuleProviderContext(databaseType, createDataSource(lowerCaseTableNames))).orElseThrow(AssertionError::new).getRule(IdentifierScope.TABLE);
        assertThat(actual.getLookupMode(QuoteCharacter.NONE), is(LookupMode.NORMALIZED));
        assertThat(actual.getLookupMode(QuoteCharacter.BACK_QUOTE), is(LookupMode.NORMALIZED));
        assertTrue(actual.matches("Foo", "foo", QuoteCharacter.NONE));
    }
    
    @Test
    void assertProvideWithoutDataSource() {
        assertFalse(provider.provide(new IdentifierCaseRuleProviderContext(databaseType, null)).isPresent());
    }
    
    @Test
    void assertProvideWithSensitiveMode() throws SQLException {
        Optional<IdentifierCaseRuleSet> actual = provider.provide(new IdentifierCaseRuleProviderContext(databaseType, createDataSource(0)));
        assertFalse(actual.isPresent());
    }
    
    @Test
    void assertProvideWithSQLException() throws SQLException {
        Optional<IdentifierCaseRuleSet> actual = provider.provide(new IdentifierCaseRuleProviderContext(databaseType, createFailingDataSource()));
        assertFalse(actual.isPresent());
    }
    
    private DataSource createDataSource(final int lowerCaseTableNames) {
        return (DataSource) Proxy.newProxyInstance(getClass().getClassLoader(), new Class[]{DataSource.class}, (proxy, method, args) -> {
            if ("getConnection".equals(method.getName())) {
                return createConnection(lowerCaseTableNames);
            }
            return getDefaultValue(method.getReturnType());
        });
    }
    
    private DataSource createFailingDataSource() {
        return (DataSource) Proxy.newProxyInstance(getClass().getClassLoader(), new Class[]{DataSource.class}, (proxy, method, args) -> {
            if ("getConnection".equals(method.getName())) {
                throw new SQLException("mocked getConnection failure");
            }
            return getDefaultValue(method.getReturnType());
        });
    }
    
    private Connection createConnection(final int lowerCaseTableNames) {
        return (Connection) Proxy.newProxyInstance(getClass().getClassLoader(), new Class[]{Connection.class}, (proxy, method, args) -> {
            if ("prepareStatement".equals(method.getName())) {
                return createPreparedStatement(lowerCaseTableNames);
            }
            return getDefaultValue(method.getReturnType());
        });
    }
    
    private PreparedStatement createPreparedStatement(final int lowerCaseTableNames) {
        return (PreparedStatement) Proxy.newProxyInstance(getClass().getClassLoader(), new Class[]{PreparedStatement.class}, (proxy, method, args) -> {
            if ("executeQuery".equals(method.getName())) {
                return createResultSet(lowerCaseTableNames);
            }
            return getDefaultValue(method.getReturnType());
        });
    }
    
    private ResultSet createResultSet(final int lowerCaseTableNames) {
        AtomicBoolean next = new AtomicBoolean(true);
        return (ResultSet) Proxy.newProxyInstance(getClass().getClassLoader(), new Class[]{ResultSet.class}, (proxy, method, args) -> {
            if ("next".equals(method.getName())) {
                return next.getAndSet(false);
            }
            if ("getInt".equals(method.getName())) {
                return lowerCaseTableNames;
            }
            return getDefaultValue(method.getReturnType());
        });
    }
    
    private Object getDefaultValue(final Class<?> returnType) {
        if (Void.TYPE == returnType) {
            return null;
        }
        if (Boolean.TYPE == returnType) {
            return false;
        }
        if (Integer.TYPE == returnType) {
            return 0;
        }
        if (Long.TYPE == returnType) {
            return 0L;
        }
        if (Double.TYPE == returnType) {
            return 0D;
        }
        if (Float.TYPE == returnType) {
            return 0F;
        }
        if (Short.TYPE == returnType) {
            return (short) 0;
        }
        if (Byte.TYPE == returnType) {
            return (byte) 0;
        }
        if (Character.TYPE == returnType) {
            return (char) 0;
        }
        return null;
    }
    
    private static Stream<Arguments> provideInsensitiveArguments() {
        return Stream.of(Arguments.of("lower_case_table_names=1", 1), Arguments.of("lower_case_table_names=2", 2));
    }
}
