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

package org.apache.shardingsphere.core.route.router.sharding.condition.generator.impl;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.expr.complex.CommonExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.predicate.value.PredicateInRightValue;
import org.apache.shardingsphere.core.route.router.sharding.condition.Column;
import org.apache.shardingsphere.core.strategy.route.value.ListRouteValue;
import org.apache.shardingsphere.core.strategy.route.value.RouteValue;
import org.junit.Test;

import java.util.Collection;
import java.util.Date;
import java.util.LinkedList;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public final class ConditionValueInOperatorGeneratorTest {

    private ConditionValueInOperatorGenerator generator = new ConditionValueInOperatorGenerator();

    private Column column = new Column("shardsphere", "apache");

    @Test
    public void assertNowExpression() {
        Collection<ExpressionSegment> segmentCollection = Lists.newArrayList();
        segmentCollection.add(new CommonExpressionSegment(0, 0, "now()"));
        PredicateInRightValue inRightValue = new PredicateInRightValue(segmentCollection);
        Optional<RouteValue> optional = generator.generate(inRightValue, column, new LinkedList<>());
        assertTrue(optional.isPresent());
        assertTrue(optional.get() instanceof ListRouteValue);
        ListRouteValue listRouteValue = (ListRouteValue) optional.get();
        assertFalse(listRouteValue.getValues().isEmpty());
        assertTrue(listRouteValue.getValues().iterator().next() instanceof Date);
    }
}
