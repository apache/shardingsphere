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

package org.apache.shardingsphere.encrypt.rewrite.parameter;

import org.apache.shardingsphere.encrypt.rewrite.parameter.rewriter.EncryptPredicateParameterRewriter;
import org.apache.shardingsphere.encrypt.rule.EncryptRule;
import org.apache.shardingsphere.infra.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.statement.dml.SelectStatementContext;
import org.apache.shardingsphere.infra.database.DefaultSchema;
import org.apache.shardingsphere.infra.metadata.schema.ShardingSphereSchema;
import org.apache.shardingsphere.infra.rewrite.parameter.rewriter.ParameterRewriter;
import org.junit.Test;

import java.util.Collection;
import java.util.Collections;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class EncryptParameterRewriterBuilderTest {
    
    @SuppressWarnings("rawtypes")
    @Test
    public void assertGetParameterRewritersWhenPredicateIsNeedRewrite() {
        EncryptRule encryptRule = mock(EncryptRule.class, RETURNS_DEEP_STUBS);
        when(encryptRule.isQueryWithCipherColumn()).thenReturn(true);
        when(encryptRule.findEncryptTable("t_order").isPresent()).thenReturn(true);
        ShardingSphereSchema shardingSphereSchema = mock(ShardingSphereSchema.class);
        SQLStatementContext<?> sqlStatementContext = mock(SelectStatementContext.class, RETURNS_DEEP_STUBS);
        when(sqlStatementContext.getTablesContext().getTableNames()).thenReturn(Collections.singletonList("t_order"));
        Collection<ParameterRewriter> actual = new EncryptParameterRewriterBuilder(
                encryptRule, DefaultSchema.LOGIC_NAME, shardingSphereSchema, sqlStatementContext, Collections.emptyList()).getParameterRewriters();
        assertThat(actual.size(), is(1));
        ParameterRewriter parameterRewriter = actual.iterator().next();
        assertThat(parameterRewriter, instanceOf(EncryptPredicateParameterRewriter.class));
    }
    
    @SuppressWarnings("rawtypes")
    @Test
    public void assertGetParameterRewritersWhenPredicateIsNotNeedRewrite() {
        EncryptRule encryptRule = mock(EncryptRule.class, RETURNS_DEEP_STUBS);
        when(encryptRule.isQueryWithCipherColumn()).thenReturn(true);
        ShardingSphereSchema shardingSphereSchema = mock(ShardingSphereSchema.class);
        SelectStatementContext sqlStatementContext = mock(SelectStatementContext.class, RETURNS_DEEP_STUBS);
        when(sqlStatementContext.getTablesContext().getTableNames()).thenReturn(Collections.singletonList("t_order"));
        when(sqlStatementContext.getWhereSegments()).thenReturn(Collections.emptyList());
        Collection<ParameterRewriter> actual = new EncryptParameterRewriterBuilder(
                encryptRule, DefaultSchema.LOGIC_NAME, shardingSphereSchema, sqlStatementContext, Collections.emptyList()).getParameterRewriters();
        assertThat(actual.size(), is(0));
    }
}
