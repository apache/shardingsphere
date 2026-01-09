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

package org.apache.shardingsphere.infra.rewrite.sql;

import org.apache.shardingsphere.infra.rewrite.context.SQLRewriteContext;
import org.apache.shardingsphere.infra.rewrite.sql.fixture.SQLTokenFixture;
import org.apache.shardingsphere.infra.route.context.RouteMapper;
import org.apache.shardingsphere.infra.route.context.RouteUnit;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SQLBuilderEngineTest {
    
    @Test
    void assertCreateSQLBuilderEngineWithDefaultConstructor() {
        SQLBuilderEngine actual = new SQLBuilderEngine("SELECT * FROM tbl WHERE id=?", Collections.emptyList());
        assertThat(actual.buildSQL(), is("SELECT * FROM tbl WHERE id=?"));
    }
    
    @Test
    void assertCreateSQLBuilderEngineWithDefaultConstructorAndTokens() {
        SQLBuilderEngine actual = new SQLBuilderEngine("SELECT * FROM tbl WHERE id=?", Collections.singletonList(new SQLTokenFixture(14, 16)));
        assertThat(actual.buildSQL(), is("SELECT * FROM XXX WHERE id=?"));
    }
    
    @Test
    void assertCreateSQLBuilderEngineWithRouteUnitConstructor() {
        RouteUnit routeUnit = new RouteUnit(mock(RouteMapper.class), Collections.singletonList(new RouteMapper("tbl", "tbl_0")));
        SQLRewriteContext sqlRewriteContext = mock(SQLRewriteContext.class);
        when(sqlRewriteContext.getSql()).thenReturn("SELECT * FROM tbl WHERE id=?");
        when(sqlRewriteContext.getSqlTokens()).thenReturn(Collections.emptyList());
        SQLBuilderEngine actual = new SQLBuilderEngine(sqlRewriteContext, routeUnit);
        assertThat(actual.buildSQL(), is("SELECT * FROM tbl WHERE id=?"));
    }
    
    @Test
    void assertCreateSQLBuilderEngineWithRouteUnitConstructorAndTokens() {
        RouteUnit routeUnit = new RouteUnit(mock(RouteMapper.class), Collections.singletonList(new RouteMapper("tbl", "tbl_0")));
        SQLRewriteContext sqlRewriteContext = mock(SQLRewriteContext.class);
        when(sqlRewriteContext.getSql()).thenReturn("SELECT * FROM tbl WHERE id=?");
        when(sqlRewriteContext.getSqlTokens()).thenReturn(Collections.singletonList(new SQLTokenFixture(14, 16)));
        SQLBuilderEngine actual = new SQLBuilderEngine(sqlRewriteContext, routeUnit);
        assertThat(actual.buildSQL(), is("SELECT * FROM XXX WHERE id=?"));
    }
    
    @Test
    void assertBuildSQLWithEmptyTokens() {
        SQLBuilderEngine sqlBuilderEngine = new SQLBuilderEngine("INSERT INTO tbl (col1, col2) VALUES (?, ?)", Collections.emptyList());
        assertThat(sqlBuilderEngine.buildSQL(), is("INSERT INTO tbl (col1, col2) VALUES (?, ?)"));
    }
    
    @Test
    void assertBuildSQLWithMultipleTokens() {
        SQLBuilderEngine sqlBuilderEngine = new SQLBuilderEngine("SELECT * FROM tbl WHERE col1=? AND col2=?",
                java.util.Arrays.asList(new SQLTokenFixture(14, 17), new SQLTokenFixture(28, 31)));
        assertThat(sqlBuilderEngine.buildSQL(), is("SELECT * FROM XXXWHERE col1XXXND col2=?"));
    }
    
    @Test
    void assertBuildSQLWithComplexSQL() {
        String complexSQL = "SELECT t1.id, t2.name FROM table1 t1 JOIN table2 t2 ON t1.id = t2.id WHERE t1.status = ? ORDER BY t2.name DESC LIMIT 10";
        SQLBuilderEngine sqlBuilderEngine = new SQLBuilderEngine(complexSQL, Collections.singletonList(new SQLTokenFixture(14, 19)));
        assertThat(sqlBuilderEngine.buildSQL(), is("SELECT t1.id, XXXe FROM table1 t1 JOIN table2 t2 ON t1.id = t2.id WHERE t1.status = ? ORDER BY t2.name DESC LIMIT 10"));
    }
    
    @Test
    void assertBuildSQLWithEmptySQLString() {
        SQLBuilderEngine sqlBuilderEngine = new SQLBuilderEngine("", Collections.emptyList());
        assertThat(sqlBuilderEngine.buildSQL(), is(""));
    }
}
