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
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.SQLParserTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.segment.impl.table.ExpectedSimpleTable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import java.util.Collection;
import java.util.LinkedList;

/**
 * Refresh materialized view statement test case for Doris.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@Getter
@Setter
public final class DorisRefreshMaterializedViewStatementTestCase extends SQLParserTestCase {
    
    @XmlElement(name = "materialized-view-name")
    private ExpectedSimpleTable materializedViewName;
    
    @XmlElement(name = "refresh-type")
    private String refreshType;
    
    @XmlElement(name = "partition-name")
    private Collection<String> partitionNames = new LinkedList<>();
}
