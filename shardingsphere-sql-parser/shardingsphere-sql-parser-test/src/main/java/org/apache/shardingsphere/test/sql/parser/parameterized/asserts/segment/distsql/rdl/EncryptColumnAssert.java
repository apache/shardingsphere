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

package org.apache.shardingsphere.test.sql.parser.parameterized.asserts.segment.distsql.rdl;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.encrypt.distsql.parser.statement.segment.EncryptColumnSegment;
import org.apache.shardingsphere.test.sql.parser.parameterized.asserts.SQLCaseAssertContext;
import org.apache.shardingsphere.test.sql.parser.parameterized.asserts.segment.distsql.AlgorithmAssert;
import org.apache.shardingsphere.test.sql.parser.parameterized.jaxb.cases.domain.segment.impl.distsql.rdl.ExpectedEncryptColumn;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

/**
 * Encrypt column assert.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class EncryptColumnAssert {

    /**
     * Assert encrypt rule statement is correct with expected parser result.
     *
     * @param assertContext assert context
     * @param actual        actual encrypt column
     * @param expected      expected encrypt column test case
     */
    public static void assertIs(final SQLCaseAssertContext assertContext, final EncryptColumnSegment actual, final ExpectedEncryptColumn expected) {
        if (null != expected) {
            assertNotNull(assertContext.getText("Actual should exist."), actual);
            assertThat(assertContext.getText(String.format("`%s`'s assertion error", actual.getClass().getSimpleName())), actual.getName(), is(expected.getName()));
            assertThat(assertContext.getText(String.format("`%s`'s assertion error", actual.getClass().getSimpleName())), actual.getPlainColumn(), is(expected.getPlainColumn()));
            assertThat(assertContext.getText(String.format("`%s`'s assertion error", actual.getClass().getSimpleName())), actual.getCipherColumn(), is(expected.getCipherColumn()));
            AlgorithmAssert.assertIs(assertContext, actual.getEncryptor(), expected.getEncryptor());
        } else {
            assertNull(assertContext.getText("Actual should not exist."), actual);
        }
    }
}
