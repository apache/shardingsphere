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
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.segment.impl.insert.ExpectedInsertColumnsClause;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.segment.impl.insert.ExpectedInsertValuesClause;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.segment.impl.insert.ExpectedOnDuplicateKeyColumns;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.segment.impl.set.ExpectedSetClause;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.segment.impl.table.ExpectedSimpleTable;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.SQLParserTestCase;

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
    
    @XmlElement(name = "values")
    private ExpectedInsertValuesClause insertValuesClause;
    
    @XmlElement(name = "set")
    private ExpectedSetClause setClause;
    
    @XmlElement(name = "select")
    private SelectStatementTestCase selectTestCase;
    
    @XmlElement(name = "on-duplicate-key-columns")
    private ExpectedOnDuplicateKeyColumns onDuplicateKeyColumns;
}
