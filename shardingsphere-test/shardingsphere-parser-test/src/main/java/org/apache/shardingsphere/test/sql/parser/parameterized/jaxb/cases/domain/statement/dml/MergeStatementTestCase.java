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

package org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.dml;

import lombok.Getter;
import lombok.Setter;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.segment.impl.expr.ExpectedExpression;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.segment.impl.table.ExpectedTable;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.SQLParserTestCase;

import javax.xml.bind.annotation.XmlElement;

/**
 * Merge statement test case.
 */
@Getter
@Setter
public final class MergeStatementTestCase extends SQLParserTestCase {
    
    @XmlElement(name = "target")
    private ExpectedTable target;
    
    @XmlElement(name = "source")
    private ExpectedTable source;
    
    @XmlElement(name = "expr")
    private ExpectedExpression expr;
    
    @XmlElement(name = "update")
    private UpdateStatementTestCase updateClause;
    
    @XmlElement(name = "delete")
    private DeleteStatementTestCase deleteClause;
}
