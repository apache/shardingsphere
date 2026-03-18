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

package org.apache.shardingsphere.sqlfederation.compiler.planner.builder;

import lombok.SneakyThrows;
import org.apache.calcite.adapter.enumerable.EnumerableConvention;
import org.apache.calcite.adapter.enumerable.EnumerableRules;
import org.apache.calcite.plan.Convention;
import org.apache.calcite.plan.ConventionTraitDef;
import org.apache.calcite.plan.RelOptPlanner;
import org.apache.calcite.plan.hep.HepPlanner;
import org.apache.calcite.plan.hep.HepProgram;
import org.apache.calcite.rel.RelCollationTraitDef;
import org.apache.calcite.rel.RelNode;
import org.apache.calcite.rel.convert.ConverterRule;
import org.junit.jupiter.api.Test;
import org.mockito.internal.configuration.plugins.Plugins;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isA;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SQLFederationPlannerBuilderTest {
    
    @Test
    void assertBuildVolcanoPlannerWithEnumerableConventionAddsRules() {
        RelOptPlanner actual = SQLFederationPlannerBuilder.buildVolcanoPlanner(EnumerableConvention.INSTANCE);
        assertThat(actual.getRelTraitDefs(), hasItem(ConventionTraitDef.INSTANCE));
        assertThat(actual.getRelTraitDefs(), hasItem(RelCollationTraitDef.INSTANCE));
        assertTrue(actual.getRules().contains(EnumerableRules.ENUMERABLE_JOIN_RULE));
        assertTrue(actual.getRules().stream().anyMatch(each -> each instanceof ConverterRule));
    }
    
    @Test
    void assertBuildVolcanoPlannerWithCustomConventionSkipsEnumerableRules() {
        Convention customConvention = new Convention.Impl("CUSTOM", RelNode.class);
        RelOptPlanner actual = SQLFederationPlannerBuilder.buildVolcanoPlanner(customConvention);
        assertThat(actual.getRelTraitDefs(), hasItem(ConventionTraitDef.INSTANCE));
        assertThat(actual.getRelTraitDefs(), hasItem(RelCollationTraitDef.INSTANCE));
        assertFalse(actual.getRules().contains(EnumerableRules.ENUMERABLE_JOIN_RULE));
    }
    
    @Test
    void assertBuildHepPlannerAddsGroupAndGlobalMatchLimits() throws ReflectiveOperationException {
        HepPlanner planner = (HepPlanner) SQLFederationPlannerBuilder.buildHepPlanner();
        HepProgram program = (HepProgram) Plugins.getMemberAccessor().get(planner.getClass().getDeclaredField("mainProgram"), planner);
        List<?> instructions = new ArrayList<>((List<?>) Plugins.getMemberAccessor().get(program.getClass().getDeclaredField("instructions"), program));
        List<Integer> matchLimits = instructions.stream()
                .filter(each -> "org.apache.calcite.plan.hep.HepInstruction$MatchLimit".equals(each.getClass().getName()))
                .map(this::getLimit)
                .collect(Collectors.toList());
        assertThat(planner, isA(HepPlanner.class));
        assertThat(matchLimits.size(), is(9));
        assertTrue(matchLimits.contains(1024));
        assertThat(matchLimits.stream().filter(limit -> 500 == limit).count(), is(8L));
    }
    
    @SneakyThrows(ReflectiveOperationException.class)
    private int getLimit(final Object matchLimit) {
        return (int) Plugins.getMemberAccessor().get(matchLimit.getClass().getDeclaredField("limit"), matchLimit);
    }
}
