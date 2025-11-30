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

import org.apache.shardingsphere.infra.rewrite.sql.fixture.RouteUnitAwareSQLTokenFixture;
import org.apache.shardingsphere.infra.rewrite.sql.fixture.SQLTokenFixture;
import org.apache.shardingsphere.infra.route.context.RouteMapper;
import org.apache.shardingsphere.infra.route.context.RouteUnit;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;

class RouteSQLBuilderTest {
    
    @Test
    void assertToSQLWithNormalSQLToken() {
        assertThat(new RouteSQLBuilder("SELECT * FROM tbl WHERE id=?", Collections.singletonList(new SQLTokenFixture(14, 16)), createRouteUnit()).toSQL(), is("SELECT * FROM XXX WHERE id=?"));
    }
    
    @Test
    void assertToSQLWithDuplicateSQLToken() {
        assertThat(new RouteSQLBuilder("SELECT * FROM tbl WHERE id=?", Arrays.asList(new SQLTokenFixture(14, 16), new SQLTokenFixture(14, 16)), createRouteUnit()).toSQL(),
                is("SELECT * FROM XXX WHERE id=?"));
    }
    
    @Test
    void assertToSQLWithRouteUnitAwareSQLToken() {
        assertThat(new RouteSQLBuilder("SELECT * FROM tbl WHERE id=?", Collections.singletonList(new RouteUnitAwareSQLTokenFixture(14, 16)), createRouteUnit()).toSQL(),
                is("SELECT * FROM tbl_0 WHERE id=?"));
    }
    
    private RouteUnit createRouteUnit() {
        return new RouteUnit(mock(RouteMapper.class), Collections.singletonList(new RouteMapper("tbl", "tbl_0")));
    }
}
