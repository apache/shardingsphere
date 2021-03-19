package org.apache.shardingsphere.infra.optimize.planner;

import com.google.common.collect.ImmutableList;
import org.apache.calcite.plan.RelOptRule;
import org.apache.calcite.plan.hep.HepMatchOrder;
import org.apache.shardingsphere.infra.optimize.planner.rule.SSCalcConverterRule;
import org.apache.shardingsphere.infra.optimize.planner.rule.SSHashAggregateConverterRule;
import org.apache.shardingsphere.infra.optimize.planner.rule.SSLimitSortConverterRule;
import org.apache.shardingsphere.infra.optimize.planner.rule.SSNestedLoopJoinConverterRule;
import org.apache.shardingsphere.infra.optimize.planner.rule.SSProjectConverterRule;
import org.apache.shardingsphere.infra.optimize.planner.rule.SSSortConverterRule;

import java.util.Collection;

import static org.apache.calcite.rel.rules.CoreRules.AGGREGATE_CASE_TO_FILTER;
import static org.apache.calcite.rel.rules.CoreRules.AGGREGATE_JOIN_REMOVE;
import static org.apache.calcite.rel.rules.CoreRules.AGGREGATE_JOIN_TRANSPOSE;
import static org.apache.calcite.rel.rules.CoreRules.AGGREGATE_PROJECT_MERGE;
import static org.apache.calcite.rel.rules.CoreRules.FILTER_AGGREGATE_TRANSPOSE;
import static org.apache.calcite.rel.rules.CoreRules.FILTER_CALC_MERGE;
import static org.apache.calcite.rel.rules.CoreRules.FILTER_INTO_JOIN;
import static org.apache.calcite.rel.rules.CoreRules.FILTER_MERGE;
import static org.apache.calcite.rel.rules.CoreRules.FILTER_PROJECT_TRANSPOSE;
import static org.apache.calcite.rel.rules.CoreRules.FILTER_SET_OP_TRANSPOSE;
import static org.apache.calcite.rel.rules.CoreRules.FILTER_SUB_QUERY_TO_CORRELATE;
import static org.apache.calcite.rel.rules.CoreRules.FILTER_TO_CALC;
import static org.apache.calcite.rel.rules.CoreRules.JOIN_CONDITION_PUSH;
import static org.apache.calcite.rel.rules.CoreRules.JOIN_SUB_QUERY_TO_CORRELATE;
import static org.apache.calcite.rel.rules.CoreRules.PROJECT_CALC_MERGE;
import static org.apache.calcite.rel.rules.CoreRules.PROJECT_JOIN_REMOVE;
import static org.apache.calcite.rel.rules.CoreRules.PROJECT_MERGE;
import static org.apache.calcite.rel.rules.CoreRules.PROJECT_REMOVE;
import static org.apache.calcite.rel.rules.CoreRules.PROJECT_SUB_QUERY_TO_CORRELATE;
import static org.apache.calcite.rel.rules.CoreRules.PROJECT_TO_CALC;
import static org.apache.shardingsphere.infra.optimize.planner.rule.Rules.JOIN_TO_SCAN_RULE;
import static org.apache.shardingsphere.infra.optimize.planner.rule.Rules.PUSH_AGG_TO_SCAN_RULE;
import static org.apache.shardingsphere.infra.optimize.planner.rule.Rules.PUSH_FILTER_TO_SCAN_RULE;
import static org.apache.shardingsphere.infra.optimize.planner.rule.Rules.PUSH_PROJECT_TO_SCAN_RULE;
import static org.apache.shardingsphere.infra.optimize.planner.rule.Rules.PUSH_SORT_TO_MULTI_ROUTING_RULE;
import static org.apache.shardingsphere.infra.optimize.planner.rule.Rules.PUSH_SORT_TO_SINGLE_ROUTING_RULE;

public final class PlannerRules {
    
    public static Collection<? extends RelOptRule> PRE_RULES = ImmutableList.of();
    
    public static Collection<? extends RelOptRule> SUB_QUERY_RULES = ImmutableList.of(FILTER_SUB_QUERY_TO_CORRELATE,
                            PROJECT_SUB_QUERY_TO_CORRELATE, JOIN_SUB_QUERY_TO_CORRELATE);
    
