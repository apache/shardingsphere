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
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.segment.AbstractExpectedSQLSegment;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.segment.impl.assignment.ExpectedAssignment;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.segment.impl.column.ExpectedColumn;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.segment.impl.expr.ExpectedExpression;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.segment.impl.index.ExpectedPartition;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.segment.impl.table.ExpectedSimpleTable;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.dal.dialect.doris.PropertyTestCase;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import java.util.LinkedList;
import java.util.List;

/**
 * Broker load data desc test case for Doris.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@Getter
@Setter
public final class BrokerLoadDataDescTestCase extends AbstractExpectedSQLSegment {
    
    @XmlAttribute(name = "merge-type")
    private String mergeType;
    
    @XmlAttribute(name = "file-paths")
    private String filePaths;
    
    @XmlAttribute(name = "negative")
    private Boolean negative;
    
    @XmlElement(name = "table")
    private ExpectedSimpleTable table;
    
    @XmlElement(name = "partition")
    private final List<ExpectedPartition> partitions = new LinkedList<>();
    
    @XmlAttribute(name = "column-separator")
    private String columnSeparator;
    
    @XmlAttribute(name = "line-delimiter")
    private String lineDelimiter;
    
    @XmlAttribute(name = "format-type")
    private String formatType;
    
    @XmlAttribute(name = "compress-type")
    private String compressType;
    
    @XmlElement(name = "column")
    private final List<ExpectedColumn> columnList = new LinkedList<>();
    
    @XmlElement(name = "column-from-path")
    private final List<ExpectedColumn> columnsFromPath = new LinkedList<>();
    
    @XmlElement(name = "set-assignment")
    private final List<ExpectedAssignment> setAssignments = new LinkedList<>();
    
    @XmlElement(name = "preceding-filter")
    private ExpectedExpression precedingFilter;
    
    @XmlElement(name = "where-expr")
    private ExpectedExpression whereExpr;
    
    @XmlElement(name = "delete-on-expr")
    private ExpectedExpression deleteOnExpr;
    
    @XmlAttribute(name = "order-by-column")
    private String orderByColumn;
    
    @XmlElement(name = "data-property")
    private final List<PropertyTestCase> dataProperties = new LinkedList<>();
}
