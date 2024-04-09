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

package org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.distsql.rdl;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.encrypt.distsql.segment.EncryptColumnSegment;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.SQLCaseAssertContext;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.distsql.AlgorithmAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.segment.impl.distsql.rdl.ExpectedEncryptColumn;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Encrypt column assert.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class EncryptColumnAssert {
    
    /**
     * Assert encrypt rule statement is correct with expected parser result.
     *
     * @param assertContext assert context
     * @param actual actual encrypt column
     * @param expected expected encrypt column test case
     */
    public static void assertIs(final SQLCaseAssertContext assertContext, final EncryptColumnSegment actual, final ExpectedEncryptColumn expected) {
        if (null == expected) {
            assertNull(actual, assertContext.getText("Actual encrypt column should not exist."));
        } else {
            assertNotNull(actual, assertContext.getText("Actual encrypt column should exist."));
            assertThat(assertContext.getText(String.format("`%s`'s assertion error", actual.getClass().getSimpleName())), actual.getName(), is(expected.getName()));
            assertThat(assertContext.getText(String.format("`%s`'s assertion error", actual.getClass().getSimpleName())),
                    actual.getCipher().getName(), is(expected.getCipher().getName()));
            AlgorithmAssert.assertIs(assertContext, actual.getCipher().getEncryptor(), expected.getCipher().getEncryptor());
            if (null == expected.getAssistedQuery()) {
                assertNull(actual.getAssistedQuery(), assertContext.getText(String.format("`%s`'s assertion error", actual.getClass().getSimpleName())));
            } else {
                assertThat(assertContext.getText(String.format("`%s`'s assertion error", actual.getClass().getSimpleName())),
                        actual.getAssistedQuery().getName(), is(expected.getAssistedQuery().getName()));
                AlgorithmAssert.assertIs(assertContext, actual.getAssistedQuery().getEncryptor(), expected.getAssistedQuery().getEncryptor());
            }
            if (null == expected.getLikeQuery()) {
                assertNull(actual.getLikeQuery(), assertContext.getText(String.format("`%s`'s assertion error", actual.getClass().getSimpleName())));
            } else {
                assertThat(assertContext.getText(String.format("`%s`'s assertion error", actual.getClass().getSimpleName())),
                        actual.getLikeQuery().getName(), is(expected.getLikeQuery().getName()));
                AlgorithmAssert.assertIs(assertContext, actual.getLikeQuery().getEncryptor(), expected.getLikeQuery().getEncryptor());
            }
        }
    }
}
