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

package org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.segment.impl.bound;

import lombok.Getter;
import lombok.Setter;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.segment.AbstractExpectedSQLSegment;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.segment.impl.bound.column.ExpectedOriginalColumn;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.segment.impl.bound.column.ExpectedOriginalTable;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.segment.impl.bound.table.ExpectedOriginalDatabase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.segment.impl.bound.table.ExpectedOriginalSchema;

import javax.xml.bind.annotation.XmlElement;

/**
 * Expected column bound info.
 */
@Getter
@Setter
public final class ExpectedColumnBoundInfo extends AbstractExpectedSQLSegment {
    
    @XmlElement(name = "original-database")
    private ExpectedOriginalDatabase originalDatabase;
    
    @XmlElement(name = "original-schema")
    private ExpectedOriginalSchema originalSchema;
    
    @XmlElement(name = "original-table")
    private ExpectedOriginalTable originalTable;
    
    @XmlElement(name = "original-column")
    private ExpectedOriginalColumn originalColumn;
    
    @XmlElement(name = "table-source-type")
    private ExpectedTableSourceType tableSourceType;
}
