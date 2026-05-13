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
import com.google.common.collect.Multimap;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.binder.engine.segment.SegmentType;
import org.apache.shardingsphere.infra.binder.engine.segment.dml.expression.ExpressionSegmentBinder;
import org.apache.shardingsphere.infra.binder.engine.segment.dml.from.context.TableSegmentBinderContext;
import org.apache.shardingsphere.infra.binder.engine.statement.SQLStatementBinderContext;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.xml.XmlElementFunctionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.xml.XmlSerializeFunctionSegment;

/**
 * XML function segment binder.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class XmlFunctionSegmentBinder {
    
    /**
     * Bind XML element function segment.
     *
     * @param segment XML element function segment
     * @param parentSegmentType parent segment type
     * @param binderContext SQL statement binder context
     * @param tableBinderContexts table binder contexts
     * @param outerTableBinderContexts outer table binder contexts
     * @return bound XML element function segment
     */
    public static XmlElementFunctionSegment bind(final XmlElementFunctionSegment segment, final SegmentType parentSegmentType, final SQLStatementBinderContext binderContext,
                                                 final Multimap<CaseInsensitiveString, TableSegmentBinderContext> tableBinderContexts,
                                                 final Multimap<CaseInsensitiveString, TableSegmentBinderContext> outerTableBinderContexts) {
        XmlElementFunctionSegment result = new XmlElementFunctionSegment(segment.getStartIndex(), segment.getStopIndex(), segment.getFunctionName(), segment.getIdentifier(), segment.getText());
        for (ExpressionSegment each : segment.getParameters()) {
            result.getParameters().add(ExpressionSegmentBinder.bind(each, parentSegmentType, binderContext, tableBinderContexts, outerTableBinderContexts));
        }
        for (ExpressionSegment each : segment.getXmlAttributes()) {
            result.getXmlAttributes().add(ExpressionSegmentBinder.bind(each, parentSegmentType, binderContext, tableBinderContexts, outerTableBinderContexts));
        }
        return result;
    }
    
    /**
     * Bind XML serialize function segment.
     *
     * @param segment XML serialize function segment
     * @param parentSegmentType parent segment type
     * @param binderContext SQL statement binder context
     * @param tableBinderContexts table binder contexts
     * @param outerTableBinderContexts outer table binder contexts
     * @return bound XML serialize function segment
     */
    public static XmlSerializeFunctionSegment bind(final XmlSerializeFunctionSegment segment, final SegmentType parentSegmentType, final SQLStatementBinderContext binderContext,
                                                   final Multimap<CaseInsensitiveString, TableSegmentBinderContext> tableBinderContexts,
                                                   final Multimap<CaseInsensitiveString, TableSegmentBinderContext> outerTableBinderContexts) {
        return new XmlSerializeFunctionSegment(segment.getStartIndex(), segment.getStopIndex(), segment.getFunctionName(),
                ExpressionSegmentBinder.bind(segment.getParameter(), parentSegmentType, binderContext, tableBinderContexts, outerTableBinderContexts),
                segment.getDataType(), segment.getEncoding(), segment.getVersion(), segment.getIdentSize(), segment.getText());
    }
}
