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

package org.apache.shardingsphere.infra.optimizer.planner;

import com.google.common.collect.ImmutableList;
import org.apache.calcite.plan.RelOptRule;
import org.apache.calcite.plan.hep.HepMatchOrder;
import org.apache.calcite.rel.rules.CoreRules;
import org.apache.shardingsphere.infra.optimizer.planner.rule.Rules;
import org.apache.shardingsphere.infra.optimizer.planner.rule.SSCalcConverterRule;
import org.apache.shardingsphere.infra.optimizer.planner.rule.SSHashAggregateConverterRule;
import org.apache.shardingsphere.infra.optimizer.planner.rule.SSLimitSortConverterRule;
import org.apache.shardingsphere.infra.optimizer.planner.rule.SSMergeSortConverterRule;
import org.apache.shardingsphere.infra.optimizer.planner.rule.SSNestedLoopJoinConverterRule;
import org.apache.shardingsphere.infra.optimizer.planner.rule.SSProjectConverterRule;
import org.apache.shardingsphere.infra.optimizer.planner.rule.SSScanConverterRule;
import org.apache.shardingsphere.infra.optimizer.planner.rule.SSSortConverterRule;

import java.util.Collection;

public final class PlannerRules {
    
    public static final Collection<? extends RelOptRule> PRE_RULES = ImmutableList.of();
    
    public static final Collection<? extends RelOptRule> SUB_QUERY_RULES = ImmutableList.of(CoreRules.FILTER_SUB_QUERY_TO_CORRELATE,
            CoreRules.PROJECT_SUB_QUERY_TO_CORRELATE, CoreRules.JOIN_SUB_QUERY_TO_CORRELATE);
    
    public static final Collection<? extends RelOptRule> FILTER_RULES = ImmutableList.of(CoreRules.FILTER_INTO_JOIN, CoreRules.JOIN_CONDITION_PUSH,
            CoreRules.FILTER_PROJECT_TRANSPOSE, CoreRules.FILTER_AGGREGATE_TRANSPOSE, CoreRules.FILTER_SET_OP_TRANSPOSE, CoreRules.FILTER_MERGE);
    
    public static final Collection<? extends RelOptRule> PROJECT_RULES = ImmutableList.of(CoreRules.PROJECT_REMOVE, CoreRules.PROJECT_MERGE,
            CoreRules.PROJECT_JOIN_REMOVE, CoreRules.PROJECT_JOIN_REMOVE);
    
    public static final Collection<? extends RelOptRule> PUSH_TO_SCAN_HEP_RULES = ImmutableList.of(Rules.PUSH_FILTER_TO_SCAN_RULE, Rules.PUSH_PROJECT_TO_SCAN_RULE);
    
    public static final Collection<? extends RelOptRule> CALC_RULES = ImmutableList.of(CoreRules.FILTER_TO_CALC, CoreRules.PROJECT_TO_CALC, CoreRules.FILTER_CALC_MERGE, CoreRules.PROJECT_CALC_MERGE);
    
    public static final Collection<? extends RelOptRule> PARTITION_PUSHDOWN_HEP_RULES = ImmutableList.of(Rules.JOIN_TO_SCAN_RULE,
            Rules.PUSH_FILTER_TO_SCAN_RULE, Rules.PUSH_PROJECT_TO_SCAN_RULE, Rules.PUSH_AGG_TO_SCAN_RULE, Rules.PUSH_SORT_TO_SINGLE_ROUTING_RULE,
            Rules.PUSH_SORT_TO_MULTI_ROUTING_RULE);
    
    public static final Collection<? extends RelOptRule> AGG_RULES = ImmutableList.of(CoreRules.AGGREGATE_PROJECT_MERGE, CoreRules.AGGREGATE_JOIN_REMOVE,
            CoreRules.AGGREGATE_JOIN_TRANSPOSE, CoreRules.AGGREGATE_CASE_TO_FILTER);

    public static final Collection<? extends RelOptRule> LIMIT_RULES = ImmutableList.of();
    
    public static final Collection<? extends RelOptRule> JOIN_RULES = ImmutableList.of();

    public static final Collection<? extends RelOptRule> SHARDING_CONVERTER_RULES = ImmutableList.of(
            SSScanConverterRule.DEFAULT_CONFIG.toRule(), SSNestedLoopJoinConverterRule.DEFAULT_CONFIG.toRule(), 
            SSProjectConverterRule.DEFAULT_CONFIG.toRule(), SSCalcConverterRule.DEFAULT_CONFIG.toRule(), 
            SSHashAggregateConverterRule.DEFAULT_CONFIG.toRule(), SSLimitSortConverterRule.Config.DEFAULT.toRule(),
            SSSortConverterRule.DEFAULT_CONFIG.toRule(), SSMergeSortConverterRule.DEFAULT_CONFIG.toRule());
    
    public static final Collection<Collection<? extends RelOptRule>> CASCADES_RULES = ImmutableList.of(AGG_RULES, LIMIT_RULES, PROJECT_RULES,
            PROJECT_RULES, JOIN_RULES);
    
    enum HepRules {
        PRE(PRE_RULES),
        
        FILTER(FILTER_RULES),
        
        PROJECT(PROJECT_RULES),
        
        SCAN_PUSH(PUSH_TO_SCAN_HEP_RULES),
        
        PARTITION_PUSHDOWN(HepMatchOrder.BOTTOM_UP, PARTITION_PUSHDOWN_HEP_RULES),
    
        CALC(CALC_RULES);
        
        private HepMatchOrder matchOrder;
        
        private Collection<? extends RelOptRule> rules;
        
        HepRules(final Collection<? extends RelOptRule> rules) {
            this(HepMatchOrder.ARBITRARY, rules);
        }
    
        HepRules(final HepMatchOrder matchOrder, final Collection<? extends RelOptRule> rules) {
            this.matchOrder = matchOrder;
            this.rules = rules;
        }
        
        public HepMatchOrder getMatchOrder() {
            return matchOrder;
        }
    
        public Collection<? extends RelOptRule> getRules() {
            return rules;
        }
        
    }
}
