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

package org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.dml.dialect.doris;

import lombok.Getter;
import lombok.Setter;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.SQLParserTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.segment.impl.column.ExpectedColumnMapping;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.segment.impl.expr.ExpectedExpression;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.segment.impl.index.ExpectedPartition;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.segment.impl.orderby.ExpectedOrderByClause;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.segment.impl.table.ExpectedOwner;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.segment.impl.table.ExpectedSimpleTable;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.segment.impl.where.ExpectedWhereClause;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.dal.dialect.doris.PropertyTestCase;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import java.util.LinkedList;
import java.util.List;

/**
 * Create routine load statement test case for Doris.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@Getter
@Setter
public final class DorisCreateRoutineLoadStatementTestCase extends SQLParserTestCase {
    
    @XmlAttribute(name = "job-name")
    private String jobName;
    
    @XmlElement
    private ExpectedOwner owner;
    
    @XmlElement
    private ExpectedSimpleTable table;
    
    @XmlAttribute(name = "merge-type")
    private String mergeType;
    
    @XmlAttribute(name = "column-separator")
    private String columnSeparator;
    
    @XmlElement(name = "column-mapping")
    private final List<ExpectedColumnMapping> columnMappings = new LinkedList<>();
    
    @XmlElement(name = "partition")
    private final List<ExpectedPartition> partitions = new LinkedList<>();
    
    @XmlElement(name = "preceding-filter")
    private ExpectedExpression precedingFilter;
    
    @XmlElement(name = "where")
    private ExpectedWhereClause whereClause;
    
    @XmlElement(name = "delete-on")
    private ExpectedExpression deleteOn;
    
    @XmlElement(name = "order-by")
    private ExpectedOrderByClause orderByClause;
    
    @XmlElement(name = "job-property")
    private final List<PropertyTestCase> jobProperties = new LinkedList<>();
    
    @XmlAttribute(name = "data-source")
    private String dataSource;
    
    @XmlElement(name = "data-source-property")
    private final List<PropertyTestCase> dataSourceProperties = new LinkedList<>();
    
    @XmlAttribute
    private String comment;
}
