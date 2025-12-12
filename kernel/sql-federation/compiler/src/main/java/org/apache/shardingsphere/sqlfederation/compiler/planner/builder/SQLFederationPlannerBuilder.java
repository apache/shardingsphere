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

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.calcite.adapter.enumerable.EnumerableConvention;
import org.apache.calcite.adapter.enumerable.EnumerableRules;
import org.apache.calcite.plan.Convention;
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
import org.apache.shardingsphere.sqlfederation.compiler.planner.rule.converter.EnumerableModifyConverterRule;
import org.apache.shardingsphere.sqlfederation.compiler.planner.rule.converter.EnumerableScanConverterRule;
import org.apache.shardingsphere.sqlfederation.compiler.planner.rule.transformation.PushFilterIntoScanRule;
import org.apache.shardingsphere.sqlfederation.compiler.planner.rule.transformation.PushProjectIntoScanRule;

import java.util.Collection;
import java.util.LinkedList;

/**
 * SQL federation planner builder.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SQLFederationPlannerBuilder {
    
    private static final int GLOBAL_MATCH_LIMIT = 1024;
    
    private static final int GROUP_MATCH_LIMIT = 500;
    
    /**
     * Build new instance of volcano planner.
     *
     * @param convention convention
     * @return volcano planner instance
     */
    public static RelOptPlanner buildVolcanoPlanner(final Convention convention) {
        RelOptPlanner result = new VolcanoPlanner();
        setUpRules(result, convention);
        return result;
    }
    
    private static void setUpRules(final RelOptPlanner planner, final Convention convention) {
        planner.addRelTraitDef(ConventionTraitDef.INSTANCE);
        planner.addRelTraitDef(RelCollationTraitDef.INSTANCE);
        if (EnumerableConvention.INSTANCE == convention) {
            setUpEnumerableConventionRules(planner);
        }
    }
    
    private static void setUpEnumerableConventionRules(final RelOptPlanner planner) {
        planner.addRule(EnumerableRules.ENUMERABLE_JOIN_RULE);
        planner.addRule(EnumerableRules.ENUMERABLE_MERGE_JOIN_RULE);
        planner.addRule(EnumerableRules.ENUMERABLE_CORRELATE_RULE);
        planner.addRule(EnumerableRules.ENUMERABLE_PROJECT_RULE);
        planner.addRule(EnumerableRules.ENUMERABLE_FILTER_RULE);
        planner.addRule(EnumerableRules.ENUMERABLE_CALC_RULE);
        planner.addRule(EnumerableRules.ENUMERABLE_AGGREGATE_RULE);
        planner.addRule(EnumerableRules.ENUMERABLE_SORT_RULE);
        planner.addRule(EnumerableRules.ENUMERABLE_LIMIT_RULE);
        planner.addRule(EnumerableRules.ENUMERABLE_COLLECT_RULE);
        planner.addRule(EnumerableRules.ENUMERABLE_UNCOLLECT_RULE);
        planner.addRule(EnumerableRules.ENUMERABLE_UNION_RULE);
        planner.addRule(EnumerableRules.ENUMERABLE_REPEAT_UNION_RULE);
        planner.addRule(EnumerableRules.ENUMERABLE_TABLE_SPOOL_RULE);
        planner.addRule(EnumerableRules.ENUMERABLE_INTERSECT_RULE);
        planner.addRule(EnumerableRules.ENUMERABLE_MINUS_RULE);
        planner.addRule(EnumerableRules.ENUMERABLE_VALUES_RULE);
        planner.addRule(EnumerableRules.ENUMERABLE_WINDOW_RULE);
        planner.addRule(EnumerableRules.ENUMERABLE_TABLE_SCAN_RULE);
        planner.addRule(EnumerableRules.ENUMERABLE_TABLE_FUNCTION_SCAN_RULE);
        planner.addRule(EnumerableRules.ENUMERABLE_MATCH_RULE);
        planner.addRule(EnumerableScanConverterRule.DEFAULT_CONFIG.toRule());
        planner.addRule(EnumerableModifyConverterRule.DEFAULT_CONFIG.toRule());
    }
    
    /**
     * Build new instance of hep planner.
     *
     * @return hep planner instance
     */
    public static RelOptPlanner buildHepPlanner() {
        HepProgramBuilder builder = new HepProgramBuilder();
        builder.addGroupBegin().addRuleCollection(getSubQueryRules()).addGroupEnd().addMatchOrder(HepMatchOrder.BOTTOM_UP).addMatchLimit(GROUP_MATCH_LIMIT);
        builder.addGroupBegin().addRuleCollection(getJoinRules()).addGroupEnd().addMatchOrder(HepMatchOrder.BOTTOM_UP).addMatchLimit(GROUP_MATCH_LIMIT);
        builder.addGroupBegin().addRuleCollection(getFilterRules()).addGroupEnd().addMatchOrder(HepMatchOrder.BOTTOM_UP).addMatchLimit(GROUP_MATCH_LIMIT);
        builder.addGroupBegin().addRuleCollection(getProjectRules()).addGroupEnd().addMatchOrder(HepMatchOrder.BOTTOM_UP).addMatchLimit(GROUP_MATCH_LIMIT);
        builder.addGroupBegin().addRuleCollection(getAggregationRules()).addGroupEnd().addMatchOrder(HepMatchOrder.BOTTOM_UP).addMatchLimit(GROUP_MATCH_LIMIT);
        builder.addGroupBegin().addRuleCollection(getSortRules()).addGroupEnd().addMatchOrder(HepMatchOrder.BOTTOM_UP).addMatchLimit(GROUP_MATCH_LIMIT);
        builder.addGroupBegin().addRuleCollection(getPushIntoScanRules()).addGroupEnd().addMatchOrder(HepMatchOrder.BOTTOM_UP).addMatchLimit(GROUP_MATCH_LIMIT);
        builder.addGroupBegin().addRuleCollection(getCalcRules()).addGroupEnd().addMatchOrder(HepMatchOrder.BOTTOM_UP).addMatchLimit(GROUP_MATCH_LIMIT);
        builder.addMatchLimit(GLOBAL_MATCH_LIMIT);
        return new HepPlanner(builder.build());
    }
    
    private static Collection<RelOptRule> getSubQueryRules() {
        Collection<RelOptRule> result = new LinkedList<>();
        result.add(CoreRules.FILTER_SUB_QUERY_TO_CORRELATE);
        result.add(CoreRules.PROJECT_SUB_QUERY_TO_CORRELATE);
        result.add(CoreRules.JOIN_SUB_QUERY_TO_CORRELATE);
        return result;
    }
    
    private static Collection<RelOptRule> getSortRules() {
        Collection<RelOptRule> result = new LinkedList<>();
        result.add(CoreRules.SORT_JOIN_TRANSPOSE);
        return result;
    }
    
    private static Collection<RelOptRule> getPushIntoScanRules() {
        Collection<RelOptRule> result = new LinkedList<>();
        result.add(PushFilterIntoScanRule.Config.DEFAULT.toRule());
        result.add(PushProjectIntoScanRule.Config.DEFAULT.toRule());
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
        result.add(CoreRules.PROJECT_REDUCE_EXPRESSIONS);
        result.add(ProjectRemoveRule.Config.DEFAULT.toRule());
        return result;
    }
    
    private static Collection<RelOptRule> getJoinRules() {
        Collection<RelOptRule> result = new LinkedList<>();
        result.add(CoreRules.PROJECT_JOIN_TRANSPOSE);
        result.add(CoreRules.JOIN_CONDITION_PUSH);
        result.add(CoreRules.JOIN_PUSH_EXPRESSIONS);
        result.add(CoreRules.JOIN_PUSH_TRANSITIVE_PREDICATES);
        return result;
    }
    
    private static Collection<RelOptRule> getFilterRules() {
        Collection<RelOptRule> result = new LinkedList<>();
        result.add(CoreRules.FILTER_INTO_JOIN);
        result.add(CoreRules.FILTER_AGGREGATE_TRANSPOSE);
        result.add(CoreRules.FILTER_SET_OP_TRANSPOSE);
        result.add(CoreRules.FILTER_REDUCE_EXPRESSIONS);
        result.add(CoreRules.FILTER_MERGE);
        result.add(CoreRules.FILTER_PROJECT_TRANSPOSE);
        result.add(CoreRules.JOIN_PUSH_TRANSITIVE_PREDICATES);
        return result;
    }
    
    private static Collection<RelOptRule> getAggregationRules() {
        Collection<RelOptRule> result = new LinkedList<>();
        result.add(CoreRules.AGGREGATE_MERGE);
        result.add(CoreRules.AGGREGATE_REDUCE_FUNCTIONS);
        return result;
    }
}
