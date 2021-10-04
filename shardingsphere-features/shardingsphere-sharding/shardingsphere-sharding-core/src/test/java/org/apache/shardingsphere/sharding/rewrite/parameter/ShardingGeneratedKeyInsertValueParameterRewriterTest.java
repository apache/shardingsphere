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

package org.apache.shardingsphere.sharding.rewrite.parameter;

import org.apache.shardingsphere.infra.binder.statement.dml.InsertStatementContext;
import org.apache.shardingsphere.infra.binder.statement.dml.SelectStatementContext;
import org.apache.shardingsphere.infra.rewrite.parameter.builder.ParameterBuilder;
import org.apache.shardingsphere.infra.rewrite.parameter.builder.impl.GroupedParameterBuilder;
import org.apache.shardingsphere.infra.rewrite.parameter.builder.impl.StandardParameterBuilder;
import org.apache.shardingsphere.sharding.rewrite.parameter.impl.ShardingGeneratedKeyInsertValueParameterRewriter;
import org.junit.Test;
import org.mockito.stubbing.Answer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class ShardingGeneratedKeyInsertValueParameterRewriterTest {
    
    private static final int TEST_PARAMETER_COUNT = 3;
    
    private static final String TEST_GENERATED_VALUE = "testGeneratedValue";
    
    @Test
    public void assertIsNeedRewrite() {
        ShardingGeneratedKeyInsertValueParameterRewriter shardingGeneratedKeyInsertValueParameterRewriter = new ShardingGeneratedKeyInsertValueParameterRewriter();
        SelectStatementContext selectStatementContext = mock(SelectStatementContext.class);
        assertFalse(shardingGeneratedKeyInsertValueParameterRewriter.isNeedRewrite(selectStatementContext));
        InsertStatementContext insertStatementContext = mock(InsertStatementContext.class, RETURNS_DEEP_STUBS);
        assertFalse(shardingGeneratedKeyInsertValueParameterRewriter.isNeedRewrite(insertStatementContext));
        when(insertStatementContext.getGeneratedKeyContext().isPresent()).thenReturn(Boolean.TRUE);
        assertFalse(shardingGeneratedKeyInsertValueParameterRewriter.isNeedRewrite(insertStatementContext));
        when(insertStatementContext.getGeneratedKeyContext().get().isGenerated()).thenReturn(Boolean.TRUE);
        when(insertStatementContext.getGeneratedKeyContext().get().getGeneratedValues().isEmpty()).thenReturn(Boolean.TRUE);
        assertFalse(shardingGeneratedKeyInsertValueParameterRewriter.isNeedRewrite(insertStatementContext));
        when(insertStatementContext.getGeneratedKeyContext().get().getGeneratedValues().isEmpty()).thenReturn(Boolean.FALSE);
        assertTrue(shardingGeneratedKeyInsertValueParameterRewriter.isNeedRewrite(insertStatementContext));
    }
    
    @Test
    public void assertRewrite() {
        InsertStatementContext insertStatementContext = getInsertStatementContext();
        ParameterBuilder groupedParameterBuilder = getParameterBuilder();
        ShardingGeneratedKeyInsertValueParameterRewriter shardingGeneratedKeyInsertValueParameterRewriter = new ShardingGeneratedKeyInsertValueParameterRewriter();
        shardingGeneratedKeyInsertValueParameterRewriter.rewrite(groupedParameterBuilder, insertStatementContext, null);
        assertThat(((GroupedParameterBuilder) groupedParameterBuilder).getParameterBuilders().get(0).getAddedIndexAndParameters().get(TEST_PARAMETER_COUNT), hasItem(TEST_GENERATED_VALUE));
    }
    
    private ParameterBuilder getParameterBuilder() {
        StandardParameterBuilder standardParameterBuilder = mock(StandardParameterBuilder.class);
        Map<Integer, Collection<Object>> addedIndexAndParameters = new HashMap<>();
        when(standardParameterBuilder.getAddedIndexAndParameters()).thenReturn(addedIndexAndParameters);
        doAnswer((Answer<Void>) invocation -> {
            int index = invocation.getArgument(0);
            Collection<Object> parameters = invocation.getArgument(1);
            addedIndexAndParameters.put(index, parameters);
            return null;
        }).when(standardParameterBuilder).addAddedParameters(anyInt(), anyCollection());
        List<StandardParameterBuilder> parameterBuildersList = new ArrayList<>();
        parameterBuildersList.add(standardParameterBuilder);
        GroupedParameterBuilder result = mock(GroupedParameterBuilder.class);
        when(result.getParameterBuilders()).thenReturn(parameterBuildersList);
        return result;
    }
    
    private InsertStatementContext getInsertStatementContext() {
        InsertStatementContext result = mock(InsertStatementContext.class, RETURNS_DEEP_STUBS);
        when(result.getGeneratedKeyContext().isPresent()).thenReturn(Boolean.TRUE);
        when(result.getGeneratedKeyContext().get().getColumnName()).thenReturn("testColumnName");
        Collection<Comparable<?>> generatedValuesCollection = new LinkedList<>();
        generatedValuesCollection.add(TEST_GENERATED_VALUE);
        when(result.getGeneratedKeyContext().get().getGeneratedValues()).thenReturn(generatedValuesCollection);
        List<Object> groupedParameter = new LinkedList<>();
        groupedParameter.add("testGroupedParameter");
        List<List<Object>> groupedParametersList = new LinkedList<>();
        groupedParametersList.add(groupedParameter);
        when(result.getGroupedParameters()).thenReturn(groupedParametersList);
        when(result.getInsertValueContexts().get(0).getParameterCount()).thenReturn(TEST_PARAMETER_COUNT);
        return result;
    }
}
