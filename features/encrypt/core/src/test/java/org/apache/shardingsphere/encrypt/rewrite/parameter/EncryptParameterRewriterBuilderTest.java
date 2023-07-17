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
import org.apache.shardingsphere.infra.database.core.DefaultDatabase;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.rewrite.parameter.rewriter.ParameterRewriter;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.Collections;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class EncryptParameterRewriterBuilderTest {
    
    @Test
    void assertGetParameterRewritersWhenPredicateIsNeedRewrite() {
        EncryptRule encryptRule = mock(EncryptRule.class, RETURNS_DEEP_STUBS);
        when(encryptRule.findEncryptTable("t_order").isPresent()).thenReturn(true);
        SQLStatementContext sqlStatementContext = mock(SelectStatementContext.class, RETURNS_DEEP_STUBS);
        when(sqlStatementContext.getTablesContext().getTableNames()).thenReturn(Collections.singletonList("t_order"));
        Collection<ParameterRewriter> actual = new EncryptParameterRewriterBuilder(
                encryptRule, DefaultDatabase.LOGIC_NAME, Collections.singletonMap("test", mock(ShardingSphereSchema.class)), sqlStatementContext, Collections.emptyList()).getParameterRewriters();
        assertThat(actual.size(), is(1));
        assertThat(actual.iterator().next(), instanceOf(EncryptPredicateParameterRewriter.class));
    }
    
    @Test
    void assertGetParameterRewritersWhenPredicateIsNotNeedRewrite() {
        EncryptRule encryptRule = mock(EncryptRule.class, RETURNS_DEEP_STUBS);
        SelectStatementContext sqlStatementContext = mock(SelectStatementContext.class, RETURNS_DEEP_STUBS);
        when(sqlStatementContext.getTablesContext().getTableNames()).thenReturn(Collections.singletonList("t_order"));
        when(sqlStatementContext.getWhereSegments()).thenReturn(Collections.emptyList());
        assertTrue(new EncryptParameterRewriterBuilder(encryptRule,
                DefaultDatabase.LOGIC_NAME, Collections.singletonMap("test", mock(ShardingSphereSchema.class)), sqlStatementContext, Collections.emptyList()).getParameterRewriters().isEmpty());
    }
}
