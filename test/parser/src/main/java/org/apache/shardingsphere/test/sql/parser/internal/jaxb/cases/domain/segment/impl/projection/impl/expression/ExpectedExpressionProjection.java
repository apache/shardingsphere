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

package org.apache.shardingsphere.test.sql.parser.internal.jaxb.cases.domain.segment.impl.projection.impl.expression;

import lombok.Getter;
import lombok.Setter;
import org.apache.shardingsphere.test.sql.parser.internal.jaxb.cases.domain.segment.AbstractExpectedSQLSegment;
import org.apache.shardingsphere.test.sql.parser.internal.jaxb.cases.domain.segment.impl.expr.ExpectedExpression;
import org.apache.shardingsphere.test.sql.parser.internal.jaxb.cases.domain.segment.impl.expr.complex.ExpectedComplexExpressionSegment;
import org.apache.shardingsphere.test.sql.parser.internal.jaxb.cases.domain.segment.impl.projection.ExpectedProjection;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

@Getter
@Setter
public final class ExpectedExpressionProjection extends AbstractExpectedSQLSegment implements ExpectedProjection, ExpectedComplexExpressionSegment {
    
    @XmlAttribute(name = "text")
    private String text;
    
    @XmlAttribute(name = "literal-text")
    private String literalText;
    
    @XmlAttribute(name = "alias")
    private String alias;
    
    @XmlElement(name = "expr")
    private ExpectedExpression expr;
}
