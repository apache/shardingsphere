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

package org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.dml.standard;

import lombok.Getter;
import lombok.Setter;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.SQLParserTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.segment.impl.exec.ExpectedExecClause;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.segment.impl.function.ExpectedFunction;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.segment.impl.hint.ExpectedWithTableHintClause;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.segment.impl.insert.ExpectedDerivedInsertColumns;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.segment.impl.insert.ExpectedInsertColumnsClause;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.segment.impl.insert.ExpectedInsertValuesClause;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.segment.impl.insert.ExpectedMultiTableConditionalIntoClause;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.segment.impl.insert.ExpectedMultiTableInsertIntoClause;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.segment.impl.insert.ExpectedMultiTableInsertType;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.segment.impl.insert.ExpectedOnDuplicateKeyColumns;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.segment.impl.insert.ExpectedReturningClause;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.segment.impl.assignment.ExpectedValueReference;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.segment.impl.output.ExpectedOutputClause;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.segment.impl.set.ExpectedSetClause;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.segment.impl.table.ExpectedSimpleTable;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.segment.impl.where.ExpectedWhereClause;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.segment.impl.with.ExpectedWithClause;

import javax.xml.bind.annotation.XmlElement;

/**
 * Insert statement test case.
 */
@Getter
@Setter
public final class InsertStatementTestCase extends SQLParserTestCase {
    
    @XmlElement
    private ExpectedSimpleTable table;
    
    @XmlElement(name = "columns")
    private ExpectedInsertColumnsClause insertColumnsClause;
    
    @XmlElement(name = "derived-columns")
    private ExpectedDerivedInsertColumns derivedInsertColumns;
    
    @XmlElement(name = "values")
    private ExpectedInsertValuesClause insertValuesClause;
    
    @XmlElement(name = "set")
    private ExpectedSetClause setClause;
    
    @XmlElement(name = "select")
    private SelectStatementTestCase selectTestCase;
    
    @XmlElement(name = "on-duplicate-key-columns")
    private ExpectedOnDuplicateKeyColumns onDuplicateKeyColumns;
    
    @XmlElement(name = "value-reference")
    private ExpectedValueReference valueReference;
    
    @XmlElement(name = "with")
    private ExpectedWithClause withClause;
    
    @XmlElement(name = "output")
    private ExpectedOutputClause outputClause;
    
    @XmlElement(name = "multi-table-insert-type")
    private ExpectedMultiTableInsertType multiTableInsertType;
    
    @XmlElement(name = "multi-table-insert-into")
    private ExpectedMultiTableInsertIntoClause multiTableInsertInto;
    
    @XmlElement(name = "multi-table-conditional-into")
    private ExpectedMultiTableConditionalIntoClause multiTableConditionalInto;
    
    @XmlElement(name = "select-subquery")
    private SelectStatementTestCase selectSubquery;
    
    @XmlElement(name = "returning")
    private ExpectedReturningClause returningClause;
    
    @XmlElement(name = "where")
    private ExpectedWhereClause whereClause;
    
    @XmlElement(name = "exec")
    private ExpectedExecClause execClause;
    
    @XmlElement(name = "table-hints")
    private ExpectedWithTableHintClause expectedWithTableHintClause;
    
    @XmlElement(name = "rowset-function")
    private ExpectedFunction expectedRowSetFunctionClause;
}
