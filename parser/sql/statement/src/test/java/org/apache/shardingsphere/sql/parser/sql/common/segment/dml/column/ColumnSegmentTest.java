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

package org.apache.shardingsphere.sql.parser.sql.common.segment.dml.column;

import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.OwnerSegment;
import org.apache.shardingsphere.sql.parser.sql.common.value.identifier.IdentifierValue;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

class ColumnSegmentTest {
    
    @Test
    void assertGetQualifiedNameWithoutOwner() {
        assertThat(new ColumnSegment(0, 0, new IdentifierValue("col")).getQualifiedName(), is("col"));
    }
    
    @Test
    void assertGetQualifiedNameWithOwner() {
        ColumnSegment actual = new ColumnSegment(0, 0, new IdentifierValue("col"));
        actual.setOwner(new OwnerSegment(0, 0, new IdentifierValue("tbl")));
        assertThat(actual.getQualifiedName(), is("tbl.col"));
    }
    
    @Test
    void assertGetExpressionWithoutOwner() {
        assertThat(new ColumnSegment(0, 0, new IdentifierValue("`col`")).getExpression(), is("col"));
    }
    
    @Test
    void assertGetExpressionWithOwner() {
        ColumnSegment actual = new ColumnSegment(0, 0, new IdentifierValue("`col`"));
        actual.setOwner(new OwnerSegment(0, 0, new IdentifierValue("`tbl`")));
        assertThat(actual.getExpression(), is("tbl.col"));
    }
}
