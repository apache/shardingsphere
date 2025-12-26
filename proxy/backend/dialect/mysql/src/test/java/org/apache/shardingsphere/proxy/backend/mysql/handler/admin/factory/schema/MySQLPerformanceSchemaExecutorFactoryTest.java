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

import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.binder.context.statement.type.dml.SelectStatementContext;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.proxy.backend.handler.admin.executor.DatabaseAdminExecutor;
import org.apache.shardingsphere.proxy.backend.handler.admin.executor.DatabaseMetaDataExecutor;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.TableNameSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.SelectStatement;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.isA;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class MySQLPerformanceSchemaExecutorFactoryTest {
    
    private final DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "MySQL");
    
    @Test
    void assertCreateMetaDataExecutorForSystemTable() {
        Optional<DatabaseAdminExecutor> actual = MySQLPerformanceSchemaExecutorFactory.newInstance(mockSelectStatementContext("variables_info"), "sql", Collections.emptyList());
        assertTrue(actual.isPresent());
        assertThat(actual.get(), isA(DatabaseMetaDataExecutor.class));
    }
    
    @Test
    void assertReturnEmptyForNonSystemTable() {
        assertFalse(MySQLPerformanceSchemaExecutorFactory.newInstance(mockSelectStatementContext("custom_table"), "sql", Collections.emptyList()).isPresent());
    }
    
    @Test
    void assertReturnEmptyWhenFromIsMissing() {
        assertFalse(MySQLPerformanceSchemaExecutorFactory.newInstance(mockSelectStatementContext(new SelectStatement(databaseType)), "sql", Collections.emptyList()).isPresent());
    }
    
    @Test
    void assertReturnEmptyWhenFromIsNotSimpleTableSegment() {
        SelectStatement selectStatement = new SelectStatement(databaseType);
        selectStatement.setFrom(mock(org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.TableSegment.class));
        assertFalse(MySQLPerformanceSchemaExecutorFactory.newInstance(mockSelectStatementContext(selectStatement), "sql", Collections.emptyList()).isPresent());
    }
    
    private SelectStatementContext mockSelectStatementContext(final String tableName) {
        SelectStatement selectStatement = new SelectStatement(databaseType);
        selectStatement.setFrom(new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue(tableName))));
        return mockSelectStatementContext(selectStatement);
    }
    
    private SelectStatementContext mockSelectStatementContext(final SelectStatement selectStatement) {
        SelectStatementContext result = mock(SelectStatementContext.class);
        when(result.getSqlStatement()).thenReturn(selectStatement);
        return result;
    }
}
