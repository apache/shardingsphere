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

package org.apache.shardingsphere.encrypt.rewrite.parameter.impl;

import org.apache.shardingsphere.encrypt.rule.EncryptRule;
import org.apache.shardingsphere.encrypt.spi.EncryptAlgorithm;
import org.apache.shardingsphere.infra.binder.segment.table.TablesContext;
import org.apache.shardingsphere.infra.binder.statement.dml.InsertStatementContext;
import org.apache.shardingsphere.infra.binder.statement.dml.UpdateStatementContext;
import org.apache.shardingsphere.infra.metadata.schema.ShardingSphereSchema;
import org.apache.shardingsphere.infra.rewrite.parameter.builder.impl.StandardParameterBuilder;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.BinaryOperationExpression;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.simple.SimpleExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.predicate.WhereSegment;
import org.apache.shardingsphere.sql.parser.sql.common.value.identifier.IdentifierValue;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class EncryptPredicateParameterRewriterTest {

    @InjectMocks
    private EncryptPredicateParameterRewriter reWriter;

    @Test
    public void isNeedRewriteForEncryptTest() {
        final InsertStatementContext insertStatementContext = mock(InsertStatementContext.class);
        assertTrue(reWriter.isNeedRewriteForEncrypt(insertStatementContext));
    }

    @Test
    public void rewriteForEmptyEncryptConditionsTest() {
        StandardParameterBuilder parameterBuilder = new StandardParameterBuilder(Collections.singletonList(new Object()));
        List<Object> parameters = new ArrayList<>();
        parameters.add("p1");

        final InsertStatementContext insertStatementContext = mock(InsertStatementContext.class);

        reWriter.rewrite(parameterBuilder, insertStatementContext, parameters);

        assertEquals(1, parameterBuilder.getParameters().size());
    }

    @Test
    public void rewriteWithEncryptedConditionsTest() {
        List<Object> parameters = new ArrayList<>();
        parameters.add("p1");
        Map<String, String> columnMap = new HashMap<>();
        columnMap.put("table1", "col1");

        final UpdateStatementContext updateStatementContext = mock(UpdateStatementContext.class);
        final EncryptRule encryptRule = mock(EncryptRule.class);
        final ShardingSphereSchema shardingSphereSchema = mock(ShardingSphereSchema.class);
        final WhereSegment whereSegment = mock(WhereSegment.class);
        final BinaryOperationExpression binaryOperationExpression = mock(BinaryOperationExpression.class);
        final ColumnSegment columnSegment = mock(ColumnSegment.class);
        final SimpleExpressionSegment simpleExpressionSegment = mock(SimpleExpressionSegment.class);
        final TablesContext tablesContext = mock(TablesContext.class);
        final EncryptAlgorithm encryptAlgorithm = mock(EncryptAlgorithm.class);

        when(updateStatementContext.getSchemaName()).thenReturn("schema");
        when(updateStatementContext.getWhere()).thenReturn(Optional.of(whereSegment));
        when(whereSegment.getExpr()).thenReturn(binaryOperationExpression);
        when(binaryOperationExpression.getLeft()).thenReturn(columnSegment);
        when(binaryOperationExpression.getRight()).thenReturn(simpleExpressionSegment);
        when(binaryOperationExpression.getOperator()).thenReturn("!=");
        when(updateStatementContext.getTablesContext()).thenReturn(tablesContext);
        when(tablesContext.findTableName(anyCollection(), any())).thenReturn(columnMap);
        when(columnSegment.getQualifiedName()).thenReturn("table1");
        IdentifierValue idf = new IdentifierValue("idf");
        when(columnSegment.getIdentifier()).thenReturn(idf);
        when(encryptRule.findEncryptor(anyString(), anyString(), anyString())).thenReturn(Optional.of(encryptAlgorithm));

        reWriter.setEncryptRule(encryptRule);
        reWriter.setSchema(shardingSphereSchema);
        reWriter.setQueryWithCipherColumn(true);

        StandardParameterBuilder parameterBuilder = new StandardParameterBuilder(Collections.singletonList(new Object()));
        reWriter.rewrite(parameterBuilder, updateStatementContext, parameters);

        assertEquals(1, parameterBuilder.getParameters().size());
    }
}

