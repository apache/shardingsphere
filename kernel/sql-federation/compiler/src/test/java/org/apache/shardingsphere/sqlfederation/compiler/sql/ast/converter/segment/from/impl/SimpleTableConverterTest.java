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

import org.apache.calcite.sql.SqlBasicCall;
import org.apache.calcite.sql.SqlIdentifier;
import org.apache.calcite.sql.fun.SqlStdOperatorTable;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.AliasSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.OwnerSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.TableNameSegment;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class SimpleTableConverterTest {
    
    @Test
    void assertConvertReturnsEmptyForDualTable() {
        SimpleTableSegment segment = new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("dual")));
        assertFalse(SimpleTableConverter.convert(segment).isPresent());
    }
    
    @Test
    void assertConvertReturnsIdentifierWithoutOwnerAliasOrDbLink() {
        SimpleTableSegment segment = new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("t_simple")));
        SqlIdentifier actual = SimpleTableConverter.convert(segment).map(SqlIdentifier.class::cast).orElse(null);
        assertNotNull(actual);
        assertThat(actual.names, is(Collections.singletonList("t_simple")));
    }
    
    @Test
    void assertConvertReturnsAliasedIdentifierWithOwnerAndDbLink() {
        SimpleTableSegment segment = new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("t_complex")));
        segment.setOwner(new OwnerSegment(0, 0, new IdentifierValue("schema")));
        segment.setDbLink(new IdentifierValue("remote"));
        segment.setAt(new IdentifierValue("db"));
        segment.setAlias(new AliasSegment(0, 0, new IdentifierValue("t_alias")));
        SqlBasicCall actual = SimpleTableConverter.convert(segment).map(SqlBasicCall.class::cast).orElse(null);
        assertNotNull(actual);
        assertThat(actual.getOperator(), is(SqlStdOperatorTable.AS));
        assertThat(((SqlIdentifier) actual.getOperandList().get(0)).names, is(Arrays.asList("schema", "t_complex", "db", "remote")));
        assertThat(((SqlIdentifier) actual.getOperandList().get(1)).names, is(Collections.singletonList("t_alias")));
        assertThat(actual.getOperandList().get(0), instanceOf(SqlIdentifier.class));
    }
}
