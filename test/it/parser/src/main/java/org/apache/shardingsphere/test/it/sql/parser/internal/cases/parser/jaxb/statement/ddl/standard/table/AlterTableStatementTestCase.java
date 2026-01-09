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

package org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.standard.table;

import lombok.Getter;
import lombok.Setter;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.SQLParserTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.segment.impl.column.ExpectedColumn;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.segment.impl.definition.ExpectedAddColumnDefinition;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.segment.impl.definition.ExpectedChangeColumnDefinition;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.segment.impl.definition.ExpectedConstraintDefinition;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.segment.impl.definition.ExpectedConvertTableDefinition;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.segment.impl.definition.ExpectedDropPrimaryKeyDefinition;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.segment.impl.definition.ExpectedModifyCollectionRetrievalDefinition;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.segment.impl.definition.ExpectedModifyColumnDefinition;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.segment.impl.definition.ExpectedRenameColumnDefinition;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.segment.impl.definition.ExpectedRenameIndexDefinition;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.segment.impl.definition.ExpectedRenamePartitionDefinition;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.segment.impl.definition.ExpectedRenameRollupDefinition;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.segment.impl.table.ExpectedSimpleTable;

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
    
    @XmlElement(name = "rename-table")
    private ExpectedSimpleTable renameTable;
    
    @XmlElement(name = "convert-table")
    private ExpectedConvertTableDefinition convertTable;
    
    @XmlElement(name = "add-column")
    private final List<ExpectedAddColumnDefinition> addColumns = new LinkedList<>();
    
    @XmlElement(name = "add-constraint")
    private final List<ExpectedConstraintDefinition> addConstraints = new LinkedList<>();
    
    @XmlElement(name = "modify-constraint")
    private final List<ExpectedConstraintDefinition> modifyConstraints = new LinkedList<>();
    
    @XmlElement(name = "modify-column")
    private final List<ExpectedModifyColumnDefinition> modifyColumns = new LinkedList<>();
    
    @XmlElement(name = "change-column")
    private final List<ExpectedChangeColumnDefinition> changeColumns = new LinkedList<>();
    
    @XmlElement(name = "rename-index")
    private final List<ExpectedRenameIndexDefinition> renameIndexes = new LinkedList<>();
    
    @XmlElement(name = "rename-column")
    private final List<ExpectedRenameColumnDefinition> renameColumns = new LinkedList<>();
    
    @XmlElement(name = "rename-rollup")
    private final List<ExpectedRenameRollupDefinition> renameRollups = new LinkedList<>();
    
    @XmlElement(name = "rename-partition")
    private final List<ExpectedRenamePartitionDefinition> renamePartitions = new LinkedList<>();
    
    @XmlElement(name = "drop-column")
    private final List<ExpectedColumn> dropColumns = new LinkedList<>();
    
    @XmlElement(name = "modify-collection-retrieval")
    private ExpectedModifyCollectionRetrievalDefinition modifyCollectionRetrievalDefinition;
    
    @XmlElement(name = "drop-primary-key")
    private ExpectedDropPrimaryKeyDefinition dropPrimaryKeyDefinition;
}
