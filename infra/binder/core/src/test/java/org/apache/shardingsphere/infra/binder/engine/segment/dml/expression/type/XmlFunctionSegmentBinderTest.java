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

package org.apache.shardingsphere.infra.binder.engine.segment.dml.expression.type;

import com.cedarsoftware.util.CaseInsensitiveMap.CaseInsensitiveString;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import org.apache.shardingsphere.infra.binder.engine.segment.SegmentType;
import org.apache.shardingsphere.infra.binder.engine.segment.dml.expression.ExpressionSegmentBinder;
import org.apache.shardingsphere.infra.binder.engine.segment.dml.from.context.TableSegmentBinderContext;
import org.apache.shardingsphere.infra.binder.engine.statement.SQLStatementBinderContext;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.xml.XmlElementFunctionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.xml.XmlSerializeFunctionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;
import org.apache.shardingsphere.test.infra.framework.extension.mock.AutoMockExtension;
import org.apache.shardingsphere.test.infra.framework.extension.mock.StaticMockSettings;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(AutoMockExtension.class)
@StaticMockSettings(ExpressionSegmentBinder.class)
class XmlFunctionSegmentBinderTest {
    
    @Test
    void assertBindXmlElementFunction() {
        XmlElementFunctionSegment segment = new XmlElementFunctionSegment(0, 30, "XMLELEMENT", new IdentifierValue("user_name_node"), "XMLELEMENT(NAME user_name_node, user_name)");
        ExpressionSegment parameter = mock(ExpressionSegment.class);
        ExpressionSegment attribute = mock(ExpressionSegment.class);
        segment.getParameters().add(parameter);
        segment.getXmlAttributes().add(attribute);
        ExpressionSegment boundParameter = mock(ExpressionSegment.class);
        ExpressionSegment boundAttribute = mock(ExpressionSegment.class);
        SQLStatementBinderContext binderContext = mock(SQLStatementBinderContext.class);
        Multimap<CaseInsensitiveString, TableSegmentBinderContext> tableBinderContexts = LinkedHashMultimap.create();
        Multimap<CaseInsensitiveString, TableSegmentBinderContext> outerTableBinderContexts = LinkedHashMultimap.create();
        when(ExpressionSegmentBinder.bind(parameter, SegmentType.PROJECTION, binderContext, tableBinderContexts, outerTableBinderContexts)).thenReturn(boundParameter);
        when(ExpressionSegmentBinder.bind(attribute, SegmentType.PROJECTION, binderContext, tableBinderContexts, outerTableBinderContexts)).thenReturn(boundAttribute);
        XmlElementFunctionSegment actual = XmlFunctionSegmentBinder.bind(segment, SegmentType.PROJECTION, binderContext, tableBinderContexts, outerTableBinderContexts);
        assertThat(actual.getFunctionName(), is(segment.getFunctionName()));
        assertThat(actual.getIdentifier(), is(segment.getIdentifier()));
        assertThat(actual.getText(), is(segment.getText()));
        assertThat(actual.getParameters().iterator().next(), is(boundParameter));
        assertThat(actual.getXmlAttributes().iterator().next(), is(boundAttribute));
    }
    
    @Test
    void assertBindXmlSerializeFunction() {
        ExpressionSegment parameter = mock(ExpressionSegment.class);
        XmlSerializeFunctionSegment segment = new XmlSerializeFunctionSegment(0, 70, "XMLSERIALIZE", parameter, "VARCHAR2(100)", "'UTF-8'", "'1.0'", "2", "XMLSERIALIZE(...)");
        ExpressionSegment boundParameter = mock(ExpressionSegment.class);
        SQLStatementBinderContext binderContext = mock(SQLStatementBinderContext.class);
        Multimap<CaseInsensitiveString, TableSegmentBinderContext> tableBinderContexts = LinkedHashMultimap.create();
        Multimap<CaseInsensitiveString, TableSegmentBinderContext> outerTableBinderContexts = LinkedHashMultimap.create();
        when(ExpressionSegmentBinder.bind(parameter, SegmentType.PROJECTION, binderContext, tableBinderContexts, outerTableBinderContexts)).thenReturn(boundParameter);
        XmlSerializeFunctionSegment actual = XmlFunctionSegmentBinder.bind(segment, SegmentType.PROJECTION, binderContext, tableBinderContexts, outerTableBinderContexts);
        assertThat(actual.getFunctionName(), is(segment.getFunctionName()));
        assertThat(actual.getParameter(), is(boundParameter));
        assertThat(actual.getDataType(), is(segment.getDataType()));
        assertThat(actual.getEncoding(), is(segment.getEncoding()));
        assertThat(actual.getVersion(), is(segment.getVersion()));
        assertThat(actual.getIdentSize(), is(segment.getIdentSize()));
        assertThat(actual.getText(), is(segment.getText()));
    }
}
