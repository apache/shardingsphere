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

package org.apache.shardingsphere.infra.rewrite.sql.token.pojo.generic;

import org.apache.shardingsphere.infra.route.context.RouteMapper;
import org.apache.shardingsphere.infra.route.context.RouteUnit;
import org.junit.Test;

import java.util.Collections;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public final class SubstitutableColumnsTokenTest {

    @Test
    public void assertToString() {
        SubstitutableColumnsToken token = new SubstitutableColumnsToken(0, 1, new SubstitutableColumn("", "", "id", null, Optional.empty()));
        assertThat(token.toString(), is("id"));
    }

    @Test
    public void assertToStringWithRouteUnit() {
        RouteUnit routeUnit = new RouteUnit(new RouteMapper("logic_db", "logic_db"), Collections.singletonList(new RouteMapper("t_user", "t_user_0")));
        SubstitutableColumnsToken token = new SubstitutableColumnsToken(0, 1, new SubstitutableColumn("t_user", "a", "id", null, Optional.empty()));
        assertThat(token.toString(routeUnit), is("a.id"));
        SubstitutableColumnsToken token2 = new SubstitutableColumnsToken(0, 1, new SubstitutableColumn("t_user", "t_user", "id", null, Optional.empty()));
        assertThat(token2.toString(routeUnit), is("t_user_0.id"));
    }
}
