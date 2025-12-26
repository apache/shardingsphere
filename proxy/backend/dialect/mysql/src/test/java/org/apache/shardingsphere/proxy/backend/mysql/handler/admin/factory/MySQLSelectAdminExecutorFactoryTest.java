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

import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.binder.context.statement.type.dml.SelectStatementContext;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.proxy.backend.handler.admin.executor.DatabaseAdminExecutor;
import org.apache.shardingsphere.proxy.backend.mysql.handler.admin.factory.schema.MySQLSystemSchemaQueryExecutorFactory;
import org.apache.shardingsphere.proxy.backend.mysql.handler.admin.factory.withoutfrom.MySQLSelectWithoutFromAdminExecutorFactory;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ProjectionsSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.TableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.SelectStatement;
import org.apache.shardingsphere.test.infra.framework.extension.mock.AutoMockExtension;
import org.apache.shardingsphere.test.infra.framework.extension.mock.StaticMockSettings;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.Collections;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(AutoMockExtension.class)
@StaticMockSettings({MySQLSelectWithoutFromAdminExecutorFactory.class, MySQLSystemSchemaQueryExecutorFactory.class})
class MySQLSelectAdminExecutorFactoryTest {
    
    private final DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "MySQL");
    
    @Test
    void assertCreateWithoutFromUsesFactory() {
        SelectStatement selectStatement = new SelectStatement(databaseType);
        selectStatement.setProjections(new ProjectionsSegment(0, 0));
        SelectStatementContext selectStatementContext = mock(SelectStatementContext.class);
        when(selectStatementContext.getSqlStatement()).thenReturn(selectStatement);
        DatabaseAdminExecutor expected = mock(DatabaseAdminExecutor.class);
        when(MySQLSelectWithoutFromAdminExecutorFactory.newInstance(any(), any(), any(), any())).thenReturn(Optional.of(expected));
        Optional<DatabaseAdminExecutor> actual = MySQLSelectAdminExecutorFactory.newInstance(selectStatementContext, "", Collections.emptyList(), "db", mock(ShardingSphereMetaData.class));
        assertTrue(actual.isPresent());
        assertThat(actual.get(), is(expected));
    }
    
    @Test
    void assertCreateSystemSchemaExecutor() {
        SelectStatement selectStatement = new SelectStatement(databaseType);
        selectStatement.setProjections(new ProjectionsSegment(0, 0));
        selectStatement.setFrom(mock(TableSegment.class));
        SelectStatementContext selectStatementContext = mock(SelectStatementContext.class);
        when(selectStatementContext.getSqlStatement()).thenReturn(selectStatement);
        DatabaseAdminExecutor expected = mock(DatabaseAdminExecutor.class);
        when(MySQLSystemSchemaQueryExecutorFactory.newInstance(selectStatementContext, "", Collections.emptyList(), "mysql")).thenReturn(Optional.of(expected));
        Optional<DatabaseAdminExecutor> actual = MySQLSelectAdminExecutorFactory.newInstance(
                selectStatementContext, "", Collections.emptyList(), "mysql", mock(ShardingSphereMetaData.class, RETURNS_DEEP_STUBS));
        assertTrue(actual.isPresent());
        assertThat(actual.get(), is(expected));
    }
    
    @Test
    void assertSkipWhenSystemSchemaComplete() {
        SelectStatement selectStatement = new SelectStatement(databaseType);
        selectStatement.setProjections(new ProjectionsSegment(0, 0));
        selectStatement.setFrom(mock(TableSegment.class));
        SelectStatementContext selectStatementContext = mock(SelectStatementContext.class);
        when(selectStatementContext.getSqlStatement()).thenReturn(selectStatement);
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class);
        when(database.isComplete()).thenReturn(true);
        ShardingSphereMetaData metaData = mock(ShardingSphereMetaData.class);
        when(metaData.getDatabase("mysql")).thenReturn(database);
        assertFalse(MySQLSelectAdminExecutorFactory.newInstance(selectStatementContext, "", Collections.emptyList(), "mysql", metaData).isPresent());
    }
    
    @Test
    void assertSkipWhenNonSystemSchema() {
        SelectStatement selectStatement = new SelectStatement(databaseType);
        selectStatement.setProjections(new ProjectionsSegment(0, 0));
        selectStatement.setFrom(mock(TableSegment.class));
        SelectStatementContext selectStatementContext = mock(SelectStatementContext.class);
        when(selectStatementContext.getSqlStatement()).thenReturn(selectStatement);
        assertFalse(MySQLSelectAdminExecutorFactory.newInstance(selectStatementContext, "", Collections.emptyList(), "test_db", mock()).isPresent());
    }
}
