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
import org.apache.shardingsphere.sql.parser.sql.common.segment.ddl.index.IndexSegment;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.SQLCaseAssertContext;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.segment.impl.definition.ExpectedIndexDefinition;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Index definition assert.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class IndexDefinitionAssert {
    
    /**
     * Assert actual index definition segment is correct with expected index definition.
     * 
     * @param assertContext assert context
     * @param actual actual index definition segment
     * @param expected expected index definition
     */
    public static void assertIs(final SQLCaseAssertContext assertContext, final IndexSegment actual, final ExpectedIndexDefinition expected) {
        assertThat(assertContext.getText("Index definition name assertion error: "), actual.getIndexName().getIdentifier().getValue(), is(expected.getIndex().getName()));
        assertThat(assertContext.getText("Index definition start index assertion error: "), actual.getStartIndex(), is(expected.getStartIndex()));
        assertThat(assertContext.getText("Index definition stop index assertion error: "), actual.getStopIndex(), is(expected.getStopIndex()));
    }
}
