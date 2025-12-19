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

package org.apache.shardingsphere.sqlfederation.compiler.sql.ast.converter.segment.from.impl;

import org.apache.calcite.sql.SqlIdentifier;
import org.apache.calcite.sql.SqlNodeList;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.DeleteMultiTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.TableNameSegment;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class DeleteMultiTableConverterTest {
    
    @Test
    void assertConvertReturnsEmptyForNullSegment() {
        assertFalse(DeleteMultiTableConverter.convert(null).isPresent());
    }
    
    @Test
    void assertConvertAddsRelationTableAndActualDeleteTables() {
        DeleteMultiTableSegment segment = new DeleteMultiTableSegment();
        segment.setRelationTable(new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("t_relation"))));
        segment.getActualDeleteTables().add(new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("t_order"))));
        segment.getActualDeleteTables().add(new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("t_item"))));
        SqlNodeList actual = (SqlNodeList) DeleteMultiTableConverter.convert(segment).orElse(null);
        assertNotNull(actual);
        SqlIdentifier relationIdentifier = (SqlIdentifier) actual.get(0);
        assertThat(relationIdentifier.names, is(Collections.singletonList("t_relation")));
        SqlIdentifier deleteIdentifier = (SqlIdentifier) actual.get(1);
        assertThat(deleteIdentifier.names, is(Arrays.asList("t_order", "t_item")));
    }
}
