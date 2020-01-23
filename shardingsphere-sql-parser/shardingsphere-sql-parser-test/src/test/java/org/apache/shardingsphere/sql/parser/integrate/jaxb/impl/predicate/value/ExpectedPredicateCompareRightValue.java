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

package org.apache.shardingsphere.sql.parser.integrate.jaxb.impl.predicate.value;

import lombok.Getter;
import lombok.Setter;
import org.apache.shardingsphere.sql.parser.integrate.jaxb.impl.expr.ExpectedExpressionSegment;
import org.apache.shardingsphere.sql.parser.integrate.jaxb.impl.expr.complex.ExpectedCommonExpression;
import org.apache.shardingsphere.sql.parser.integrate.jaxb.impl.expr.complex.ExpectedComplexExpressionSegment;
import org.apache.shardingsphere.sql.parser.integrate.jaxb.impl.expr.complex.ExpectedSubquery;
import org.apache.shardingsphere.sql.parser.integrate.jaxb.impl.expr.simple.ExpectedLiteralExpression;
import org.apache.shardingsphere.sql.parser.integrate.jaxb.impl.expr.simple.ExpectedParameterMarkerExpression;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

@Getter
@Setter
@XmlAccessorType(XmlAccessType.FIELD)
public final class ExpectedPredicateCompareRightValue implements ExpectedPredicateRightValue {

    @XmlAttribute
    private String operator;

    @XmlElement(name = "common-expression")
    private ExpectedCommonExpression commonExpressionSegment;

    @XmlElement(name = "subquery-segment")
    private ExpectedSubquery subquerySegment;

    @XmlElement(name = "literal-expression")
    private ExpectedLiteralExpression literalExpression;

    @XmlElement(name = "parameter-marker-expression")
    private ExpectedParameterMarkerExpression parameterMarkerExpression;

    /**
     * Find expected expression.
     * 
     * @param expectedExpressionSegment expected expression segment
     * @param <T> type of expected expression segment
     * @return expression segment
     */
    @SuppressWarnings("unchecked")
    public <T extends ExpectedExpressionSegment> T findExpectedExpression(final Class<T> expectedExpressionSegment) {
        if (expectedExpressionSegment.isAssignableFrom(ExpectedParameterMarkerExpression.class)) {
            return (T) parameterMarkerExpression;
        }
        if (expectedExpressionSegment.isAssignableFrom(ExpectedLiteralExpression.class)) {
            return (T) literalExpression;
        }
        if (expectedExpressionSegment.isAssignableFrom(ExpectedCommonExpression.class)) {
            return (T) commonExpressionSegment;
        }
        if (expectedExpressionSegment.isAssignableFrom(ExpectedComplexExpressionSegment.class)) {
            return (T) subquerySegment;
        }
        return null;
    }
}
