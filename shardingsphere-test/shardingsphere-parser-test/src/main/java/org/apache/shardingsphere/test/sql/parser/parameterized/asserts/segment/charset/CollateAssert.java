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

package org.apache.shardingsphere.test.sql.parser.parameterized.asserts.segment.charset;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.sql.parser.sql.common.segment.ddl.charset.CollateClauseSegment;
import org.apache.shardingsphere.test.sql.parser.parameterized.asserts.SQLCaseAssertContext;
import org.apache.shardingsphere.test.sql.parser.parameterized.asserts.segment.SQLSegmentAssert;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.segment.impl.charset.ExpectedCollate;

import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

/**
 * Collate assert.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class CollateAssert {
    
    /**
     * Assert actual collate segment is correct with expected column.
     *
     * @param assertContext assert context
     * @param actual actual collate segment
     * @param expected expected collate
     */
    public static void assertIs(final SQLCaseAssertContext assertContext, final CollateClauseSegment actual, final ExpectedCollate expected) {
        if (null != expected) {
            assertTrue(assertContext.getText("Actual collate should exist."), Optional.ofNullable(actual).isPresent());
            assertThat(assertContext.getText("collate assertion error. "), actual.getName(), is(expected.getName()));
            SQLSegmentAssert.assertIs(assertContext, actual, expected);
        } else {
            assertFalse(assertContext.getText("Actual collate should not exist."), Optional.ofNullable(actual).isPresent());
        }
    }
}
