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

package org.apache.shardingsphere.sql.parser.sql.common;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertNull;

import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.TableNameSegment;
import org.apache.shardingsphere.sql.parser.sql.common.value.identifier.IdentifierValue;
import org.junit.Test;

public final class SQLStatsTest {
    
    @Test
    public void assertAddTable() {
        SQLStats sqlStats = new SQLStats();
        assertNull(sqlStats.getTables().get("foo"));
        SimpleTableSegment tableSegment = new SimpleTableSegment(new TableNameSegment(0, 5, new IdentifierValue("foo")));
        sqlStats.addTable(tableSegment);
        assertThat(sqlStats.getTables().get("foo"), is(tableSegment));
        sqlStats.addTable(tableSegment);
        assertThat(sqlStats.getTables().get("foo"), is(tableSegment));
    }
    
    @Test
    public void assertAddColumn() {
        SQLStats sqlStats = new SQLStats();
        ColumnSegment columnSegment = new ColumnSegment(0, 5, new IdentifierValue("foo"));
        assertNull(sqlStats.getColumns().get(columnSegment.hashCode()));
        sqlStats.addColumn(columnSegment);
        assertThat(sqlStats.getColumns().get(columnSegment.hashCode()), is(columnSegment));
        sqlStats.addColumn(columnSegment);
        assertThat(sqlStats.getColumns().get(columnSegment.hashCode()), is(columnSegment));
    }
}
