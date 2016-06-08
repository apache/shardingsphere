/*
 * Copyright 1999-2015 dangdang.com.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package com.dangdang.ddframe.rdb.sharding.merger.resultset.memory.row;

import com.dangdang.ddframe.rdb.sharding.merger.fixture.MergerTestUtil;
import com.dangdang.ddframe.rdb.sharding.merger.fixture.TestResultSetRow;
import org.junit.Before;
import org.junit.Test;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public final class ResultSetRowTest {
    
    private ResultSetRow resultSetRow;
    
    @Before
    public void setUp() throws SQLException {
        resultSetRow = new AbstractResultSetRow(MergerTestUtil.mockResult(Arrays.asList("col_1", "col_2"),
                Collections.<ResultSetRow>singletonList(new TestResultSetRow("1", 2)))) { };
    }
    
    @Test
    public void assertGetCell() {
        assertThat(resultSetRow.getCell(1), is((Object) "1"));
        assertThat(resultSetRow.getCell(2), is((Object) 2));
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void assertGetCellWhenOutOfRange() {
        resultSetRow.getCell(10);
    }
    
    @Test
    public void assertSetCell() {
        resultSetRow.setCell(1, "new");
        assertThat(resultSetRow.getCell(1), is((Object) "new"));
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void assertSetCellWhenOutOfRange() {
        resultSetRow.setCell(0, "new");
    }
    
    @Test
    public void assertInRange() {
        assertTrue(resultSetRow.inRange(1));
    }
    
    @Test
    public void assertOutOfRangeWhenLessThanOne() {
        assertFalse(resultSetRow.inRange(0));
        assertFalse(resultSetRow.inRange(-1));
    }
    
    @Test
    public void assertOutOfRangeWhenGreatThanMaxSize() {
        assertFalse(resultSetRow.inRange(3));
    }
}
