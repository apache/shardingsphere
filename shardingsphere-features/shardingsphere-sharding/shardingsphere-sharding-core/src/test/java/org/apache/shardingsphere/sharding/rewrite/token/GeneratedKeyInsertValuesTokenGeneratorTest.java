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
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.ExpressionSegment;
import org.junit.Test;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class GeneratedKeyInsertValuesTokenGeneratorTest {

    @Test
    public void assertGenerateSQLToken() {
        InsertStatementContext insertStatementContext = mock(InsertStatementContext.class);
        GeneratedKeyContext generatedKeyContext = getGeneratedKeyContext();
        when(insertStatementContext.getGeneratedKeyContext()).thenReturn(Optional.of(generatedKeyContext));
        List<InsertValueContext> insertValueContextsList = getInsertValueContextsList();
        when(insertStatementContext.getInsertValueContexts()).thenReturn(insertValueContextsList);
        List<List<Object>> groupedParametersList = new LinkedList<>();
        List<Object> groupedParameters = new LinkedList<>();
        groupedParameters.add(new Object());
        groupedParametersList.add(groupedParameters);
        when(insertStatementContext.getGroupedParameters()).thenReturn(groupedParametersList);
        GeneratedKeyInsertValuesTokenGenerator generatedKeyInsertValuesTokenGenerator = new GeneratedKeyInsertValuesTokenGenerator();
        List<SQLToken> previousSQLTokens = getPreviousSQLTokens();
        generatedKeyInsertValuesTokenGenerator.setPreviousSQLTokens(previousSQLTokens);
        SQLToken sqlToken = generatedKeyInsertValuesTokenGenerator.generateSQLToken(insertStatementContext);
        assertThat(sqlToken, instanceOf(InsertValuesToken.class));
        assertThat(((InsertValuesToken) sqlToken).getInsertValues().get(0).getValues().get(0), instanceOf(DerivedParameterMarkerExpressionSegment.class));
        groupedParametersList.get(0).clear();
        ((InsertValuesToken) sqlToken).getInsertValues().get(0).getValues().clear();
        sqlToken = generatedKeyInsertValuesTokenGenerator.generateSQLToken(insertStatementContext);
        assertThat(((InsertValuesToken) sqlToken).getInsertValues().get(0).getValues().get(0), instanceOf(DerivedLiteralExpressionSegment.class));
    }

    private List<SQLToken> getPreviousSQLTokens() {
        List<ExpressionSegment> valuesList = new LinkedList<>();
        InsertValue insertValue = mock(InsertValue.class);
        when(insertValue.getValues()).thenReturn(valuesList);
        List<InsertValue> insertValuesList = new LinkedList<>();
        insertValuesList.add(insertValue);
        InsertValuesToken insertValuesToken = mock(InsertValuesToken.class);
        when(insertValuesToken.getInsertValues()).thenReturn(insertValuesList);
        List<SQLToken> result = new LinkedList<>();
        result.add(insertValuesToken);
        return result;
    }

    private GeneratedKeyContext getGeneratedKeyContext() {
        GeneratedKeyContext result = mock(GeneratedKeyContext.class);
        Collection<Comparable<?>> generatedValuesCollection = new LinkedList<>();
        generatedValuesCollection.add("TEST_GENERATED_VALUE");
        when(result.getGeneratedValues()).thenReturn(generatedValuesCollection);
        return result;
    }

    private List<InsertValueContext> getInsertValueContextsList() {
        InsertValueContext insertValueContext = mock(InsertValueContext.class);
        List<InsertValueContext> result = new LinkedList<>();
        result.add(insertValueContext);
        return result;
    }
}
