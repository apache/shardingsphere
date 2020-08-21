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

package org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.segment.impl.predicate.value;

import lombok.Getter;
import lombok.Setter;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.segment.impl.expr.complex.ExpectedCommonExpression;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.segment.impl.expr.simple.ExpectedSubquery;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.segment.impl.expr.simple.ExpectedLiteralExpression;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.segment.impl.expr.simple.ExpectedParameterMarkerExpression;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

@Getter
@Setter
@XmlAccessorType(XmlAccessType.FIELD)
public final class ExpectedPredicateBetweenRightValue implements ExpectedPredicateRightValue {
    
    @XmlElement(name = "between-parameter-marker-expression")
    private ExpectedParameterMarkerExpression betweenParameterMarkerExpression;
    
    @XmlElement(name = "between-literal-expression")
    private ExpectedLiteralExpression betweenLiteralExpression;
    
    @XmlElement(name = "between-common-expression")
    private ExpectedCommonExpression betweenCommonExpression;
    
    @XmlElement(name = "between-subquery-expression")
    private ExpectedSubquery betweenSubquery;
    
    @XmlElement(name = "and-parameter-marker-expression")
    private ExpectedParameterMarkerExpression andParameterMarkerExpression;
    
    @XmlElement(name = "and-literal-expression")
    private ExpectedLiteralExpression andLiteralExpression;
    
    @XmlElement(name = "and-common-expression")
    private ExpectedCommonExpression andCommonExpression;
    
    @XmlElement(name = "and-subquery-expression")
    private ExpectedSubquery andSubquery;
}
