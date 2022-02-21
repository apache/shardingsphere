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

package org.apache.shardingsphere.sql.parser.sql.common.extractor;

import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.predicate.LockSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.TableNameSegment;
import org.apache.shardingsphere.sql.parser.sql.common.value.identifier.IdentifierValue;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dml.MySQLSelectStatement;
import org.junit.Test;

import java.util.Iterator;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public final class TableExtractorTest {
    
    private final TableExtractor tableExtractor = new TableExtractor();
    
    @Test
    public void assertExtractTablesFromSelectLockWithEmptyValue() {
        MySQLSelectStatement selectStatement = new MySQLSelectStatement();
        tableExtractor.extractTablesFromSelect(selectStatement);
        assertTrue(tableExtractor.getRewriteTables().isEmpty());
    }
    
    @Test
    public void assertExtractTablesFromSelectLockWithValue() {
        MySQLSelectStatement selectStatement = new MySQLSelectStatement();
        LockSegment lockSegment = new LockSegment(108, 154);
        selectStatement.setLock(lockSegment);
        lockSegment.getTables().add(new SimpleTableSegment(new TableNameSegment(122, 128, new IdentifierValue("t_order"))));
        lockSegment.getTables().add(new SimpleTableSegment(new TableNameSegment(143, 154, new IdentifierValue("t_order_item"))));
        tableExtractor.extractTablesFromSelect(selectStatement);
        assertNotNull(tableExtractor.getRewriteTables());
        assertThat(tableExtractor.getRewriteTables().size(), is(2));
        Iterator<SimpleTableSegment> tableSegmentIterator = tableExtractor.getRewriteTables().iterator();
        assertTableSegment(tableSegmentIterator.next(), 122, 128, "t_order");
        assertTableSegment(tableSegmentIterator.next(), 143, 154, "t_order_item");
    }
    
    private void assertTableSegment(final SimpleTableSegment actual, final int expectedStartIndex, final int expectedStopIndex, final String expectedTableName) {
        assertThat(actual.getStartIndex(), is(expectedStartIndex));
        assertThat(actual.getStopIndex(), is(expectedStopIndex));
        Optional<String> actualTableName = Optional.ofNullable(actual.getTableName()).map(TableNameSegment::getIdentifier).map(IdentifierValue::getValue);
        assertTrue(actualTableName.isPresent());
        assertThat(actualTableName.get(), is(expectedTableName));
    }
}
