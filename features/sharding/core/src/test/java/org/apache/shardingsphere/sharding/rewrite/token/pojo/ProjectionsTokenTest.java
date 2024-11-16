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

package org.apache.shardingsphere.sharding.rewrite.token.pojo;

import org.apache.shardingsphere.infra.route.context.RouteMapper;
import org.apache.shardingsphere.infra.route.context.RouteUnit;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

class ProjectionsTokenTest {
    
    @Test
    void assertGetStopIndex() {
        assertThat(new ProjectionsToken(10, Collections.emptyMap()).getStopIndex(), is(10));
    }
    
    @Test
    void assertToStringWithoutRouteUnit() {
        RouteUnit routeUnit = new RouteUnit(new RouteMapper("foo_ds", "foo_ds_0"), Collections.singleton(new RouteMapper("foo_tbl", "foo_tbl_1")));
        assertThat(new ProjectionsToken(0, Collections.singletonMap(routeUnit, Collections.singleton("foo_col"))).toString(routeUnit), is(", foo_col"));
    }
}
