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

package org.apache.shardingsphere.sql.parser.integrate.asserts.segment.owner;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.sql.parser.integrate.asserts.SQLCaseAssertContext;
import org.apache.shardingsphere.sql.parser.integrate.asserts.segment.SQLSegmentAssert;
import org.apache.shardingsphere.sql.parser.integrate.jaxb.impl.owner.ExpectedSchemaOwner;
import org.apache.shardingsphere.sql.parser.integrate.jaxb.impl.owner.ExpectedTableOwner;
import org.apache.shardingsphere.sql.parser.sql.segment.generic.SchemaSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.generic.TableSegment;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * Owner assert.
 *
 * @author zhangliang
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class OwnerAssert {
    
    /**
     * Assert actual table segment is correct with expected table owner.
     * 
     * @param assertContext assert context
     * @param actual actual table segment
     * @param expected expected table owner
     */
    public static void assertTable(final SQLCaseAssertContext assertContext, final TableSegment actual, final ExpectedTableOwner expected) {
        assertThat(assertContext.getText("Owner name assertion error: "), actual.getTableName(), is(expected.getName()));
        assertThat(assertContext.getText("Owner name start delimiter assertion error: "), actual.getTableQuoteCharacter().getStartDelimiter(), is(expected.getStartDelimiter()));
        assertThat(assertContext.getText("Owner name end delimiter assertion error: "), actual.getTableQuoteCharacter().getEndDelimiter(), is(expected.getEndDelimiter()));
        SQLSegmentAssert.assertIs(assertContext, actual, expected);
    }
    
    /**
     * Assert actual schema segment is correct with expected schema owner.
     *
     * @param assertContext assert context
     * @param actual actual schema segment
     * @param expected expected schema owner
     */
    public static void assertSchema(final SQLCaseAssertContext assertContext, final SchemaSegment actual, final ExpectedSchemaOwner expected) {
        assertThat(assertContext.getText("Owner name assertion error: "), actual.getName(), is(expected.getName()));
        assertThat(assertContext.getText("Owner start delimiter assertion error: "), actual.getQuoteCharacter().getStartDelimiter(), is(expected.getStartDelimiter()));
        assertThat(assertContext.getText("Owner end delimiter assertion error: "), actual.getQuoteCharacter().getEndDelimiter(), is(expected.getEndDelimiter()));
        SQLSegmentAssert.assertIs(assertContext, actual, expected);
    }
}
