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

package org.apache.shardingsphere.proxy.backend.mysql.handler.admin.factory;

import org.apache.shardingsphere.proxy.backend.handler.admin.executor.DatabaseAdminExecutor;
import org.apache.shardingsphere.proxy.backend.mysql.handler.admin.executor.show.MySQLShowCreateDatabaseExecutor;
import org.apache.shardingsphere.proxy.backend.mysql.handler.admin.executor.show.MySQLShowDatabasesExecutor;
import org.apache.shardingsphere.proxy.backend.mysql.handler.admin.executor.show.MySQLShowFunctionStatusExecutor;
import org.apache.shardingsphere.proxy.backend.mysql.handler.admin.executor.show.MySQLShowProcedureStatusExecutor;
import org.apache.shardingsphere.proxy.backend.mysql.handler.admin.executor.show.MySQLShowProcessListExecutor;
import org.apache.shardingsphere.proxy.backend.mysql.handler.admin.executor.show.MySQLShowTablesExecutor;
import org.apache.shardingsphere.sql.parser.statement.core.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.statement.mysql.dal.show.database.MySQLShowCreateDatabaseStatement;
import org.apache.shardingsphere.sql.parser.statement.mysql.dal.show.database.MySQLShowDatabasesStatement;
import org.apache.shardingsphere.sql.parser.statement.mysql.dal.show.function.MySQLShowFunctionStatusStatement;
import org.apache.shardingsphere.sql.parser.statement.mysql.dal.show.procedure.MySQLShowProcedureStatusStatement;
import org.apache.shardingsphere.sql.parser.statement.mysql.dal.show.process.MySQLShowProcessListStatement;
import org.apache.shardingsphere.sql.parser.statement.mysql.dal.show.table.MySQLShowTablesStatement;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Optional;
import java.util.stream.Stream;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

class MySQLShowAdminExecutorFactoryTest {
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("statementProvider")
    void assertCreateExecutor(final String caseName, final SQLStatement statement, final Class<? extends DatabaseAdminExecutor> expectedType) {
        Optional<DatabaseAdminExecutor> actual = MySQLShowAdminExecutorFactory.newInstance(statement);
        assertTrue(actual.isPresent(), caseName);
        assertThat(actual.get(), instanceOf(expectedType));
    }
    
    @Test
    void assertUnsupportedStatement() {
        assertFalse(MySQLShowAdminExecutorFactory.newInstance(mock(SQLStatement.class)).isPresent());
    }
    
    private static Stream<Arguments> statementProvider() {
        return Stream.of(
                Arguments.of("show databases", mock(MySQLShowDatabasesStatement.class), MySQLShowDatabasesExecutor.class),
                Arguments.of("show tables", mock(MySQLShowTablesStatement.class), MySQLShowTablesExecutor.class),
                Arguments.of("show create database", mock(MySQLShowCreateDatabaseStatement.class), MySQLShowCreateDatabaseExecutor.class),
                Arguments.of("show function status", mock(MySQLShowFunctionStatusStatement.class), MySQLShowFunctionStatusExecutor.class),
                Arguments.of("show procedure status", mock(MySQLShowProcedureStatusStatement.class), MySQLShowProcedureStatusExecutor.class),
                Arguments.of("show processlist", mock(MySQLShowProcessListStatement.class), MySQLShowProcessListExecutor.class));
    }
}
