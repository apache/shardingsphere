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

package org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.ddl;

import lombok.Getter;
import lombok.Setter;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.segment.impl.column.ExpectedColumn;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.segment.impl.definition.ExpectedAddColumnDefinition;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.segment.impl.definition.ExpectedConstraintDefinition;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.segment.impl.definition.ExpectedModifyColumnDefinition;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.segment.impl.table.ExpectedSimpleTable;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.statement.SQLParserTestCase;

import javax.xml.bind.annotation.XmlElement;
import java.util.LinkedList;
import java.util.List;

/**
 * Alter table statement test case.
 */
@Getter
@Setter
public final class AlterTableStatementTestCase extends SQLParserTestCase {
    
    @XmlElement(name = "table")
    private ExpectedSimpleTable table;
    
    @XmlElement(name = "add-column")
    private final List<ExpectedAddColumnDefinition> addColumns = new LinkedList<>();
    
    @XmlElement(name = "add-constraint")
    private final List<ExpectedConstraintDefinition> addConstraints = new LinkedList<>();
    
    @XmlElement(name = "modify-column")
    private final List<ExpectedModifyColumnDefinition> modifyColumns = new LinkedList<>();
    
    @XmlElement(name = "drop-column")
    private final List<ExpectedColumn> dropColumns = new LinkedList<>();
}
