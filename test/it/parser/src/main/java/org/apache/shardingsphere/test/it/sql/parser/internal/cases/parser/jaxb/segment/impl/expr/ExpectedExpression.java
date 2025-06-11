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

package org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.segment.impl.expr;

import lombok.Getter;
import lombok.Setter;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.segment.AbstractExpectedSQLSegment;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.segment.impl.column.ExpectedColumn;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.segment.impl.expr.complex.ExpectedCommonExpression;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.segment.impl.expr.simple.ExpectedLiteralExpression;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.segment.impl.expr.simple.ExpectedParameterMarkerExpression;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.segment.impl.expr.simple.ExpectedSubquery;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.segment.impl.function.ExpectedFunction;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.segment.impl.generic.ExpectedDataType;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.segment.impl.json.ExpectedJsonNullClauseSegment;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.segment.impl.projection.impl.aggregation.ExpectedAggregationProjection;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.segment.impl.projection.impl.expression.ExpectedExpressionProjection;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.segment.impl.xmlquery.ExpectedXmlQueryAndExistsFunctionSegment;

import javax.xml.bind.annotation.XmlElement;

/**
 * Expected expression.
 */
@Getter
@Setter
public final class ExpectedExpression extends AbstractExpectedSQLSegment {
    
    @XmlElement(name = "between-expression")
    private ExpectedBetweenExpression betweenExpression;
    
    @XmlElement(name = "binary-operation-expression")
    private ExpectedBinaryOperationExpression binaryOperationExpression;
    
    @XmlElement
    private ExpectedColumn column;
    
    @XmlElement(name = "data-type")
    private ExpectedDataType dataType;
    
    @XmlElement(name = "common-expression")
    private ExpectedCommonExpression commonExpression;
    
    @XmlElement(name = "exists-subquery")
    private ExpectedExistsSubquery existsSubquery;
    
    @XmlElement(name = "expression-projection")
    private ExpectedExpressionProjection expressionProjection;
    
    @XmlElement
    private ExpectedFunction function;
    
    @XmlElement(name = "in-expression")
    private ExpectedInExpression inExpression;
    
    @XmlElement(name = "list-expression")
    private ExpectedListExpression listExpression;
    
    @XmlElement(name = "literal-expression")
    private ExpectedLiteralExpression literalExpression;
    
    @XmlElement(name = "not-expression")
    private ExpectedNotExpression notExpression;
    
    @XmlElement(name = "parameter-marker-expression")
    private ExpectedParameterMarkerExpression parameterMarkerExpression;
    
    @XmlElement
    private ExpectedSubquery subquery;
    
    @XmlElement(name = "aggregation-projection")
    private ExpectedAggregationProjection aggregationProjection;
    
    @XmlElement(name = "collate-expression")
    private ExpectedCollateExpression collateExpression;
    
    @XmlElement(name = "case-when-expression")
    private ExpectedCaseWhenExpression caseWhenExpression;
    
    @XmlElement(name = "type-cast-expression")
    private ExpectedTypeCastExpression typeCastExpression;
    
    @XmlElement(name = "variable-segment")
    private ExpectedVariableSegment variableSegment;
    
    @XmlElement(name = "values-expression")
    private ExpectedValuesExpression valuesExpression;
    
    @XmlElement(name = "extract-arg")
    private ExpectedExtractArgExpression extractArgExpression;
    
    @XmlElement(name = "match-expression")
    private ExpectedMatchExpression matchExpression;
    
    @XmlElement(name = "outer-join-expression")
    private ExpectedOuterJoinExpression outerJoinExpression;
    
    @XmlElement(name = "interval-expression-projection")
    private ExpectedIntervalExpressionProjection intervalExpressionProjection;
    
    @XmlElement(name = "interval-expression")
    private ExpectedIntervalExpression intervalExpression;
    
    @XmlElement(name = "quantify-subquery-expression")
    private ExpectedQuantifySubqueryExpression quantifySubqueryExpression;
    
    @XmlElement(name = "multiset-expression")
    private ExpectedMultisetExpression multisetExpression;
    
    @XmlElement(name = "row-expression")
    private ExpectedRowExpression rowExpression;
    
    @XmlElement(name = "unary-operation-expression")
    private ExpectedUnaryOperationExpression unaryOperationExpression;
    
    @XmlElement(name = "xmlquery-projection")
    private ExpectedXmlQueryAndExistsFunctionSegment expectedXmlQueryAndExistsFunctionSegment;
    
    @XmlElement(name = "key-value")
    private ExpectedKeyValueSegment keyValueSegment;
    
    @XmlElement(name = "json-null-clause-expression")
    private ExpectedJsonNullClauseSegment jsonNullClauseSegment;
}
