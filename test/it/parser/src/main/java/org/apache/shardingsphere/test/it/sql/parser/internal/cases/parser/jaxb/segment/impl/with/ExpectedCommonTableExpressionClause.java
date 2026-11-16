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

package org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.segment.impl.with;

import lombok.Getter;
import lombok.Setter;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.segment.AbstractExpectedSQLSegment;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.segment.impl.column.ExpectedColumn;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.segment.impl.expr.simple.ExpectedSubquery;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.dml.standard.DeleteStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.dml.standard.InsertStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.dml.standard.MergeStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.dml.standard.UpdateStatementTestCase;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import java.util.LinkedList;
import java.util.List;

/**
 * Expected common table expression clause.
 */
@Getter
@Setter
public final class ExpectedCommonTableExpressionClause extends AbstractExpectedSQLSegment {
    
    @XmlAttribute
    private String name;
    
    @XmlElement(name = "column")
    private final List<ExpectedColumn> columns = new LinkedList<>();
    
    @XmlAttribute(name = "search-column-count")
    private Integer searchColumnCount;
    
    @XmlElement(name = "search-column")
    private final List<ExpectedColumn> searchColumns = new LinkedList<>();
    
    @XmlAttribute(name = "cycle-column-count")
    private Integer cycleColumnCount;
    
    @XmlElement(name = "cycle-column")
    private final List<ExpectedColumn> cycleColumns = new LinkedList<>();
    
    @XmlElement(name = "subquery-expression")
    private ExpectedSubquery subquery;
    
    @XmlElement(name = "insert")
    private InsertStatementTestCase insert;
    
    @XmlElement(name = "update")
    private UpdateStatementTestCase update;
    
    @XmlElement(name = "delete")
    private DeleteStatementTestCase delete;
    
    @XmlElement(name = "merge")
    private MergeStatementTestCase merge;
}
