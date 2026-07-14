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
import org.apache.shardingsphere.database.connector.core.metadata.identifier.IdentifierCasePolicySet;
import org.apache.shardingsphere.database.connector.core.metadata.identifier.IdentifierScope;
import org.apache.shardingsphere.database.connector.core.metadata.identifier.LookupMode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MySQLIdentifierCasePolicyLoaderTest {
    
    private final MySQLIdentifierCasePolicyLoader loader = new MySQLIdentifierCasePolicyLoader();
    
    @Mock
    private Connection connection;
    
    @Mock
    private PreparedStatement preparedStatement;
    
    @Mock
    private ResultSet resultSet;
    
    @BeforeEach
    void setUp() throws SQLException {
        when(connection.prepareStatement("SELECT @@lower_case_table_names")).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
    }
    
    @Test
    void assertLoadSensitiveTableNames() throws SQLException {
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getInt(1)).thenReturn(0);
        IdentifierCasePolicySet actual = loader.load(connection);
        assertTrue(actual.getPolicy(IdentifierScope.SCHEMA).matches("foo_schema", "FOO_SCHEMA", QuoteCharacter.NONE));
        assertFalse(actual.getPolicy(IdentifierScope.TABLE).matches("foo_table", "FOO_TABLE", QuoteCharacter.NONE));
        assertFalse(actual.getPolicy(IdentifierScope.VIEW).matches("foo_view", "FOO_VIEW", QuoteCharacter.NONE));
        assertTrue(actual.getPolicy(IdentifierScope.COLUMN).matches("foo_column", "FOO_COLUMN", QuoteCharacter.NONE));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("insensitiveTableNameArguments")
    void assertLoadInsensitiveTableNames(final String name, final int lowerCaseTableNames) throws SQLException {
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getInt(1)).thenReturn(lowerCaseTableNames);
        IdentifierCasePolicySet actual = loader.load(connection);
        assertThat(actual.getPolicy(IdentifierScope.TABLE).getLookupMode(QuoteCharacter.NONE), is(LookupMode.NORMALIZED));
        assertThat(actual.getPolicy(IdentifierScope.TABLE).getLookupMode(QuoteCharacter.BACK_QUOTE), is(LookupMode.NORMALIZED));
    }
    
    @Test
    void assertLoadWithoutResult() throws SQLException {
        when(resultSet.next()).thenReturn(false);
        assertThrows(SQLException.class, () -> loader.load(connection));
    }
    
    @Test
    void assertLoadWithUnsupportedValue() throws SQLException {
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getInt(1)).thenReturn(3);
        assertThrows(SQLException.class, () -> loader.load(connection));
    }
    
    @Test
    void assertLoadWithSQLException() throws SQLException {
        SQLException expected = new SQLException("expected");
        when(preparedStatement.executeQuery()).thenThrow(expected);
        assertThat(assertThrows(SQLException.class, () -> loader.load(connection)), is(expected));
    }
    
    private static Stream<Arguments> insensitiveTableNameArguments() {
        return Stream.of(Arguments.of("lower_case_table_names_1", 1), Arguments.of("lower_case_table_names_2", 2));
    }
}
