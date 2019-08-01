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

package org.apache.shardingsphere.core.parse.sql.statement.generic;

import com.google.common.base.Optional;
import org.apache.shardingsphere.core.parse.sql.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.core.parse.sql.segment.generic.TableSegment;
import org.apache.shardingsphere.core.parse.sql.statement.SQLStatement;
import org.apache.shardingsphere.core.parse.sql.statement.dml.SelectStatement;
import org.junit.Test;

import java.util.Iterator;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public final class AbstractSQLStatementTest {
    
    @Test
    public void assertFindSQLSegment() {
        SQLStatement sqlStatement = createSQLStatement();
        Optional<TableSegment> actual = sqlStatement.findSQLSegment(TableSegment.class);
        assertTrue(actual.isPresent());
        assertThat(actual.get().getTableName(), is("tbl1"));
    }
    
    @Test
    public void assertNotFindSQLSegment() {
        SQLStatement sqlStatement = createSQLStatement();
        Optional<ColumnSegment> actual = sqlStatement.findSQLSegment(ColumnSegment.class);
        assertFalse(actual.isPresent());
    }
    
    @Test
    public void assertFindSQLSegments() {
        SQLStatement sqlStatement = createSQLStatement();
        Iterator<TableSegment> actual = sqlStatement.findSQLSegments(TableSegment.class).iterator();
        assertThat(actual.next().getTableName(), is("tbl1"));
        assertThat(actual.next().getTableName(), is("tbl2"));
        assertFalse(actual.hasNext());
    }
    
    private SQLStatement createSQLStatement() {
        SQLStatement result = new SelectStatement();
        result.getAllSQLSegments().add(new TableSegment(0, 0, "tbl1"));
        result.getAllSQLSegments().add(new TableSegment(0, 0, "tbl2"));
        return result;
    }
}
