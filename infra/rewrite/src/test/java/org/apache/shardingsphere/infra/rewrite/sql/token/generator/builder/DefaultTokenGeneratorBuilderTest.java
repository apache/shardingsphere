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

package org.apache.shardingsphere.infra.rewrite.sql.token.generator.builder;

import org.apache.shardingsphere.infra.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.statement.dal.ShowColumnsStatementContext;
import org.apache.shardingsphere.infra.binder.statement.dal.ShowTableStatusStatementContext;
import org.apache.shardingsphere.infra.binder.statement.dml.SelectStatementContext;
import org.apache.shardingsphere.infra.rewrite.sql.token.generator.SQLTokenGenerator;
import org.apache.shardingsphere.infra.rewrite.sql.token.generator.generic.RemoveTokenGenerator;
import org.junit.Test;

import java.util.Collection;
import java.util.Iterator;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class DefaultTokenGeneratorBuilderTest {
    
    @Test
    public void assertGetSQLTokenGeneratorsWithShowTableStatus() {
        ShowTableStatusStatementContext sqlStatementContext = mock(ShowTableStatusStatementContext.class, RETURNS_DEEP_STUBS);
        when(sqlStatementContext.getRemoveSegments().isEmpty()).thenReturn(false);
        assertGetSQLTokenGenerators(sqlStatementContext);
    }
    
    @Test
    public void assertGetSQLTokenGeneratorsWithSelect() {
        SelectStatementContext sqlStatementContext = mock(SelectStatementContext.class, RETURNS_DEEP_STUBS);
        when(sqlStatementContext.getTablesContext().getDatabaseName().isPresent()).thenReturn(true);
        assertGetSQLTokenGenerators(sqlStatementContext);
    }
    
    @Test
    public void assertGetSQLTokenGeneratorsWithShowColumns() {
        ShowColumnsStatementContext sqlStatementContext = mock(ShowColumnsStatementContext.class, RETURNS_DEEP_STUBS);
        when(sqlStatementContext.getRemoveSegments().isEmpty()).thenReturn(false);
        when(sqlStatementContext.getTablesContext().getDatabaseName().isPresent()).thenReturn(true);
        assertGetSQLTokenGenerators(sqlStatementContext);
    }
    
    private void assertGetSQLTokenGenerators(final SQLStatementContext sqlStatementContext) {
        DefaultTokenGeneratorBuilder defaultTokenGeneratorBuilder = new DefaultTokenGeneratorBuilder(sqlStatementContext);
        Collection<SQLTokenGenerator> sqlTokenGenerators = defaultTokenGeneratorBuilder.getSQLTokenGenerators();
        assertThat(sqlTokenGenerators.size(), is(1));
        Iterator<SQLTokenGenerator> iterator = sqlTokenGenerators.iterator();
        SQLTokenGenerator removeTokenGenerator = iterator.next();
        assertThat(removeTokenGenerator, instanceOf(RemoveTokenGenerator.class));
    }
}
