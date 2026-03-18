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

package org.apache.shardingsphere.proxy.backend.mysql.handler.admin.factory.schema.type;

import org.apache.shardingsphere.infra.binder.context.statement.type.dml.SelectStatementContext;
import org.apache.shardingsphere.proxy.backend.handler.admin.executor.DatabaseAdminExecutor;
import org.apache.shardingsphere.proxy.backend.mysql.handler.admin.executor.select.SelectInformationSchemataExecutor;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.SelectStatement;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.isA;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class MySQLSchemataQueryExecutorFactoryTest {
    
    @Test
    void assertAccept() {
        MySQLSchemataQueryExecutorFactory factory = new MySQLSchemataQueryExecutorFactory();
        assertTrue(factory.accept("information_schema", "schemata"));
        assertFalse(factory.accept("other_schema", "schemata"));
    }
    
    @Test
    void assertNewInstance() {
        MySQLSchemataQueryExecutorFactory factory = new MySQLSchemataQueryExecutorFactory();
        SelectStatementContext selectStatementContext = mock(SelectStatementContext.class);
        when(selectStatementContext.getSqlStatement()).thenReturn(mock(SelectStatement.class));
        Optional<DatabaseAdminExecutor> actual = factory.newInstance(selectStatementContext, "SELECT * FROM information_schema.schemata", Collections.emptyList(), "SCHEMATA");
        assertTrue(actual.isPresent());
        assertThat(actual.get(), isA(SelectInformationSchemataExecutor.class));
    }
}
