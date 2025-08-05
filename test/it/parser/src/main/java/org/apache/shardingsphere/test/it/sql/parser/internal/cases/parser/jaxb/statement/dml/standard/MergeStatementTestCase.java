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
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.segment.impl.expr.ExpectedExpression;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.segment.impl.expr.ExpectedMergeWhenAndThenSegment;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.segment.impl.hint.ExpectedWithTableHintClause;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.segment.impl.index.ExpectedIndex;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.segment.impl.output.ExpectedOutputClause;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.segment.impl.table.ExpectedTable;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.segment.impl.with.ExpectedWithClause;

import javax.xml.bind.annotation.XmlElement;
import java.util.LinkedList;
import java.util.List;

/**
 * Merge statement test case.
 */
@Getter
@Setter
public final class MergeStatementTestCase extends SQLParserTestCase {
    
    @XmlElement
    private ExpectedTable target;
    
    @XmlElement
    private ExpectedTable source;
    
    @XmlElement
    private ExpectedExpression expr;
    
    @XmlElement(name = "update")
    private UpdateStatementTestCase updateClause;
    
    @XmlElement(name = "insert")
    private InsertStatementTestCase insertClause;
    
    @XmlElement(name = "merge-items")
    private List<ExpectedMergeWhenAndThenSegment> mergeWhenAndThenSegments = new LinkedList<>();
    
    @XmlElement(name = "with")
    private ExpectedWithClause withClause;
    
    @XmlElement(name = "table-hints")
    private ExpectedWithTableHintClause expectedWithTableHintClause;
    
    @XmlElement(name = "output")
    private ExpectedOutputClause outputClause;
    
    @XmlElement(name = "index")
    private List<ExpectedIndex> indexs = new LinkedList<>();
}
