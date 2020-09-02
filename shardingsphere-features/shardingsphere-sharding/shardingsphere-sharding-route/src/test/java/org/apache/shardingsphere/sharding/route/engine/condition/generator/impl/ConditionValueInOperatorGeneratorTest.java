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

package org.apache.shardingsphere.sharding.route.engine.condition.generator.impl;

import org.apache.shardingsphere.sharding.strategy.value.ListRouteValue;
import org.apache.shardingsphere.sharding.strategy.value.RouteValue;
import org.apache.shardingsphere.sharding.route.engine.condition.Column;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.complex.CommonExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.predicate.value.PredicateInRightValue;
import org.junit.Test;

import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public final class ConditionValueInOperatorGeneratorTest {
    
    private final ConditionValueInOperatorGenerator generator = new ConditionValueInOperatorGenerator();
    
    private final Column column = new Column("id", "tbl");
    
    @Test
    public void assertNowExpression() {
        PredicateInRightValue inRightValue = new PredicateInRightValue(0, 1, Collections.singletonList(new CommonExpressionSegment(0, 0, "now()")));
        Optional<RouteValue> routeValue = generator.generate(inRightValue, column, new LinkedList<>());
        assertTrue(routeValue.isPresent());
        assertThat(((ListRouteValue) routeValue.get()).getValues().iterator().next(), instanceOf(Date.class));
    }
}
