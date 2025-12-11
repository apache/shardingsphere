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

package org.apache.shardingsphere.sqlfederation.compiler.sql.ast.converter.segment.expression.impl;

import org.apache.calcite.sql.SqlIdentifier;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.OwnerSegment;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ColumnConverterTest {
    
    @Test
    void assertConvertColumnWithoutOwner() {
        ColumnSegment columnSegment = new ColumnSegment(0, 0, new IdentifierValue("col"));
        SqlIdentifier actual = (SqlIdentifier) ColumnConverter.convert(columnSegment).orElse(null);
        assertNotNull(actual);
        assertThat(actual.getSimple(), is("col"));
        assertThat(actual.names, is(Collections.singletonList("col")));
    }
    
    @Test
    void assertConvertColumnWithNestedOwner() {
        OwnerSegment owner = new OwnerSegment(0, 0, new IdentifierValue("schema"));
        owner.setOwner(new OwnerSegment(0, 0, new IdentifierValue("catalog")));
        ColumnSegment columnSegment = new ColumnSegment(0, 0, new IdentifierValue("col"));
        columnSegment.setOwner(owner);
        SqlIdentifier actual = (SqlIdentifier) ColumnConverter.convert(columnSegment).orElse(null);
        assertNotNull(actual);
        assertThat(actual.names, is(Arrays.asList("catalog", "schema", "col")));
    }
}
