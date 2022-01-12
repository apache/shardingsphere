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

package org.apache.shardingsphere.proxy.backend.text.admin.postgresql;

import org.apache.shardingsphere.proxy.backend.text.admin.executor.DatabaseAdminExecutor;
import org.apache.shardingsphere.proxy.backend.text.admin.postgresql.executor.SelectDatabaseExecutor;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.TableNameSegment;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.SelectStatement;
import org.apache.shardingsphere.sql.parser.sql.common.value.identifier.IdentifierValue;
import org.junit.Test;

import java.util.Optional;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class PostgreSQLAdminExecutorFactoryTest {
    
    private final PostgreSQLAdminExecutorFactory postgreSQLAdminExecutorFactory = new PostgreSQLAdminExecutorFactory();
    
    @Test
    public void assertNewInstanceWithMySQLShowFunctionStatusStatement() {
        SelectStatement statement = mock(SelectStatement.class);
        SimpleTableSegment tableSegment = mock(SimpleTableSegment.class);
        when(tableSegment.getTableName()).thenReturn(new TableNameSegment(0, 0, new IdentifierValue("pg_database")));
        when(statement.getFrom()).thenReturn(tableSegment);
        Optional<DatabaseAdminExecutor> executorOptional = postgreSQLAdminExecutorFactory.newInstance(statement, "", Optional.empty());
        assertTrue(executorOptional.isPresent());
        assertThat(executorOptional.get(), instanceOf(SelectDatabaseExecutor.class));
    }
    
}
