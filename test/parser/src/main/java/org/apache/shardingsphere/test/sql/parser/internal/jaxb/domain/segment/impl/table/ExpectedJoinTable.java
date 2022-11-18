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

package org.apache.shardingsphere.test.sql.parser.internal.jaxb.domain.segment.impl.table;

import lombok.Getter;
import lombok.Setter;
import org.apache.shardingsphere.test.sql.parser.internal.jaxb.domain.segment.AbstractExpectedDelimiterSQLSegment;
import org.apache.shardingsphere.test.sql.parser.internal.jaxb.domain.segment.impl.column.ExpectedColumn;
import org.apache.shardingsphere.test.sql.parser.internal.jaxb.domain.segment.impl.expr.ExpectedExpression;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import java.util.LinkedList;
import java.util.List;

/**
 * Expected JoinTable.
 */
@Getter
@Setter
public final class ExpectedJoinTable extends AbstractExpectedDelimiterSQLSegment {
    
    @XmlElement(name = "left")
    private ExpectedTable left;
    
    @XmlElement(name = "right")
    private ExpectedTable right;
    
    @XmlElement(name = "on-condition")
    private ExpectedExpression onCondition;
    
    @XmlElement(name = "using-columns")
    private final List<ExpectedColumn> usingColumns = new LinkedList<>();
    
    @XmlAttribute(name = "join-type")
    private String joinType;
}
