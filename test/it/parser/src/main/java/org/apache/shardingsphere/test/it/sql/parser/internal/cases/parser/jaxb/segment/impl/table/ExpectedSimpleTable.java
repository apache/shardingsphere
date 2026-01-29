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

package org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.segment.impl.table;

import lombok.Getter;
import lombok.Setter;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.segment.AbstractExpectedIdentifierSQLSegment;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.segment.impl.bound.ExpectedTableBoundInfo;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.segment.impl.column.ExpectedViewColumn;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import java.util.Collection;
import java.util.LinkedList;

/**
 * Expected simple table.
 */
@Getter
@Setter
public final class ExpectedSimpleTable extends AbstractExpectedIdentifierSQLSegment {
    
    @XmlAttribute
    private String alias;
    
    @XmlElement
    private ExpectedOwner owner;
    
    @XmlElement(name = "table-bound")
    private ExpectedTableBoundInfo tableBound;
    
    @XmlElement(name = "index-hint")
    private final Collection<ExpectedIndexHint> indexHints = new LinkedList<>();
    
    @XmlElement(name = "column")
    private final Collection<ExpectedViewColumn> columns = new LinkedList<>();
}
