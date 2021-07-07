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

package org.apache.shardingsphere.shadow.rewrite.parameter.impl;

import com.google.common.collect.Lists;
import org.apache.shardingsphere.infra.binder.statement.dml.InsertStatementContext;
import org.apache.shardingsphere.infra.rewrite.parameter.builder.impl.GroupedParameterBuilder;
import org.apache.shardingsphere.shadow.rule.ShadowRule;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class ShadowInsertValueParameterRewriterTest {

    private ShadowInsertValueParameterRewriter shadowInsertValueParameterRewriter;

    private InsertStatementContext insertStatementContext;

    @Before
    public void init() {
        String shadowColumn = "shadow_column";
        initShadowInsertValueParameterRewriter(shadowColumn);
        mockInsertStatementContext(shadowColumn);
    }

    private void mockInsertStatementContext(final String shadowColumn) {
        insertStatementContext = mock(InsertStatementContext.class);
        when(insertStatementContext.getInsertColumnNames()).thenReturn(Lists.newArrayList(shadowColumn));
    }

    private void initShadowInsertValueParameterRewriter(final String shadowColumn) {
        shadowInsertValueParameterRewriter = new ShadowInsertValueParameterRewriter();
        shadowInsertValueParameterRewriter.setShadowRule(mockShadowRule(shadowColumn));
    }

    private ShadowRule mockShadowRule(final String shadowColumn) {
        ShadowRule shadowRule = mock(ShadowRule.class);
        when(shadowRule.getColumn()).thenReturn(shadowColumn);
        return shadowRule;
    }

    @Test
    public void assertIsNeedRewriteForShadow() {
        assertTrue(shadowInsertValueParameterRewriter.isNeedRewriteForShadow(insertStatementContext));
    }

    @Test
    public void assertRewrite() {
        shadowInsertValueParameterRewriter.rewrite(mock(GroupedParameterBuilder.class), insertStatementContext, mock(List.class));
    }
}
