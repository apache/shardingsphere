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

package org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.standard.function;

import lombok.Getter;
import lombok.Setter;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.SQLParserTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.segment.impl.expr.ExpectedExpression;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.segment.impl.distsql.ExpectedProperty;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.segment.impl.plsql.ExpectedDynamicSqlStatementExpressionSegment;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.segment.impl.plsql.ExpectedProcedureCallNameSegment;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.segment.impl.plsql.ExpectedRoutineName;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.segment.impl.plsql.ExpectedSQLStatementSegment;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.segment.impl.type.ExpectedDataTypeSegment;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import java.util.LinkedList;
import java.util.List;

/**
 * Create function statement test case.
 */
@Getter
@Setter
public final class CreateFunctionStatementTestCase extends SQLParserTestCase {
    
    @XmlElement(name = "function-name")
    private ExpectedRoutineName functionName;
    
    @XmlElementWrapper(name = "sql-statements")
    @XmlElement(name = "sql-statement")
    private List<ExpectedSQLStatementSegment> sqlStatements = new LinkedList<>();
    
    @XmlElementWrapper(name = "procedure-calls")
    @XmlElement(name = "procedure-call")
    private List<ExpectedProcedureCallNameSegment> procedureCalls = new LinkedList<>();
    
    @XmlElementWrapper(name = "dynamic-sql-statement-expressions")
    @XmlElement(name = "dynamic-sql-statement-expression")
    private List<ExpectedDynamicSqlStatementExpressionSegment> dynamicSqlStatementExpressions = new LinkedList<>();
    
    @XmlAttribute(name = "global")
    private Boolean global;
    
    @XmlAttribute(name = "function-type")
    private String functionType;
    
    @XmlElementWrapper(name = "parameter-data-types")
    @XmlElement(name = "data-type")
    private List<ExpectedDataTypeSegment> parameterDataTypes = new LinkedList<>();
    
    @XmlElement(name = "return-type")
    private ExpectedDataTypeSegment returnType;
    
    @XmlElement(name = "intermediate-type")
    private ExpectedDataTypeSegment intermediateType;
    
    @XmlElementWrapper(name = "with-parameters")
    @XmlElement(name = "parameter")
    private List<String> withParameters = new LinkedList<>();
    
    @XmlElement(name = "alias-expression")
    private ExpectedExpression aliasExpression;
    
    @XmlElementWrapper(name = "properties")
    @XmlElement(name = "property")
    private List<ExpectedProperty> properties = new LinkedList<>();
}
