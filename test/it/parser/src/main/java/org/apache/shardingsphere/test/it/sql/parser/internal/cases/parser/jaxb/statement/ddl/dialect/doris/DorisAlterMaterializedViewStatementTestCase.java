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
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.segment.impl.distsql.ExpectedProperties;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.segment.impl.table.ExpectedSimpleTable;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.SQLParserTestCase;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

/**
 * Doris alter materialized view statement test case.
 */
@Getter
@Setter
public final class DorisAlterMaterializedViewStatementTestCase extends SQLParserTestCase {
    
    @XmlElement(name = "materialized-view")
    private ExpectedSimpleTable materializedView;
    
    @XmlAttribute(name = "rename-value")
    private String renameValue;
    
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
    
    @XmlAttribute(name = "replace-with-view")
    private String replaceWithView;
    
    @XmlElement(name = "replace-properties")
    private ExpectedProperties replaceProperties;
    
    @XmlElement(name = "set-properties")
    private ExpectedProperties setProperties;
}
