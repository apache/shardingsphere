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

package org.apache.shardingsphere.test.sql.parser.parameterized.asserts.segment.distsql;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.distsql.parser.segment.DataSourceSegment;
import org.apache.shardingsphere.test.sql.parser.parameterized.asserts.SQLCaseAssertContext;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.segment.impl.distsql.ExpectedDataSource;
import org.hamcrest.CoreMatchers;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

/**
 * Data source assert.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DataSourceAssert {
    
    /**
     * Assert RQL statement is correct with expected parser result.
     *
     * @param assertContext assert context
     * @param actual actual RQL statement
     * @param expected expected RQL statement test case
     */
    public static void assertIs(final SQLCaseAssertContext assertContext, final DataSourceSegment actual, final ExpectedDataSource expected) {
        if (null == expected) {
            assertNull(assertContext.getText("Actual dataSource should not exist."), actual);
        } else {
            assertNotNull(assertContext.getText("Actual dataSource should exist."), actual);
            assertThat(assertContext.getText(String.format("`%s`'s datasource segment assertion error: ",
                    actual.getClass().getSimpleName())), actual.getName(), CoreMatchers.is(expected.getName()));
            assertThat(assertContext.getText(String.format("`%s`'s datasource segment assertion error: ",
                    actual.getClass().getSimpleName())), actual.getUrl(), CoreMatchers.is(expected.getUrl()));
            assertThat(assertContext.getText(String.format("`%s`'s datasource segment assertion error: ",
                    actual.getClass().getSimpleName())), actual.getHostName(), CoreMatchers.is(expected.getHostName()));
            assertThat(assertContext.getText(String.format("`%s`'s datasource segment assertion error: ",
                    actual.getClass().getSimpleName())), actual.getPort(), CoreMatchers.is(expected.getPort()));
            assertThat(assertContext.getText(String.format("`%s`'s datasource segment assertion error: ",
                    actual.getClass().getSimpleName())), actual.getDb(), CoreMatchers.is(expected.getDb()));
            assertThat(assertContext.getText(String.format("`%s`'s datasource segment assertion error: ",
                    actual.getClass().getSimpleName())), actual.getUser(), CoreMatchers.is(expected.getUser()));
            assertThat(assertContext.getText(String.format("`%s`'s datasource segment assertion error: ",
                    actual.getClass().getSimpleName())), actual.getPassword(), CoreMatchers.is(expected.getPassword()));
        }
    }
}
