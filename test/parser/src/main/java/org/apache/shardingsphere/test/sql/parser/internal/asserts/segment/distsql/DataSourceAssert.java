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

package org.apache.shardingsphere.test.sql.parser.internal.asserts.segment.distsql;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.distsql.parser.segment.DataSourceSegment;
import org.apache.shardingsphere.distsql.parser.segment.HostnameAndPortBasedDataSourceSegment;
import org.apache.shardingsphere.distsql.parser.segment.URLBasedDataSourceSegment;
import org.apache.shardingsphere.test.sql.parser.internal.asserts.SQLCaseAssertContext;
import org.apache.shardingsphere.test.sql.parser.internal.jaxb.cases.domain.segment.impl.distsql.ExpectedDataSource;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Data source assert.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DataSourceAssert {
    
    /**
     * Assert data source is correct with expected parser result.
     *
     * @param assertContext assert context
     * @param actual actual data source
     * @param expected expected data source test case
     */
    public static void assertIs(final SQLCaseAssertContext assertContext, final DataSourceSegment actual, final ExpectedDataSource expected) {
        if (null == expected) {
            assertNull(assertContext.getText("Actual dataSource should not exist."), actual);
        } else {
            assertNotNull(assertContext.getText("Actual dataSource should exist."), actual);
            assertThat(assertContext.getText(String.format("`%s`'s datasource segment assertion error: ", actual.getClass().getSimpleName())), actual.getName(), is(expected.getName()));
            assertThat(assertContext.getText(String.format("`%s`'s datasource segment assertion error: ", actual.getClass().getSimpleName())), actual.getUser(), is(expected.getUser()));
            assertThat(assertContext.getText(String.format("`%s`'s datasource segment assertion error: ", actual.getClass().getSimpleName())), actual.getPassword(), is(expected.getPassword()));
            PropertiesAssert.assertIs(assertContext, actual.getProps(), expected.getProps());
            if (actual instanceof URLBasedDataSourceSegment) {
                assertThat(assertContext.getText(String.format("`%s`'s datasource segment assertion error: ",
                        actual.getClass().getSimpleName())), ((URLBasedDataSourceSegment) actual).getUrl(), is(expected.getUrl()));
            }
            if (actual instanceof HostnameAndPortBasedDataSourceSegment) {
                HostnameAndPortBasedDataSourceSegment actualSegment = (HostnameAndPortBasedDataSourceSegment) actual;
                assertThat(assertContext.getText(String.format("`%s`'s datasource segment assertion error: ",
                        actual.getClass().getSimpleName())), actualSegment.getHostname(), is(expected.getHostname()));
                assertThat(assertContext.getText(String.format("`%s`'s datasource segment assertion error: ", actual.getClass().getSimpleName())), actualSegment.getPort(), is(expected.getPort()));
                assertThat(assertContext.getText(String.format("`%s`'s datasource segment assertion error: ", actual.getClass().getSimpleName())), actualSegment.getDatabase(), is(expected.getDb()));
            }
        }
    }
}
