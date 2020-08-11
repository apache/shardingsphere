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

package org.apache.shardingsphere.infra.rewrite.sql.impl;

import org.apache.shardingsphere.infra.rewrite.context.SQLRewriteContext;
import org.apache.shardingsphere.infra.rewrite.sql.fixture.RouteUnitAwareSQLTokenFixture;
import org.apache.shardingsphere.infra.rewrite.sql.fixture.SQLTokenFixture;
import org.apache.shardingsphere.infra.route.context.RouteMapper;
import org.apache.shardingsphere.infra.route.context.RouteUnit;
import org.junit.Test;

import java.util.Collections;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class RouteSQLBuilderTest {
    
    @Test
    public void assertToSQLWithNormalSQLToken() {
        SQLRewriteContext context = mock(SQLRewriteContext.class);
        when(context.getSql()).thenReturn("SELECT * FROM tbl WHERE id=?");
        when(context.getSqlTokens()).thenReturn(Collections.singletonList(new SQLTokenFixture(14, 16)));
        assertThat(new RouteSQLBuilder(context, createRouteUnit()).toSQL(), is("SELECT * FROM XXX WHERE id=?"));
    }
    
    @Test
    public void assertToSQLWithRouteUnitAwaredSQLToken() {
        SQLRewriteContext context = mock(SQLRewriteContext.class);
        when(context.getSql()).thenReturn("SELECT * FROM tbl WHERE id=?");
        when(context.getSqlTokens()).thenReturn(Collections.singletonList(new RouteUnitAwareSQLTokenFixture(14, 16)));
        assertThat(new RouteSQLBuilder(context, createRouteUnit()).toSQL(), is("SELECT * FROM tbl_0 WHERE id=?"));
    }
    
    private RouteUnit createRouteUnit() {
        return new RouteUnit(mock(RouteMapper.class), Collections.singletonList(new RouteMapper("tbl", "tbl_0")));
    }
}
