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

package org.apache.shardingsphere.test.sql.parser.internal.asserts.segment.charset;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.sql.parser.sql.common.segment.ddl.charset.CharsetNameSegment;
import org.apache.shardingsphere.test.sql.parser.internal.asserts.SQLCaseAssertContext;
import org.apache.shardingsphere.test.sql.parser.internal.asserts.segment.SQLSegmentAssert;
import org.apache.shardingsphere.test.sql.parser.internal.jaxb.cases.domain.segment.impl.charset.ExpectedCharsetName;

import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertTrue;

/**
 * Charset assert.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class CharsetAssert {
    
    /**
     * Assert actual charset name segment is correct with expected charset name.
     *
     * @param assertContext assert context
     * @param actual actual charset name segment
     * @param expected expected charset name
     */
    public static void assertIs(final SQLCaseAssertContext assertContext, final CharsetNameSegment actual, final ExpectedCharsetName expected) {
        if (null == expected) {
            assertFalse(assertContext.getText("Actual charset name should not exist."), Optional.ofNullable(actual).isPresent());
        } else {
            assertTrue(assertContext.getText("Actual charset name should exist."), Optional.ofNullable(actual).isPresent());
            assertThat(assertContext.getText("charset name assertion error. "), actual.getName(), is(expected.getName()));
            SQLSegmentAssert.assertIs(assertContext, actual, expected);
        }
    }
}
