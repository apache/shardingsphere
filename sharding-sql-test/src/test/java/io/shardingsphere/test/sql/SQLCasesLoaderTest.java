/*
 * Copyright 2016-2018 shardingsphere.io.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingsphere.test.sql;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public final class SQLCasesLoaderTest {
    
    @Test
    public void assertGetSupportedSQLForLiteralWithoutParameter() {
        assertThat(SQLCasesLoader.getInstance().getSupportedSQL("select_constant_without_table", SQLCaseType.Literal, Collections.emptyList()), is("SELECT 1 as a"));
    }
    
    @Test
    public void assertGetSupportedSQLForLiteralWithParameters() {
        assertThat(SQLCasesLoader.getInstance().getSupportedSQL("select_with_alias", SQLCaseType.Literal, Arrays.asList(10, 1000)), 
                is("SELECT t_order.* FROM t_order t_order WHERE user_id = 10 AND order_id = 1000"));
    }
    
    @Test
    public void assertGetSupportedSQLForPlaceholder() {
        assertThat(SQLCasesLoader.getInstance().getSupportedSQL("select_with_alias", SQLCaseType.Placeholder, Arrays.asList(10, 1000)),
                is("SELECT t_order.* FROM t_order t_order WHERE user_id = ? AND order_id = ?"));
    }
    
    @Test(expected = IllegalStateException.class)
    public void assertGetSupportedSQLWithoutSQLCaseId() {
        SQLCasesLoader.getInstance().getSupportedSQL("no_sql_case_id", SQLCaseType.Literal, Collections.emptyList());
    }
    
    @Test
    public void assertGetUnsupportedSQLForLiteral() {
        assertThat(SQLCasesLoader.getInstance().getUnsupportedSQL("assertSelectIntoSQL", SQLCaseType.Literal, Collections.emptyList()), is("SELECT * INTO t_order_new FROM t_order"));
    }
    
    @Test(expected = IllegalStateException.class)
    public void assertGetUnsupportedSQLWithoutSQLCaseId() {
        SQLCasesLoader.getInstance().getUnsupportedSQL("no_sql_case_id", SQLCaseType.Literal, Collections.emptyList());
    }
    
    @Test
    public void assertGetSupportedSQLTestParameters() {
        Collection<Object[]> actual = SQLCasesLoader.getInstance().getSupportedSQLTestParameters(Collections.singletonList(DatabaseTypeEnum.H2), DatabaseTypeEnum.class);
        assertFalse(actual.isEmpty());
        Object[] actualRow = actual.iterator().next();
        assertThat(actualRow.length, is(3));
        assertThat(actualRow[0], instanceOf(String.class));
        assertThat(actualRow[1], instanceOf(DatabaseTypeEnum.class));
        assertThat(actualRow[2], instanceOf(SQLCaseType.class));
    }
    
    @Test
    public void assertGetUnsupportedSQLTestParameters() {
        Collection<Object[]> actual = SQLCasesLoader.getInstance().getUnsupportedSQLTestParameters(Collections.singletonList(DatabaseTypeEnum.H2), DatabaseTypeEnum.class);
        assertFalse(actual.isEmpty());
        Object[] actualRow = actual.iterator().next();
        assertThat(actualRow.length, is(3));
        assertThat(actualRow[0], instanceOf(String.class));
        assertThat(actualRow[1], instanceOf(DatabaseTypeEnum.class));
        assertThat(actualRow[2], instanceOf(SQLCaseType.class));
    }
    
    @Test
    public void assertCountAllSupportedSQLCases() {
        assertTrue(SQLCasesLoader.getInstance().countAllSupportedSQLCases() > 1);
    }
}
