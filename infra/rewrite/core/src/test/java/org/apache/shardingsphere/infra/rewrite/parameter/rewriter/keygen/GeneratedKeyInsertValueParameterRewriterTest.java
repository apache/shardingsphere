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

package org.apache.shardingsphere.infra.rewrite.parameter.rewriter.keygen;

import org.apache.shardingsphere.infra.binder.context.statement.type.dml.InsertStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.type.dml.SelectStatementContext;
import org.apache.shardingsphere.infra.rewrite.parameter.builder.ParameterBuilder;
import org.apache.shardingsphere.infra.rewrite.parameter.builder.impl.GroupedParameterBuilder;
import org.apache.shardingsphere.infra.rewrite.parameter.builder.impl.StandardParameterBuilder;
import org.apache.shardingsphere.infra.rewrite.parameter.rewriter.ParameterRewriter;
import org.junit.jupiter.api.Test;
import org.mockito.stubbing.Answer;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GeneratedKeyInsertValueParameterRewriterTest {
    
    private static final int TEST_PARAMETER_COUNT = 3;
    
    private static final String TEST_GENERATED_VALUE = "testGeneratedValue";
    
    @Test
    void assertIsNeedRewrite() {
        ParameterRewriter paramRewriter = new GeneratedKeyInsertValueParameterRewriter();
        SelectStatementContext selectStatementContext = mock(SelectStatementContext.class);
        assertFalse(paramRewriter.isNeedRewrite(selectStatementContext));
        InsertStatementContext insertStatementContext = mock(InsertStatementContext.class, RETURNS_DEEP_STUBS);
        assertFalse(paramRewriter.isNeedRewrite(insertStatementContext));
        when(insertStatementContext.getGeneratedKeyContext().isPresent()).thenReturn(Boolean.TRUE);
        assertFalse(paramRewriter.isNeedRewrite(insertStatementContext));
        when(insertStatementContext.getGeneratedKeyContext().get().isGenerated()).thenReturn(Boolean.TRUE);
        when(insertStatementContext.getGeneratedKeyContext().get().getGeneratedValues().isEmpty()).thenReturn(Boolean.TRUE);
        assertFalse(paramRewriter.isNeedRewrite(insertStatementContext));
        when(insertStatementContext.getGeneratedKeyContext().get().getGeneratedValues().isEmpty()).thenReturn(Boolean.FALSE);
        assertTrue(paramRewriter.isNeedRewrite(insertStatementContext));
    }
    
    @Test
    void assertRewriteWithoutGeneratedKeys() {
        InsertStatementContext insertStatementContext = mock(InsertStatementContext.class, RETURNS_DEEP_STUBS);
        ParameterBuilder groupedParamBuilder = getParameterBuilder();
        ParameterRewriter paramRewriter = new GeneratedKeyInsertValueParameterRewriter();
        paramRewriter.rewrite(groupedParamBuilder, insertStatementContext, null);
        assertFalse(((GroupedParameterBuilder) groupedParamBuilder).getParameterBuilders().get(0).getAddedIndexAndParameters().containsKey(TEST_PARAMETER_COUNT));
    }
    
    @Test
    void assertRewriteWithGeneratedKeys() {
        InsertStatementContext insertStatementContext = getInsertStatementContext();
        ParameterBuilder groupedParamBuilder = getParameterBuilder();
        ParameterRewriter paramRewriter = new GeneratedKeyInsertValueParameterRewriter();
        paramRewriter.rewrite(groupedParamBuilder, insertStatementContext, null);
        assertThat(((GroupedParameterBuilder) groupedParamBuilder).getParameterBuilders().get(0).getAddedIndexAndParameters().get(TEST_PARAMETER_COUNT), hasItem(TEST_GENERATED_VALUE));
    }
    
    private ParameterBuilder getParameterBuilder() {
        StandardParameterBuilder standardParamBuilder = mock(StandardParameterBuilder.class);
        Map<Integer, Collection<Object>> addedIndexAndParams = new HashMap<>();
        when(standardParamBuilder.getAddedIndexAndParameters()).thenReturn(addedIndexAndParams);
        doAnswer((Answer<Void>) invocation -> {
            int index = invocation.getArgument(0);
            addedIndexAndParams.put(index, invocation.getArgument(1));
            return null;
        }).when(standardParamBuilder).addAddedParameters(anyInt(), anyCollection());
        GroupedParameterBuilder result = mock(GroupedParameterBuilder.class);
        when(result.getParameterBuilders()).thenReturn(Collections.singletonList(standardParamBuilder));
        return result;
    }
    
    private InsertStatementContext getInsertStatementContext() {
        InsertStatementContext result = mock(InsertStatementContext.class, RETURNS_DEEP_STUBS);
        when(result.getGeneratedKeyContext().isPresent()).thenReturn(true);
        when(result.getGeneratedKeyContext().get().getColumnName()).thenReturn("testColumnName");
        when(result.getGeneratedKeyContext().get().getGeneratedValues()).thenReturn(Collections.singleton(TEST_GENERATED_VALUE));
        when(result.getGroupedParameters()).thenReturn(Collections.singletonList(Collections.singletonList("testGroupedParameter")));
        when(result.getInsertValueContexts().get(0).getParameterCount()).thenReturn(TEST_PARAMETER_COUNT);
        return result;
    }
}
