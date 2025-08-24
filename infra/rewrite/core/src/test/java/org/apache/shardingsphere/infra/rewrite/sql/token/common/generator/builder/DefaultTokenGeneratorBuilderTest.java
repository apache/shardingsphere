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

package org.apache.shardingsphere.infra.rewrite.sql.token.common.generator.builder;

import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.type.CommonSQLStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.type.dml.SelectStatementContext;
import org.apache.shardingsphere.infra.rewrite.sql.token.common.generator.SQLTokenGenerator;
import org.apache.shardingsphere.infra.rewrite.sql.token.common.generator.generic.RemoveTokenGenerator;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.Iterator;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.isA;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DefaultTokenGeneratorBuilderTest {
    
    @Test
    void assertGetSQLTokenGeneratorsWithCommonSQLStatementContext() {
        assertGetSQLTokenGenerators(mock(CommonSQLStatementContext.class, RETURNS_DEEP_STUBS));
    }
    
    @Test
    void assertGetSQLTokenGeneratorsWithSelect() {
        SelectStatementContext sqlStatementContext = mock(SelectStatementContext.class, RETURNS_DEEP_STUBS);
        when(sqlStatementContext.getTablesContext().getDatabaseName().isPresent()).thenReturn(true);
        assertGetSQLTokenGenerators(sqlStatementContext);
    }
    
    @Test
    void assertGetSQLTokenGeneratorsWithShowColumns() {
        SQLStatementContext sqlStatementContext = mock(SQLStatementContext.class, RETURNS_DEEP_STUBS);
        when(sqlStatementContext.getTablesContext().getDatabaseName().isPresent()).thenReturn(true);
        assertGetSQLTokenGenerators(sqlStatementContext);
    }
    
    private void assertGetSQLTokenGenerators(final SQLStatementContext sqlStatementContext) {
        DefaultTokenGeneratorBuilder defaultTokenGeneratorBuilder = new DefaultTokenGeneratorBuilder(sqlStatementContext);
        Collection<SQLTokenGenerator> sqlTokenGenerators = defaultTokenGeneratorBuilder.getSQLTokenGenerators();
        assertThat(sqlTokenGenerators.size(), is(1));
        Iterator<SQLTokenGenerator> iterator = sqlTokenGenerators.iterator();
        SQLTokenGenerator removeTokenGenerator = iterator.next();
        assertThat(removeTokenGenerator, isA(RemoveTokenGenerator.class));
    }
}
