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
import org.apache.shardingsphere.core.parse.integrate.jaxb.expr.complex.ExpectedCommonExpressionSegment;
import org.apache.shardingsphere.core.parse.integrate.jaxb.expr.complex.ExpectedSubquerySegment;
import org.apache.shardingsphere.core.parse.integrate.jaxb.expr.simple.ExpectedLiteralExpressionSegment;
import org.apache.shardingsphere.core.parse.integrate.jaxb.expr.simple.ExpectedParamMarkerExpressionSegment;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

@Getter
@Setter
@XmlAccessorType(XmlAccessType.FIELD)
public final class ExpectedPredicateBetweenRightValue implements ExpectedPredicateRightValue {

    @XmlElement(name = "between-common-expression")
    private ExpectedCommonExpressionSegment betweenCommonExpressionSegment;

    @XmlElement(name = "between-subquery-segment")
    private ExpectedSubquerySegment betweenSubquerySegment;

    @XmlElement(name = "between-literal-expression")
    private ExpectedLiteralExpressionSegment betweenLiteralExpression;

    @XmlElement(name = "between-param-marker-expression")
    private ExpectedParamMarkerExpressionSegment betweenParamMarkerExpression;

    @XmlElement(name = "and-subquery-segment")
    private ExpectedSubquerySegment andSubquerySegment;

    @XmlElement(name = "and-common-expression")
    private ExpectedCommonExpressionSegment andCommonExpressionSegment;

    @XmlElement(name = "and-literal-expression")
    private ExpectedLiteralExpressionSegment andLiteralExpression;

    @XmlElement(name = "and-param-marker-expression")
    private ExpectedParamMarkerExpressionSegment andParamMarkerExpression;
}