    public static Collection<? extends RelOptRule> FILTER_RULES = ImmutableList.of(FILTER_INTO_JOIN, JOIN_CONDITION_PUSH,
            FILTER_PROJECT_TRANSPOSE, FILTER_AGGREGATE_TRANSPOSE, FILTER_SET_OP_TRANSPOSE, FILTER_MERGE);
    
    public static Collection<? extends RelOptRule> PROJECT_RULES = ImmutableList.of(PROJECT_REMOVE, PROJECT_MERGE,
            PROJECT_JOIN_REMOVE, PROJECT_JOIN_REMOVE);
    
    public static Collection<? extends RelOptRule> PUSH_TO_SCAN_HEP_RULES = ImmutableList.of(PUSH_FILTER_TO_SCAN_RULE, PUSH_PROJECT_TO_SCAN_RULE);
    
    public static Collection<? extends RelOptRule> CALC_RULES = ImmutableList.of(FILTER_TO_CALC, PROJECT_TO_CALC, FILTER_CALC_MERGE, PROJECT_CALC_MERGE);
    
    public static Collection<? extends RelOptRule> PARTITION_PUSHDOWN_HEP_RULES = ImmutableList.of(JOIN_TO_SCAN_RULE, 
            PUSH_FILTER_TO_SCAN_RULE, PUSH_PROJECT_TO_SCAN_RULE, PUSH_AGG_TO_SCAN_RULE, PUSH_SORT_TO_SINGLE_ROUTING_RULE, 
            PUSH_SORT_TO_MULTI_ROUTING_RULE);
    
    public static Collection<? extends RelOptRule> AGG_RULES = ImmutableList.of(AGGREGATE_PROJECT_MERGE, AGGREGATE_JOIN_REMOVE,
            AGGREGATE_JOIN_TRANSPOSE, AGGREGATE_CASE_TO_FILTER);

    public static Collection<? extends RelOptRule> LIMIT_RULES = ImmutableList.of();
    

    public static Collection<? extends RelOptRule> JOIN_RULES = ImmutableList.of();

    public static Collection<? extends RelOptRule> SHARDING_CONVERTER_RULES = ImmutableList.of(SSNestedLoopJoinConverterRule.DEFAULT_CONFIG.toRule(), 
            SSProjectConverterRule.DEFAULT_CONFIG.toRule(), SSCalcConverterRule.DEFAULT_CONFIG.toRule(), 
            SSHashAggregateConverterRule.DEFAULT_CONFIG.toRule(), SSLimitSortConverterRule.Config.DEFAULT.toRule(),
            SSSortConverterRule.DEFAULT_CONFIG.toRule());
    
    public static Collection<Collection<? extends RelOptRule>> CASCADES_RULES = ImmutableList.of(AGG_RULES, LIMIT_RULES, PROJECT_RULES,
            PROJECT_RULES, JOIN_RULES);
    
    
    enum HEP_RULE {
        
        PRE(PRE_RULES),
        
        FILTER(FILTER_RULES),
        
        PROJECT(PROJECT_RULES),
        
        SCAN_PUSH(PUSH_TO_SCAN_HEP_RULES),
        
        PARTITION_PUSHDOWN(HepMatchOrder.BOTTOM_UP, PARTITION_PUSHDOWN_HEP_RULES),
    
        CALC(CALC_RULES),
        ;
        
    
        private HepMatchOrder matchOrder;
        
        private Collection<? extends RelOptRule> rules;
        
        HEP_RULE(Collection<? extends RelOptRule> rules) {
            this(HepMatchOrder.ARBITRARY, rules);
        }
    
        public HepMatchOrder getMatchOrder() {
            return matchOrder;
        }
    
        public Collection<? extends RelOptRule> getRules() {
            return rules;
        }
    
        HEP_RULE(HepMatchOrder matchOrder, Collection<? extends RelOptRule> rules) {
            this.matchOrder = matchOrder;
            this.rules = rules;
        }
        
        
    }
}
