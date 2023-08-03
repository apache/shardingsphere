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
import org.apache.shardingsphere.sql.parser.sql.common.segment.ddl.type.TypeDefinitionSegment;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.SQLCaseAssertContext;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.segment.impl.definition.ExpectedTypeDefinition;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Type definition assert.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class TypeDefinitionAssert {
    
    /**
     * Assert whether actual type definition segment is correct with expected type definition.
     *
     * @param assertContext assert context
     * @param actual actual type definition segment
     * @param expected expected type definition
     */
    public static void assertIs(final SQLCaseAssertContext assertContext, final TypeDefinitionSegment actual, final ExpectedTypeDefinition expected) {
        assertThat(assertContext.getText("Type definition attribute name assertion error: "), actual.getAttributeName(), is(expected.getAttributeName()));
        assertThat(assertContext.getText("Type definition data type assertion error: "), actual.getDataType().getDataTypeName(), is(expected.getDataType().getValue()));
    }
}
