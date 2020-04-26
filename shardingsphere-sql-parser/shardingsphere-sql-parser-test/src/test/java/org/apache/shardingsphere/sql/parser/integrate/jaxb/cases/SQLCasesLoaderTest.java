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

package org.apache.shardingsphere.sql.parser.integrate.jaxb.cases;

import org.apache.shardingsphere.sql.parser.integrate.jaxb.sql.SQLCaseType;
import org.apache.shardingsphere.sql.parser.integrate.jaxb.sql.loader.SQLCasesRegistry;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;

public final class SQLCasesLoaderTest {
    
    @Test
    public void assertGetSQLForLiteralWithoutParameter() {
        assertThat(SQLCasesRegistry.getInstance().getSqlCasesLoader().getSQL("select_constant_without_table", SQLCaseType.Literal, Collections.emptyList()), is("SELECT 1 as a"));
    }
    
    @Test
    public void assertGetSQLForLiteralWithParameters() {
        assertThat(SQLCasesRegistry.getInstance().getSqlCasesLoader().getSQL("select_with_same_table_name_and_alias", SQLCaseType.Literal, Arrays.asList(10, 1000)), 
                is("SELECT t_order.* FROM t_order t_order WHERE user_id = 10 AND order_id = 1000"));
    }
    
    @Test
    public void assertGetSQLForPlaceholder() {
        assertThat(SQLCasesRegistry.getInstance().getSqlCasesLoader().getSQL("select_with_same_table_name_and_alias", SQLCaseType.Placeholder, Arrays.asList(10, 1000)),
                is("SELECT t_order.* FROM t_order t_order WHERE user_id = ? AND order_id = ?"));
    }
    
    @Test(expected = IllegalStateException.class)
    public void assertGetSQLWithoutSQLCaseId() {
        SQLCasesRegistry.getInstance().getSqlCasesLoader().getSQL("no_sql_case_id", SQLCaseType.Literal, Collections.emptyList());
    }
    
    @Test
    public void assertGetTestParameters() {
        Collection<Object[]> actual = SQLCasesRegistry.getInstance().getSqlCasesLoader().getSQLTestParameters();
        assertFalse(actual.isEmpty());
        Object[] actualRow = actual.iterator().next();
        assertThat(actualRow.length, is(3));
        assertThat(actualRow[0], instanceOf(String.class));
        assertThat(actualRow[1], instanceOf(String.class));
        assertThat(actualRow[2], instanceOf(SQLCaseType.class));
    }
    
    @Test
    public void assertCountAllSQLCases() {
        assertFalse(SQLCasesRegistry.getInstance().getSqlCasesLoader().getAllSQLCaseIDs().isEmpty());
    }
}
