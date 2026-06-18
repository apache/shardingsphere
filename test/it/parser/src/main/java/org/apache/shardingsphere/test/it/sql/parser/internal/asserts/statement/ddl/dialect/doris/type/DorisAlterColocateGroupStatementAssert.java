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

package org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.ddl.dialect.doris.type;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.colocategroup.ColocateGroupSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.property.PropertiesSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.property.PropertySegment;
import org.apache.shardingsphere.sql.parser.statement.doris.ddl.DorisAlterColocateGroupStatement;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.SQLCaseAssertContext;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.SQLSegmentAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.identifier.IdentifierValueAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.owner.OwnerAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.segment.impl.catalog.ExpectedCatalogProperties;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.segment.impl.catalog.ExpectedCatalogProperty;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.segment.impl.colocategroup.ExpectedColocateGroup;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.dialect.doris.DorisAlterColocateGroupStatementTestCase;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Alter colocate group statement assert for Doris.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DorisAlterColocateGroupStatementAssert {
    
    /**
     * Assert alter colocate group statement is correct with expected parser result.
     *
     * @param assertContext assert context
     * @param actual actual alter colocate group statement
     * @param expected expected alter colocate group statement test case
     */
    public static void assertIs(final SQLCaseAssertContext assertContext, final DorisAlterColocateGroupStatement actual, final DorisAlterColocateGroupStatementTestCase expected) {
        assertGroupName(assertContext, actual.getGroupName(), expected.getGroupName());
        if (null != expected.getProperties()) {
            assertProperties(assertContext, actual.getProperties(), expected.getProperties());
        }
    }
    
    private static void assertGroupName(final SQLCaseAssertContext assertContext, final ColocateGroupSegment actual, final ExpectedColocateGroup expected) {
        IdentifierValueAssert.assertIs(assertContext, actual.getIdentifier(), expected, "Colocate group");
        SQLSegmentAssert.assertIs(assertContext, actual, expected);
        if (null == expected.getOwner()) {
            assertFalse(actual.getOwner().isPresent(), assertContext.getText("Actual owner should not exist."));
        } else {
            assertTrue(actual.getOwner().isPresent(), assertContext.getText("Actual owner should exist."));
            OwnerAssert.assertIs(assertContext, actual.getOwner().get(), expected.getOwner());
        }
    }
    
    private static void assertProperties(final SQLCaseAssertContext assertContext, final PropertiesSegment actual, final ExpectedCatalogProperties expected) {
        SQLSegmentAssert.assertIs(assertContext, actual, expected);
        assertThat(assertContext.getText("Properties size assertion error: "), actual.getProperties().size(), is(expected.getProperties().size()));
        for (int i = 0; i < expected.getProperties().size(); i++) {
            assertProperty(assertContext, actual.getProperties().get(i), expected.getProperties().get(i));
        }
    }
    
    private static void assertProperty(final SQLCaseAssertContext assertContext, final PropertySegment actual, final ExpectedCatalogProperty expected) {
        assertThat(assertContext.getText(String.format("Property key '%s' assertion error: ", expected.getKey())), actual.getKey(), is(expected.getKey()));
        assertThat(assertContext.getText(String.format("Property value for key '%s' assertion error: ", expected.getKey())), actual.getValue(), is(expected.getValue()));
        SQLSegmentAssert.assertIs(assertContext, actual, expected);
    }
}
