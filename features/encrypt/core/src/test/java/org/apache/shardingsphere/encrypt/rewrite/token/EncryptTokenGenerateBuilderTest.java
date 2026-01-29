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

package org.apache.shardingsphere.encrypt.rewrite.token;

import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.encrypt.rewrite.condition.EncryptCondition;
import org.apache.shardingsphere.encrypt.rewrite.token.generator.predicate.EncryptPredicateColumnTokenGenerator;
import org.apache.shardingsphere.encrypt.rewrite.token.generator.predicate.EncryptPredicateValueTokenGenerator;
import org.apache.shardingsphere.encrypt.rewrite.token.generator.projection.EncryptSelectProjectionTokenGenerator;
import org.apache.shardingsphere.encrypt.rule.EncryptRule;
import org.apache.shardingsphere.infra.binder.context.segment.select.orderby.OrderByItem;
import org.apache.shardingsphere.infra.binder.context.statement.type.dml.SelectStatementContext;
import org.apache.shardingsphere.infra.rewrite.context.SQLRewriteContext;
import org.apache.shardingsphere.infra.rewrite.sql.token.common.generator.SQLTokenGenerator;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.SelectStatement;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.isA;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EncryptTokenGenerateBuilderTest {
    
    private final DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "FIXTURE");
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private EncryptRule rule;
    
    @Test
    void assertGetSQLTokenGenerators() {
        SelectStatementContext selectStatementContext = mock(SelectStatementContext.class, RETURNS_DEEP_STUBS);
        when(selectStatementContext.getTablesContext().getSimpleTables().isEmpty()).thenReturn(false);
        when(selectStatementContext.getTablesContext().getTableNames()).thenReturn(Collections.singleton("table"));
        when(selectStatementContext.getOrderByContext().getItems()).thenReturn(Collections.singleton(mock(OrderByItem.class)));
        when(selectStatementContext.getGroupByContext().getItems()).thenReturn(Collections.emptyList());
        when(selectStatementContext.getWhereSegments()).thenReturn(Collections.emptyList());
        when(selectStatementContext.getSqlStatement()).thenReturn(new SelectStatement(databaseType));
        SQLRewriteContext sqlRewriteContext = mock(SQLRewriteContext.class, RETURNS_DEEP_STUBS);
        EncryptCondition encryptCondition = mock(EncryptCondition.class);
        EncryptTokenGenerateBuilder encryptTokenGenerateBuilder = new EncryptTokenGenerateBuilder(selectStatementContext, Collections.singleton(encryptCondition), rule, sqlRewriteContext);
        Collection<SQLTokenGenerator> sqlTokenGenerators = encryptTokenGenerateBuilder.getSQLTokenGenerators();
        assertThat(sqlTokenGenerators.size(), is(3));
        Iterator<SQLTokenGenerator> iterator = sqlTokenGenerators.iterator();
        SQLTokenGenerator item1 = iterator.next();
        assertThat(item1, isA(EncryptSelectProjectionTokenGenerator.class));
        SQLTokenGenerator item2 = iterator.next();
        assertThat(item2, isA(EncryptPredicateColumnTokenGenerator.class));
        SQLTokenGenerator item3 = iterator.next();
        assertThat(item3, isA(EncryptPredicateValueTokenGenerator.class));
    }
}
