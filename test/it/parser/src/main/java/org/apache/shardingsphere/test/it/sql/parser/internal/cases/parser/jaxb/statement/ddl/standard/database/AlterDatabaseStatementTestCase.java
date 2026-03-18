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

package org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.standard.database;

import lombok.Getter;
import lombok.Setter;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.SQLParserTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.dal.dialect.doris.PropertyTestCase;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import java.util.LinkedList;
import java.util.List;

/**
 * Alter database statement test case.
 */
@Getter
@Setter
public final class AlterDatabaseStatementTestCase extends SQLParserTestCase {
    
    @XmlAttribute(name = "database-name")
    private String databaseName;
    
    @XmlAttribute(name = "rename-database-name")
    private String renameDatabaseName;
    
    @XmlAttribute(name = "quota-type")
    private String quotaType;
    
    @XmlAttribute(name = "quota-value")
    private Long quotaValue;
    
    @XmlElement(name = "property")
    private final List<PropertyTestCase> properties = new LinkedList<>();
}
