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

package org.apache.shardingsphere.proxy.backend.mysql.handler.admin.factory.schema;

import lombok.SneakyThrows;
import org.apache.shardingsphere.infra.binder.context.statement.type.dml.SelectStatementContext;
import org.apache.shardingsphere.proxy.backend.handler.admin.executor.DatabaseAdminExecutor;
import org.apache.shardingsphere.proxy.backend.handler.admin.executor.DatabaseMetaDataExecutor;
import org.apache.shardingsphere.proxy.backend.mysql.handler.admin.executor.select.SelectInformationSchemataExecutor;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.TableNameSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.TableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.SelectStatement;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.internal.configuration.plugins.Plugins;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.isA;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class MySQLSystemSchemaQueryExecutorFactoryTest {
    
    private Collection<MySQLSpecialTableQueryExecutorFactory> originalFactories;
    
    @BeforeEach
    void setUp() {
        originalFactories = new LinkedHashSet<>(getSpecialTableExecutorFactories());
    }
    
    @AfterEach
    void resetFactories() {
        Collection<MySQLSpecialTableQueryExecutorFactory> factories = getSpecialTableExecutorFactories();
        factories.clear();
        factories.addAll(originalFactories);
    }
    
    @SuppressWarnings("unchecked")
    @SneakyThrows(ReflectiveOperationException.class)
    private Collection<MySQLSpecialTableQueryExecutorFactory> getSpecialTableExecutorFactories() {
        return (Collection<MySQLSpecialTableQueryExecutorFactory>) Plugins.getMemberAccessor().get(
                MySQLSystemSchemaQueryExecutorFactory.class.getDeclaredField("SPECIAL_TABLE_EXECUTOR_FACTORIES"), null);
    }
    
    @Test
    void assertNewInstanceWithoutFrom() {
        SelectStatement selectStatement = mock(SelectStatement.class);
        when(selectStatement.getFrom()).thenReturn(Optional.empty());
        SelectStatementContext selectStatementContext = mock(SelectStatementContext.class);
        when(selectStatementContext.getSqlStatement()).thenReturn(selectStatement);
        assertFalse(MySQLSystemSchemaQueryExecutorFactory.newInstance(selectStatementContext, "SELECT 1", Collections.emptyList(), "information_schema").isPresent());
    }
    
    @Test
    void assertNewInstanceWithNonSimpleFrom() {
        SelectStatement selectStatement = mock(SelectStatement.class);
        when(selectStatement.getFrom()).thenReturn(Optional.of(mock(TableSegment.class)));
        SelectStatementContext selectStatementContext = mock(SelectStatementContext.class);
        when(selectStatementContext.getSqlStatement()).thenReturn(selectStatement);
        assertFalse(MySQLSystemSchemaQueryExecutorFactory.newInstance(selectStatementContext, "SELECT 1", Collections.emptyList(), "information_schema").isPresent());
    }
    
    @Test
    void assertNewInstanceWithCreateSpecialTableExecutor() {
        SelectStatement selectStatement = mock(SelectStatement.class);
        when(selectStatement.getFrom()).thenReturn(Optional.of(new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("SCHEMATA")))));
        SelectStatementContext selectStatementContext = mock(SelectStatementContext.class);
        when(selectStatementContext.getSqlStatement()).thenReturn(selectStatement);
        Optional<DatabaseAdminExecutor> actual = MySQLSystemSchemaQueryExecutorFactory.newInstance(selectStatementContext, "SELECT * FROM information_schema.schemata", Collections.emptyList(), "information_schema");
        assertTrue(actual.isPresent());
        assertThat(actual.get(), isA(SelectInformationSchemataExecutor.class));
    }
    
    @Test
    void assertNewInstanceWithSpecialFactory() {
        Collection<MySQLSpecialTableQueryExecutorFactory> factories = getSpecialTableExecutorFactories();
        factories.clear();
        SelectStatementContext selectStatementContext = mock(SelectStatementContext.class);
        MySQLSpecialTableQueryExecutorFactory specialFactory = mock(MySQLSpecialTableQueryExecutorFactory.class);
        when(specialFactory.accept("custom_schema", "custom_table")).thenReturn(true);
        when(specialFactory.newInstance(selectStatementContext, "SELECT * FROM custom_schema.custom_table", Collections.emptyList(), "custom_table")).thenReturn(Optional.empty());
        factories.add(specialFactory);
        TableNameSegment tableNameSegment = new TableNameSegment(0, 0, new IdentifierValue("custom_table"));
        SimpleTableSegment tableSegment = new SimpleTableSegment(tableNameSegment);
        SelectStatement selectStatement = mock(SelectStatement.class);
        when(selectStatement.getFrom()).thenReturn(Optional.of(tableSegment));
        when(selectStatementContext.getSqlStatement()).thenReturn(selectStatement);
        assertFalse(MySQLSystemSchemaQueryExecutorFactory.newInstance(selectStatementContext, "SELECT * FROM custom_schema.custom_table", Collections.emptyList(), "custom_schema").isPresent());
    }
    
    @Test
    void assertNewInstanceWithCreateSystemTableExecutor() {
        SelectStatement selectStatement = mock(SelectStatement.class);
        when(selectStatement.getFrom()).thenReturn(Optional.of(new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("db")))));
        SelectStatementContext selectStatementContext = mock(SelectStatementContext.class);
        when(selectStatementContext.getSqlStatement()).thenReturn(selectStatement);
        Optional<DatabaseAdminExecutor> actual = MySQLSystemSchemaQueryExecutorFactory.newInstance(selectStatementContext, "SELECT * FROM mysql.db", Collections.emptyList(), "mysql");
        assertTrue(actual.isPresent());
        assertThat(actual.get(), isA(DatabaseMetaDataExecutor.class));
    }
}
