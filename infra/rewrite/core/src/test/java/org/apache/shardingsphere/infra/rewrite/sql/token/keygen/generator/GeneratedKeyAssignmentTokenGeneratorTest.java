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

package org.apache.shardingsphere.infra.rewrite.sql.token.keygen.generator;

import org.apache.shardingsphere.infra.binder.context.segment.insert.keygen.GeneratedKeyContext;
import org.apache.shardingsphere.infra.binder.context.statement.type.dml.InsertStatementContext;
import org.apache.shardingsphere.infra.rewrite.sql.token.keygen.pojo.LiteralGeneratedKeyAssignmentToken;
import org.apache.shardingsphere.infra.rewrite.sql.token.keygen.pojo.ParameterMarkerGeneratedKeyAssignmentToken;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.assignment.SetAssignmentSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.InsertStatement;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.isA;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GeneratedKeyAssignmentTokenGeneratorTest {
    
    @Test
    void assertIsGenerateSQLToken() {
        InsertStatementContext insertStatementContext = mock(InsertStatementContext.class, RETURNS_DEEP_STUBS);
        when(insertStatementContext.getSqlStatement().getSetAssignment().isPresent()).thenReturn(true);
        assertTrue(new GeneratedKeyAssignmentTokenGenerator().isGenerateSQLToken(insertStatementContext));
    }
    
    @Test
    void assertGenerateSQLTokenWithLiteralValue() {
        InsertStatementContext insertStatementContext = mockInsertStatementContext();
        GeneratedKeyAssignmentTokenGenerator generator = new GeneratedKeyAssignmentTokenGenerator();
        generator.setParameters(Collections.emptyList());
        assertThat(generator.generateSQLToken(insertStatementContext), isA(LiteralGeneratedKeyAssignmentToken.class));
    }
    
    @Test
    void assertGenerateSQLTokenWithPlaceholder() {
        InsertStatementContext insertStatementContext = mockInsertStatementContext();
        GeneratedKeyAssignmentTokenGenerator generator = new GeneratedKeyAssignmentTokenGenerator();
        generator.setParameters(Collections.singletonList("testObject"));
        assertThat(generator.generateSQLToken(insertStatementContext), isA(ParameterMarkerGeneratedKeyAssignmentToken.class));
    }
    
    private InsertStatementContext mockInsertStatementContext() {
        InsertStatementContext result = mock(InsertStatementContext.class);
        GeneratedKeyContext generatedKeyContext = new GeneratedKeyContext("testColumnName", false);
        generatedKeyContext.getGeneratedValues().add(4);
        when(result.getGeneratedKeyContext()).thenReturn(Optional.of(generatedKeyContext));
        InsertStatement insertStatement = mock(InsertStatement.class);
        when(insertStatement.getSetAssignment()).thenReturn(Optional.of(new SetAssignmentSegment(0, 2, Collections.emptyList())));
        when(result.getSqlStatement()).thenReturn(insertStatement);
        return result;
    }
}
