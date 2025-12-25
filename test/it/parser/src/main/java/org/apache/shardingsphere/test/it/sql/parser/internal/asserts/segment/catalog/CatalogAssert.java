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

package org.apache.shardingsphere.test.it.sql.parser.internal.asserts.segment.catalog;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.SQLCaseAssertContext;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.segment.impl.catalog.ExpectedCatalogComment;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.segment.impl.catalog.ExpectedCatalogProperties;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.segment.impl.catalog.ExpectedCatalogName;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.segment.impl.catalog.ExpectedCatalogProperty;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.segment.impl.catalog.ExpectedRenameCatalog;

import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Catalog assert.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class CatalogAssert {
    
    /**
     * Assert catalog name is correct with expected catalog name.
     *
     * @param assertContext assert context
     * @param actual actual catalog name
     * @param expected expected catalog name
     */
    public static void assertCatalogName(final SQLCaseAssertContext assertContext, final String actual, final ExpectedCatalogName expected) {
        assertThat(assertContext.getText("Catalog name assertion error: "), actual, is(expected.getName()));
    }
    
    /**
     * Assert rename catalog is correct with expected rename catalog.
     *
     * @param assertContext assert context
     * @param actual actual new catalog name
     * @param expected expected rename catalog
     */
    public static void assertRenameCatalog(final SQLCaseAssertContext assertContext, final String actual, final ExpectedRenameCatalog expected) {
        assertThat(assertContext.getText("Rename catalog assertion error: "), actual, is(expected.getValue()));
    }
    
    /**
     * Assert properties are correct with expected properties.
     *
     * @param assertContext assert context
     * @param actual actual properties
     * @param expected expected properties
     */
    public static void assertProperties(final SQLCaseAssertContext assertContext, final Map<String, String> actual, final ExpectedCatalogProperties expected) {
        assertThat(assertContext.getText("Properties size assertion error: "), actual.size(), is(expected.getProperties().size()));
        for (ExpectedCatalogProperty each : expected.getProperties()) {
            assertProperty(assertContext, actual, each);
        }
    }
    
    private static void assertProperty(final SQLCaseAssertContext assertContext, final Map<String, String> actual, final ExpectedCatalogProperty expected) {
        assertThat(assertContext.getText(String.format("Property key '%s' assertion error: ", expected.getKey())), actual.containsKey(expected.getKey()), is(true));
        assertThat(assertContext.getText(String.format("Property value '%s' assertion error: ", expected.getKey())), actual.get(expected.getKey()), is(expected.getValue()));
    }
    
    /**
     * Assert catalog comment is correct with expected catalog comment.
     *
     * @param assertContext assert context
     * @param actual actual comment
     * @param expected expected catalog comment
     */
    public static void assertCatalogComment(final SQLCaseAssertContext assertContext, final String actual, final ExpectedCatalogComment expected) {
        assertThat(assertContext.getText("Catalog comment assertion error: "), actual, is(expected.getValue()));
    }
}
