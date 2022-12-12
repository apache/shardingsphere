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

package org.apache.shardingsphere.sharding.rewrite.token;

import org.apache.shardingsphere.infra.binder.segment.insert.keygen.GeneratedKeyContext;
import org.apache.shardingsphere.infra.binder.segment.insert.values.InsertValueContext;
import org.apache.shardingsphere.infra.binder.segment.insert.values.expression.DerivedLiteralExpressionSegment;
import org.apache.shardingsphere.infra.binder.segment.insert.values.expression.DerivedParameterMarkerExpressionSegment;
import org.apache.shardingsphere.infra.binder.statement.dml.InsertStatementContext;
import org.apache.shardingsphere.infra.rewrite.sql.token.pojo.SQLToken;
import org.apache.shardingsphere.infra.rewrite.sql.token.pojo.generic.InsertValue;
import org.apache.shardingsphere.infra.rewrite.sql.token.pojo.generic.InsertValuesToken;
import org.apache.shardingsphere.sharding.rewrite.token.generator.impl.keygen.GeneratedKeyInsertValuesTokenGenerator;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class GeneratedKeyInsertValuesTokenGeneratorTest {
    
    @Test
    public void assertGenerateSQLToken() {
        InsertStatementContext insertStatementContext = mock(InsertStatementContext.class);
        GeneratedKeyContext generatedKeyContext = getGeneratedKeyContext();
        when(insertStatementContext.getGeneratedKeyContext()).thenReturn(Optional.of(generatedKeyContext));
        when(insertStatementContext.getInsertValueContexts()).thenReturn(Collections.singletonList(mock(InsertValueContext.class)));
        List<List<Object>> parameterGroups = Collections.singletonList(new ArrayList<>(Collections.singleton(new Object())));
        when(insertStatementContext.getGroupedParameters()).thenReturn(parameterGroups);
        GeneratedKeyInsertValuesTokenGenerator generator = new GeneratedKeyInsertValuesTokenGenerator();
        generator.setPreviousSQLTokens(getPreviousSQLTokens());
        SQLToken sqlToken = generator.generateSQLToken(insertStatementContext);
        assertThat(sqlToken, instanceOf(InsertValuesToken.class));
        assertThat(((InsertValuesToken) sqlToken).getInsertValues().get(0).getValues().get(0), instanceOf(DerivedParameterMarkerExpressionSegment.class));
        parameterGroups.get(0).clear();
        ((InsertValuesToken) sqlToken).getInsertValues().get(0).getValues().clear();
        sqlToken = generator.generateSQLToken(insertStatementContext);
        assertThat(((InsertValuesToken) sqlToken).getInsertValues().get(0).getValues().get(0), instanceOf(DerivedLiteralExpressionSegment.class));
    }
    
    private List<SQLToken> getPreviousSQLTokens() {
        InsertValue insertValue = mock(InsertValue.class);
        when(insertValue.getValues()).thenReturn(new LinkedList<>());
        InsertValuesToken insertValuesToken = mock(InsertValuesToken.class);
        when(insertValuesToken.getInsertValues()).thenReturn(Collections.singletonList(insertValue));
        return Collections.singletonList(insertValuesToken);
    }
    
    private GeneratedKeyContext getGeneratedKeyContext() {
        GeneratedKeyContext result = mock(GeneratedKeyContext.class);
        when(result.getGeneratedValues()).thenReturn(Collections.singleton("TEST_GENERATED_VALUE"));
        return result;
    }
}
