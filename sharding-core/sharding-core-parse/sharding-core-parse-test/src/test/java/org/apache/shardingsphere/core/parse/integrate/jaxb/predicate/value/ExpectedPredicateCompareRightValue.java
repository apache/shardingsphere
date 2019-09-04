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

package org.apache.shardingsphere.core.parse.integrate.jaxb.predicate.value;

import lombok.Getter;
import lombok.Setter;
import org.apache.shardingsphere.core.parse.integrate.jaxb.expr.ExpectedExpressionSegment;
import org.apache.shardingsphere.core.parse.integrate.jaxb.expr.complex.ExpectedCommonExpressionSegment;
import org.apache.shardingsphere.core.parse.integrate.jaxb.expr.complex.ExpectedComplexExpressionSegment;
import org.apache.shardingsphere.core.parse.integrate.jaxb.expr.complex.ExpectedSubquerySegment;
import org.apache.shardingsphere.core.parse.integrate.jaxb.expr.simple.ExpectedLiteralExpressionSegment;
import org.apache.shardingsphere.core.parse.integrate.jaxb.expr.simple.ExpectedParamMarkerExpressionSegment;

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
    private ExpectedCommonExpressionSegment commonExpressionSegment;

    @XmlElement(name = "subquery-segment")
    private ExpectedSubquerySegment subquerySegment;

    @XmlElement(name = "literal-expression")
    private ExpectedLiteralExpressionSegment literalExpression;

    @XmlElement(name = "param-marker-expression")
    private ExpectedParamMarkerExpressionSegment paramMarkerExpression;

    /**
     * find expected expression
     * @param expectedExpressionSegment
     * @return  expression segment
     */
    public <T extends ExpectedExpressionSegment> T findExpectedExpression(final Class<T> expectedExpressionSegment) {
        if (expectedExpressionSegment.isAssignableFrom(ExpectedParamMarkerExpressionSegment.class)) {
            return (T) paramMarkerExpression;
        }
        if (expectedExpressionSegment.isAssignableFrom(ExpectedLiteralExpressionSegment.class)) {
            return (T) literalExpression;
        }
        if (expectedExpressionSegment.isAssignableFrom(ExpectedCommonExpressionSegment.class)) {
            return (T) commonExpressionSegment;
        }
        if (expectedExpressionSegment.isAssignableFrom(ExpectedComplexExpressionSegment.class)) {
            return (T) subquerySegment;
        }
        return null;
    }
}
