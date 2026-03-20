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

package org.apache.shardingsphere.distsql.handler.engine.update.rdl.rule.engine.database;

import org.apache.shardingsphere.distsql.handler.engine.update.rdl.rule.engine.database.type.AlterDatabaseRuleOperator;
import org.apache.shardingsphere.distsql.handler.engine.update.rdl.rule.engine.database.type.CreateDatabaseRuleOperator;
import org.apache.shardingsphere.distsql.handler.engine.update.rdl.rule.engine.database.type.DropDatabaseRuleOperator;
import org.apache.shardingsphere.distsql.handler.engine.update.rdl.rule.spi.database.DatabaseRuleDefinitionExecutor;
import org.apache.shardingsphere.distsql.handler.engine.update.rdl.rule.spi.database.type.DatabaseRuleAlterExecutor;
import org.apache.shardingsphere.distsql.handler.engine.update.rdl.rule.spi.database.type.DatabaseRuleCreateExecutor;
import org.apache.shardingsphere.distsql.handler.engine.update.rdl.rule.spi.database.type.DatabaseRuleDropExecutor;
import org.apache.shardingsphere.infra.exception.generic.UnsupportedSQLOperationException;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.isA;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

class DatabaseRuleOperatorFactoryTest {
    
    private final ContextManager contextManager = mock(ContextManager.class);
    
    @SuppressWarnings("rawtypes")
    @ParameterizedTest(name = "{0}")
    @MethodSource("assertNewInstanceWithSupportedExecutorArguments")
    void assertNewInstanceWithSupportedExecutor(final String name, final DatabaseRuleDefinitionExecutor executor, final Class expectedOperatorType) {
        assertThat((Object) DatabaseRuleOperatorFactory.newInstance(contextManager, executor), isA(expectedOperatorType));
    }
    
    @SuppressWarnings("rawtypes")
    @Test
    void assertNewInstanceWithUnsupportedExecutor() {
        DatabaseRuleDefinitionExecutor executor = mock(DatabaseRuleDefinitionExecutor.class);
        String expectedMessage = String.format("Unsupported SQL operation: Cannot support RDL executor type `%s`.", executor.getClass().getName());
        assertThat(assertThrows(UnsupportedSQLOperationException.class, () -> DatabaseRuleOperatorFactory.newInstance(contextManager, executor)).getMessage(), CoreMatchers.is(expectedMessage));
    }
    
    private static Stream<Arguments> assertNewInstanceWithSupportedExecutorArguments() {
        return Stream.of(
                Arguments.of("create executor returns create operator", mock(DatabaseRuleCreateExecutor.class), CreateDatabaseRuleOperator.class),
                Arguments.of("alter executor returns alter operator", mock(DatabaseRuleAlterExecutor.class), AlterDatabaseRuleOperator.class),
                Arguments.of("drop executor returns drop operator", mock(DatabaseRuleDropExecutor.class), DropDatabaseRuleOperator.class));
    }
}
