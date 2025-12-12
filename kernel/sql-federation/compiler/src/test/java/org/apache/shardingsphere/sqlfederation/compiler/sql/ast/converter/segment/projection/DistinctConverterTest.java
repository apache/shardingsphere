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

package org.apache.shardingsphere.sqlfederation.compiler.sql.ast.converter.segment.projection;

import org.apache.calcite.sql.SqlNodeList;
import org.apache.calcite.sql.SqlSelectKeyword;
import org.apache.calcite.sql.parser.SqlParserPos;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ProjectionsSegment;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DistinctConverterTest {
    
    @Test
    void assertConvertReturnsDistinctNodeListWhenFlagPresent() {
        ProjectionsSegment projectionsSegment = new ProjectionsSegment(0, 0);
        projectionsSegment.setDistinctRow(true);
        Optional<SqlNodeList> actual = DistinctConverter.convert(projectionsSegment);
        assertTrue(actual.isPresent());
        SqlNodeList expected = new SqlNodeList(Collections.singletonList(SqlSelectKeyword.DISTINCT.symbol(SqlParserPos.ZERO)), SqlParserPos.ZERO);
        assertThat(actual.get(), is(expected));
    }
    
    @Test
    void assertConvertReturnsEmptyWhenFlagAbsent() {
        assertFalse(DistinctConverter.convert(new ProjectionsSegment(0, 0)).isPresent());
    }
}
