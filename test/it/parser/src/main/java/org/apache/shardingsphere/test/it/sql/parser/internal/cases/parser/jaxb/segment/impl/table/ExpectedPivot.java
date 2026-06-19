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
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.segment.AbstractExpectedSQLSegment;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.segment.impl.column.ExpectedColumn;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import java.util.Collection;
import java.util.LinkedList;

/**
 * Expected pivot.
 */
@Getter
public final class ExpectedPivot extends AbstractExpectedSQLSegment {
    
    @XmlAttribute(name = "unpivot")
    private boolean unpivot;
    
    @XmlAttribute(name = "xml")
    private boolean xml;
    
    @XmlElementWrapper(name = "aggregation-columns")
    @XmlElement(name = "column")
    private final Collection<ExpectedColumn> aggregationColumns = new LinkedList<>();
    
    @XmlElementWrapper(name = "for-columns")
    @XmlElement(name = "column")
    private final Collection<ExpectedColumn> forColumns = new LinkedList<>();
    
    @XmlElementWrapper(name = "in-columns")
    @XmlElement(name = "column")
    private final Collection<ExpectedColumn> inColumns = new LinkedList<>();
    
    @XmlElementWrapper(name = "unpivot-columns")
    @XmlElement(name = "column")
    private final Collection<ExpectedColumn> unpivotColumns = new LinkedList<>();
}
