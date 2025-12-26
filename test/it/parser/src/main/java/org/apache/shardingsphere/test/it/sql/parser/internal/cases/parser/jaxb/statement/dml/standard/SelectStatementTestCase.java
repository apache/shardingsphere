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

package org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.dml.standard;

import lombok.Getter;
import lombok.Setter;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.SQLParserTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.segment.impl.having.ExpectedHavingClause;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.segment.impl.limit.ExpectedLimitClause;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.segment.impl.lock.ExpectedLockClause;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.segment.impl.model.ExpectedModelClause;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.segment.impl.orderby.ExpectedOrderByClause;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.segment.impl.outfile.ExpectedOutfileClause;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.segment.impl.projection.ExpectedProjections;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.segment.impl.table.ExpectedTable;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.segment.impl.union.ExpectedCombine;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.segment.impl.where.ExpectedWhereClause;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.segment.impl.window.ExpectedWindowClause;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.segment.impl.with.ExpectedWithClause;

import javax.xml.bind.annotation.XmlElement;

/**
 * Select statement test case.
 */
@Getter
@Setter
public final class SelectStatementTestCase extends SQLParserTestCase {
    
    @XmlElement
    private ExpectedTable from;
    
    @XmlElement
    private final ExpectedProjections projections = new ExpectedProjections();
    
    @XmlElement(name = "where")
    private ExpectedWhereClause whereClause;
    
    @XmlElement(name = "group-by")
    private ExpectedOrderByClause groupByClause;
    
    @XmlElement(name = "order-by")
    private ExpectedOrderByClause orderByClause;
    
    @XmlElement(name = "having")
    private ExpectedHavingClause havingClause;
    
    @XmlElement(name = "window")
    private ExpectedWindowClause windowClause;
    
    @XmlElement(name = "limit")
    private ExpectedLimitClause limitClause;
    
    @XmlElement(name = "lock")
    private ExpectedLockClause lockClause;
    
    @XmlElement(name = "with")
    private ExpectedWithClause withClause;
    
    @XmlElement(name = "combine")
    private ExpectedCombine combineClause;
    
    @XmlElement(name = "model")
    private ExpectedModelClause modelClause;
    
    @XmlElement(name = "into")
    private ExpectedTable intoClause;
    
    @XmlElement(name = "outfile")
    private ExpectedOutfileClause outfileClause;
}
