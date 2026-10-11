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

package org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl;

import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.TableNameSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.attribute.type.TableSQLStatementAttribute;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.MergeStatement;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class PrepareStatementTest {
    
    @Test
    void assertMergeBodyAndTableAttribute() {
        SimpleTableSegment target = new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("foo_target")));
        SimpleTableSegment source = new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("foo_source")));
        MergeStatement merge = MergeStatement.builder().target(target).source(source).build();
        PrepareStatement actual = PrepareStatement.builder().merge(merge).build();
        assertThat(actual.getMerge().orElse(null), is(merge));
        assertThat(actual.getAttributes().getAttribute(TableSQLStatementAttribute.class).getTables(), is(Arrays.asList(target, source)));
    }
}
