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

package org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.dialect.doris;

import lombok.Getter;
import lombok.Setter;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.segment.impl.column.ExpectedColumn;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.segment.impl.column.ExpectedMaterializedViewColumn;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.segment.impl.comments.ExpectedComment;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.segment.impl.distsql.ExpectedProperties;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.segment.impl.partition.ExpectedPartitionFunction;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.segment.impl.table.ExpectedSimpleTable;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.SQLParserTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.dml.standard.SelectStatementTestCase;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import java.util.LinkedList;
import java.util.List;

/**
 * Doris create materialized view statement test case.
 */
@Getter
@Setter
public final class DorisCreateMaterializedViewStatementTestCase extends SQLParserTestCase {
    
    @XmlElement(name = "materialized-view")
    private ExpectedSimpleTable materializedView;
    
    @XmlAttribute(name = "if-not-exists")
    private Boolean ifNotExists;
    
    @XmlElement(name = "column")
    private final List<ExpectedMaterializedViewColumn> columns = new LinkedList<>();
    
    @XmlAttribute(name = "build-mode")
    private String buildMode;
    
    @XmlAttribute(name = "refresh-method")
    private String refreshMethod;
    
    @XmlAttribute(name = "refresh-trigger")
    private String refreshTrigger;
    
    @XmlAttribute(name = "refresh-interval")
    private Integer refreshInterval;
    
    @XmlAttribute(name = "refresh-unit")
    private String refreshUnit;
    
    @XmlAttribute(name = "start-time")
    private String startTime;
    
    @XmlAttribute(name = "duplicate-key")
    private Boolean duplicateKey;
    
    @XmlElement(name = "key-column")
    private final List<ExpectedColumn> keyColumns = new LinkedList<>();
    
    @XmlElement(name = "comment")
    private ExpectedComment comment;
    
    @XmlElement(name = "partition-column")
    private ExpectedColumn partitionColumn;
    
    @XmlElement(name = "partition-function")
    private ExpectedPartitionFunction partitionFunction;
    
    @XmlAttribute(name = "distribute-type")
    private String distributeType;
    
    @XmlElementWrapper(name = "distribute-columns")
    @XmlElement(name = "column")
    private List<String> distributeColumns = new LinkedList<>();
    
    @XmlAttribute(name = "bucket-count")
    private Integer bucketCount;
    
    @XmlAttribute(name = "auto-bucket")
    private Boolean autoBucket;
    
    @XmlElement(name = "properties")
    private ExpectedProperties properties;
    
    @XmlElement(name = "select")
    private SelectStatementTestCase selectClause;
}
