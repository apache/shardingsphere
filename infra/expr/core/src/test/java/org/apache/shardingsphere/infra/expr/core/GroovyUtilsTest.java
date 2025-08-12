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

package org.apache.shardingsphere.infra.expr.core;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

class GroovyUtilsTest {
    
    @Test
    void assertSplit() {
        assertThat(GroovyUtils.split(""), is(Collections.emptyList()));
        assertThat(GroovyUtils.split(" t_order_0, t_order_1 "), is(Arrays.asList("t_order_0", "t_order_1")));
        assertThat(GroovyUtils.split("t_order_${null}"), is(Collections.singletonList("t_order_${null}")));
        assertThat(GroovyUtils.split("t_order_${'xx'}"), is(Collections.singletonList("t_order_${'xx'}")));
        assertThat(GroovyUtils.split("t_order_${[0, 1, 2]},t_order_item_${[0, 2]}"), is(Arrays.asList("t_order_${[0, 1, 2]}", "t_order_item_${[0, 2]}")));
        assertThat(GroovyUtils.split("t_order_${0..2},t_order_item_${0..1}"), is(Arrays.asList("t_order_${0..2}", "t_order_item_${0..1}")));
        assertThat(GroovyUtils.split("t_${[\"new${1+2}\",'old']}_order_${1..2}"), is(Collections.singletonList("t_${[\"new${1+2}\",'old']}_order_${1..2}")));
        assertThat(GroovyUtils.split("t_$->{[\"new$->{1+2}\",'old']}_order_$->{1..2}"), is(Collections.singletonList("t_$->{[\"new$->{1+2}\",'old']}_order_$->{1..2}")));
    }
}
