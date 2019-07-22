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

package org.apache.shardingsphere.core.parse.integrate.asserts.table;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.core.parse.integrate.asserts.SQLStatementAssertMessage;
import org.apache.shardingsphere.core.parse.integrate.jaxb.table.ExpectedTable;
import org.apache.shardingsphere.core.parse.sql.segment.generic.TableSegment;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
        Collection<TableSegment> mergedActual = mergeTableSegments(actual);
        Map<String, ExpectedTable> expectedMap = getExpectedMap(expected);
        assertThat(assertMessage.getFullAssertMessage("Tables size assertion error: "), mergedActual.size(), is(expectedMap.size()));
        for (TableSegment each : mergedActual) {
            assertThat(assertMessage.getFullAssertMessage("Table name assertion error: "), each.getTableName(), is(expectedMap.get(each.getTableName()).getName()));
            assertThat(assertMessage.getFullAssertMessage("Table alias assertion error: "), each.getAlias().orNull(), is(expectedMap.get(each.getTableName()).getAlias()));
        }
    }
    
    // TODO:yanan remove this method and make sure the table number of xml is correct
    private Collection<TableSegment> mergeTableSegments(final Collection<TableSegment> actual) {
        Collection<TableSegment> result = new LinkedList<>();
        Set<String> tableNames = new HashSet<>(actual.size(), 1);
        for (TableSegment each : actual) {
            if (tableNames.add(each.getTableName())) {
                result.add(each);
            }
        }
        return result;
    }

    // TODO:yanan remove this method and make sure the seq of xml is correct
    private Map<String, ExpectedTable> getExpectedMap(final List<ExpectedTable> expected) {
        Map<String, ExpectedTable> result = new HashMap<>(expected.size(), 1);
        for (ExpectedTable each : expected) {
            result.put(each.getName(), each);
        }
        return result;
    }
}
