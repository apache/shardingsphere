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

package org.apache.shardingsphere.sql.parser.integrate.asserts.table;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.sql.parser.integrate.asserts.SQLStatementAssertMessage;
import org.apache.shardingsphere.sql.parser.integrate.jaxb.table.ExpectedTable;
import org.apache.shardingsphere.sql.parser.sql.segment.generic.TableSegment;

import java.util.Collection;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * Table assert.
 *
 * @author zhangliang
 */
@RequiredArgsConstructor
public final class TableAssert {
    
    private final SQLStatementAssertMessage assertMessage;
    
    /**
     * Assert tables.
     * 
     * @param actual actual tables
     * @param expected expected tables
     */
    public void assertTables(final Collection<TableSegment> actual, final List<ExpectedTable> expected) {
        assertThat(assertMessage.getFullAssertMessage("Tables size assertion error: "), actual.size(), is(expected.size()));
        int count = 0;
        for (TableSegment each : actual) {
            assertThat(assertMessage.getFullAssertMessage("Table name assertion error: "), each.getTableName(), is(expected.get(count).getName()));
            assertThat(assertMessage.getFullAssertMessage("Table alias assertion error: "), each.getAlias().orNull(), is(expected.get(count).getAlias()));
            count++;
        }
    }
}
