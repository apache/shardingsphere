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

package org.apache.shardingsphere.proxy.backend.firebird.handler.admin;

import org.apache.shardingsphere.database.connector.core.spi.DatabaseTypedSPILoader;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.type.CommonSQLStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.type.dml.DeleteStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.type.dml.SelectStatementContext;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.proxy.backend.firebird.handler.admin.executor.FirebirdSetVariableAdminExecutor;
import org.apache.shardingsphere.proxy.backend.firebird.handler.admin.executor.FirebirdShowVariableExecutor;
import org.apache.shardingsphere.proxy.backend.handler.admin.executor.DatabaseAdminExecutor;
import org.apache.shardingsphere.proxy.backend.handler.admin.executor.DatabaseAdminExecutorCreator;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dal.SetStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dal.ShowStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.DeleteStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.SelectStatement;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Collections;
import java.util.Optional;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.isA;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class FirebirdAdminExecutorCreatorTest {
    
    private static final DatabaseType DATABASE_TYPE = TypedSPILoader.getService(DatabaseType.class, "Firebird");
    
    private final DatabaseAdminExecutorCreator creator = DatabaseTypedSPILoader.getService(DatabaseAdminExecutorCreator.class, DATABASE_TYPE);
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("createArguments")
    void assertCreate(final String name, final SQLStatementContext sqlStatementContext, final String sql, final Class<? extends DatabaseAdminExecutor> expectedExecutorType) {
        final Optional<DatabaseAdminExecutor> actual = creator.create(sqlStatementContext, sql, "", Collections.emptyList());
        if (null == expectedExecutorType) {
            assertFalse(actual.isPresent(), name);
        } else {
            assertTrue(actual.isPresent(), name);
            assertThat(actual.get(), isA(expectedExecutorType));
        }
    }
    
    private static Stream<Arguments> createArguments() {
        return Stream.of(
                Arguments.of("select statement returns empty", createSelectStatementContext(), "SELECT 1", null),
                Arguments.of("set statement returns set executor", createSetStatementContext(), "SET NAMES utf8", FirebirdSetVariableAdminExecutor.class),
                Arguments.of("show statement returns show executor", createShowStatementContext(), "SHOW server_version", FirebirdShowVariableExecutor.class),
                Arguments.of("delete statement returns empty", createOtherStatementContext(), "DELETE FROM t WHERE id = 1", null));
    }
    
    private static SQLStatementContext createSetStatementContext() {
        SetStatement sqlStatement = new SetStatement(DATABASE_TYPE, Collections.emptyList());
        sqlStatement.buildAttributes();
        return new CommonSQLStatementContext(sqlStatement);
    }
    
    private static SQLStatementContext createShowStatementContext() {
        ShowStatement sqlStatement = new ShowStatement(DATABASE_TYPE, "server_version");
        sqlStatement.buildAttributes();
        return new CommonSQLStatementContext(sqlStatement);
    }
    
    private static SQLStatementContext createSelectStatementContext() {
        SelectStatementContext result = mock(SelectStatementContext.class);
        when(result.getSqlStatement()).thenReturn(new SelectStatement(DATABASE_TYPE));
        return result;
    }
    
    private static SQLStatementContext createOtherStatementContext() {
        return new DeleteStatementContext(new DeleteStatement(DATABASE_TYPE));
    }
}
