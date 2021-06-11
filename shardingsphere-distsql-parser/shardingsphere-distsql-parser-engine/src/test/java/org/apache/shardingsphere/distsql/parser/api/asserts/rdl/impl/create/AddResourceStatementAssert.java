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

package org.apache.shardingsphere.distsql.parser.api.asserts.rdl.impl.create;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.distsql.parser.api.asserts.segment.DataSourceAssert;
import org.apache.shardingsphere.distsql.parser.api.sql.jaxb.cases.domain.SQLCaseAssertContext;
import org.apache.shardingsphere.distsql.parser.api.sql.jaxb.cases.domain.statement.rdl.create.AddResourceStatementTestCase;
import org.apache.shardingsphere.distsql.parser.segment.DataSourceSegment;
import org.apache.shardingsphere.distsql.parser.statement.rdl.create.impl.AddResourceStatement;

import static org.junit.Assert.assertEquals;

/**
 * Add Resource statement assert.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class AddResourceStatementAssert {

    /**
     * Assert add resource statement is correct with expected parser result.
     *
     * @param assertContext assert context
     * @param actual        actual add resource statement
     * @param expected      expected add resource statement test case
     */
    public static void assertIs(final SQLCaseAssertContext assertContext, final AddResourceStatement actual, final AddResourceStatementTestCase expected) {
        assertEquals(assertContext.getText("Except statement dateSource size need the same as actual"), expected.getDataSources().size(), actual.getDataSources().size());
        int count = 0;
        for (DataSourceSegment actualDataSource : actual.getDataSources()) {
            DataSourceAssert.assertIs(assertContext, actualDataSource, expected.getDataSources().get(count));
            count++;
        }
    }
}
