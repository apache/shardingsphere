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

package org.apache.shardingsphere.infra.federation.optimizer.planner;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.calcite.adapter.enumerable.EnumerableRules;
import org.apache.calcite.plan.ConventionTraitDef;
import org.apache.calcite.plan.RelOptPlanner;
import org.apache.calcite.plan.RelOptRule;
import org.apache.calcite.plan.hep.HepMatchOrder;
import org.apache.calcite.plan.hep.HepPlanner;
import org.apache.calcite.plan.hep.HepProgramBuilder;
import org.apache.calcite.plan.volcano.VolcanoPlanner;
import org.apache.calcite.rel.RelCollationTraitDef;
import org.apache.calcite.rel.rules.AggregateExpandDistinctAggregatesRule;
import org.apache.calcite.rel.rules.CoreRules;
import org.apache.calcite.rel.rules.ProjectRemoveRule;
import org.apache.shardingsphere.infra.federation.optimizer.metadata.translatable.TranslatableFilterRule;
import org.apache.shardingsphere.infra.federation.optimizer.metadata.translatable.TranslatableProjectFilterRule;
import org.apache.shardingsphere.infra.federation.optimizer.metadata.translatable.TranslatableProjectRule;

import java.util.Collection;
import java.util.LinkedList;

/**
 * Query optimize planner factory.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class QueryOptimizePlannerFactory {
    
    private static final int DEFAULT_MATCH_LIMIT = 1024;
    
    /**
     * Create new instance of volcano planner.
     *
     * @return volcano planner instance
     */
    public static RelOptPlanner createVolcanoPlanner() {
        RelOptPlanner result = new VolcanoPlanner();
        setUpRules(result);
        return result;
    }
    
    /**
     * Create new instance of hep planner without calc rules.
     *
     * @return hep planner instance
     */
    public static RelOptPlanner createHepPlannerWithoutCalc() {
        HepProgramBuilder builder = new HepProgramBuilder();
        builder.addGroupBegin().addRuleCollection(getFilterRules()).addGroupEnd().addMatchOrder(HepMatchOrder.BOTTOM_UP);
        builder.addGroupBegin().addRuleCollection(getProjectRules()).addGroupEnd().addMatchOrder(HepMatchOrder.BOTTOM_UP);
        builder.addGroupBegin().addRuleInstance(CoreRules.PROJECT_MERGE).addGroupEnd().addMatchOrder(HepMatchOrder.BOTTOM_UP);
        builder.addMatchLimit(DEFAULT_MATCH_LIMIT);
        return new HepPlanner(builder.build());
    }
    
    /**
     * Create new instance of hep planner with calc rules.
     *
     * @return hep planner instance
     */
    public static RelOptPlanner createHepPlannerWithCalc() {
        HepProgramBuilder builder = new HepProgramBuilder();
        builder.addGroupBegin().addRuleCollection(getCalcRules()).addGroupEnd().addMatchOrder(HepMatchOrder.BOTTOM_UP);
        builder.addMatchLimit(DEFAULT_MATCH_LIMIT);
        return new HepPlanner(builder.build());
    }
    
    /**
     * Create new instance of hep planner.
     *
     * @return hep planner instance
     */
    public static RelOptPlanner createHepPlanner() {
        HepProgramBuilder builder = new HepProgramBuilder();
        builder.addGroupBegin().addRuleCollection(getSubQueryRules()).addGroupEnd().addMatchOrder(HepMatchOrder.DEPTH_FIRST);
        builder.addGroupBegin().addRuleCollection(getProjectRules()).addGroupEnd().addMatchOrder(HepMatchOrder.BOTTOM_UP);
        builder.addGroupBegin().addRuleCollection(getFilterRules()).addGroupEnd().addMatchOrder(HepMatchOrder.BOTTOM_UP);
        builder.addGroupBegin().addRuleCollection(getCalcRules()).addGroupEnd().addMatchOrder(HepMatchOrder.BOTTOM_UP);
        builder.addMatchLimit(DEFAULT_MATCH_LIMIT);
        return new HepPlanner(builder.build());
    }
    
    private static void setUpRules(final RelOptPlanner planner) {
        planner.addRelTraitDef(ConventionTraitDef.INSTANCE);
        planner.addRelTraitDef(RelCollationTraitDef.INSTANCE);
        planner.addRule(EnumerableRules.ENUMERABLE_CALC_RULE);
        planner.addRule(EnumerableRules.ENUMERABLE_SORT_RULE);
        planner.addRule(EnumerableRules.ENUMERABLE_JOIN_RULE);
        planner.addRule(EnumerableRules.ENUMERABLE_TABLE_SCAN_RULE);
        planner.addRule(EnumerableRules.ENUMERABLE_AGGREGATE_RULE);
        planner.addRule(EnumerableRules.ENUMERABLE_FILTER_RULE);
        planner.addRule(EnumerableRules.ENUMERABLE_PROJECT_RULE);
        planner.addRule(EnumerableRules.ENUMERABLE_CORRELATE_RULE);
        planner.addRule(EnumerableRules.ENUMERABLE_UNION_RULE);
        planner.addRule(EnumerableRules.ENUMERABLE_FILTER_TO_CALC_RULE);
        planner.addRule(EnumerableRules.ENUMERABLE_PROJECT_TO_CALC_RULE);
    }
    
    private static Collection<RelOptRule> getSubQueryRules() {
        Collection<RelOptRule> result = new LinkedList<>();
        result.add(CoreRules.FILTER_SUB_QUERY_TO_CORRELATE);
        result.add(CoreRules.PROJECT_SUB_QUERY_TO_CORRELATE);
        result.add(CoreRules.JOIN_SUB_QUERY_TO_CORRELATE);
        return result;
    }
    
    private static Collection<RelOptRule> getCalcRules() {
        Collection<RelOptRule> result = new LinkedList<>();
        result.add(AggregateExpandDistinctAggregatesRule.Config.DEFAULT.toRule());
        result.add(CoreRules.PROJECT_TO_CALC);
        result.add(CoreRules.FILTER_TO_CALC);
        result.add(CoreRules.PROJECT_CALC_MERGE);
        result.add(CoreRules.FILTER_CALC_MERGE);
        result.add(EnumerableRules.ENUMERABLE_FILTER_TO_CALC_RULE);
        result.add(EnumerableRules.ENUMERABLE_PROJECT_TO_CALC_RULE);
        result.add(CoreRules.CALC_MERGE);
        return result;
    }
    
    private static Collection<RelOptRule> getProjectRules() {
        Collection<RelOptRule> result = new LinkedList<>();
        result.add(CoreRules.PROJECT_MERGE);
        result.add(CoreRules.PROJECT_CORRELATE_TRANSPOSE);
        result.add(CoreRules.PROJECT_SET_OP_TRANSPOSE);
        result.add(CoreRules.PROJECT_JOIN_TRANSPOSE);
        result.add(CoreRules.PROJECT_REDUCE_EXPRESSIONS);
        result.add(ProjectRemoveRule.Config.DEFAULT.toRule());
        result.add(TranslatableProjectRule.INSTANCE);
        return result;
    }
    
    private static Collection<RelOptRule> getFilterRules() {
        Collection<RelOptRule> result = new LinkedList<>();
        result.add(CoreRules.FILTER_INTO_JOIN);
        result.add(CoreRules.JOIN_CONDITION_PUSH);
        result.add(CoreRules.SORT_JOIN_TRANSPOSE);
        result.add(CoreRules.FILTER_AGGREGATE_TRANSPOSE);
        result.add(CoreRules.FILTER_PROJECT_TRANSPOSE);
        result.add(CoreRules.FILTER_SET_OP_TRANSPOSE);
        result.add(CoreRules.FILTER_REDUCE_EXPRESSIONS);
        result.add(CoreRules.FILTER_MERGE);
        result.add(CoreRules.JOIN_PUSH_EXPRESSIONS);
        result.add(CoreRules.JOIN_PUSH_TRANSITIVE_PREDICATES);
        result.add(TranslatableFilterRule.INSTANCE);
        result.add(TranslatableProjectFilterRule.INSTANCE);
        return result;
    }
}
