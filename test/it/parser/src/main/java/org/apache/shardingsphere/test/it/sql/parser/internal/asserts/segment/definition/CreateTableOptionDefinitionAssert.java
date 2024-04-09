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

package org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.definition;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.sql.parser.sql.common.segment.ddl.table.CreateTableOptionSegment;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.SQLCaseAssertContext;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.segment.impl.definition.ExpectedCreateTableOptionDefinition;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Create table option assert.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class CreateTableOptionDefinitionAssert {
    
    /**
     * Assert actual create table option segment is correct with expected create table option.
     *
     * @param assertContext assert context
     * @param actual actual create table option segment
     * @param expected expected create table option
     */
    public static void assertIs(final SQLCaseAssertContext assertContext, final CreateTableOptionSegment actual, final ExpectedCreateTableOptionDefinition expected) {
        if (null == expected.getEngine()) {
            assertFalse(actual.getEngine().isPresent(), assertContext.getText("Actual engine should not exist."));
        } else {
            assertTrue(actual.getEngine().isPresent(), assertContext.getText("Actual engine should exist."));
            assertThat(assertContext.getText(String.format("`%s`'s engine assertion error: ", actual.getClass().getSimpleName())), actual.getEngine().get().getEngine(),
                    is(expected.getEngine().getName()));
        }
        if (null == expected.getComment()) {
            assertFalse(actual.getComment().isPresent(), assertContext.getText("Actual comment should not exist."));
        } else {
            assertTrue(actual.getComment().isPresent(), assertContext.getText("Actual comment should exist."));
            assertThat(assertContext.getText(String.format("`%s`'s comment assertion error: ", actual.getClass().getSimpleName())), actual.getComment().get().getText(),
                    is(expected.getComment().getText()));
        }
    }
}
