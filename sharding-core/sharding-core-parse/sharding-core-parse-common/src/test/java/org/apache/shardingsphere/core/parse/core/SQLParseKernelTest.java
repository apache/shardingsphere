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

package org.apache.shardingsphere.core.parse.core;

import lombok.SneakyThrows;
import org.antlr.v4.runtime.ParserRuleContext;
import org.apache.shardingsphere.core.database.DatabaseTypes;
import org.apache.shardingsphere.core.parse.core.extractor.SQLSegmentsExtractorEngine;
import org.apache.shardingsphere.core.parse.core.filler.SQLStatementFillerEngine;
import org.apache.shardingsphere.core.parse.core.parser.SQLAST;
import org.apache.shardingsphere.core.parse.core.parser.SQLParserEngine;
import org.apache.shardingsphere.core.parse.core.rule.registry.ParseRuleRegistry;
import org.apache.shardingsphere.core.parse.core.rule.registry.statement.SQLStatementRule;
import org.apache.shardingsphere.core.parse.sql.segment.SQLSegment;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.lang.reflect.Field;
import java.util.Collections;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class SQLParseKernelTest {
    
    private SQLParseKernel parseKernel;
    
    @Mock
    private SQLStatementFillerEngine fillerEngine;
    
    @Mock
    private SQLStatementRule sqlStatementRule;
    
    @Before
    public void setUp() {
        parseKernel = new SQLParseKernel(ParseRuleRegistry.getInstance(), DatabaseTypes.getTrunkDatabaseType("MySQL"), "SELECT 1");
        SQLAST ast = mock(SQLAST.class);
        when(ast.getParameterMarkerIndexes()).thenReturn(Collections.<ParserRuleContext, Integer>emptyMap());
        when(ast.getSqlStatementRule()).thenReturn(sqlStatementRule);
        SQLParserEngine parserEngine = mock(SQLParserEngine.class);
        when(parserEngine.parse()).thenReturn(ast);
        setField("parserEngine", parserEngine);
        SQLSegmentsExtractorEngine extractorEngine = mock(SQLSegmentsExtractorEngine.class);
        when(extractorEngine.extract(ast)).thenReturn(Collections.<SQLSegment>emptyList());
        setField("extractorEngine", extractorEngine);
        setField("fillerEngine", fillerEngine);
    }
    
    @SneakyThrows
    private void setField(final String fieldName, final Object value) {
        Field field = SQLParseKernel.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(parseKernel, value);
    }
    
    @Test
    public void assertParse() {
        parseKernel.parse();
        verify(fillerEngine).fill(Collections.<SQLSegment>emptyList(), 0, sqlStatementRule);
    }
}
