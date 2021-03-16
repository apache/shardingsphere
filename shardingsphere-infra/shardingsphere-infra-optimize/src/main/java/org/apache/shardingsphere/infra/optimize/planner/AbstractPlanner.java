package org.apache.shardingsphere.infra.optimize.planner;

import com.google.common.collect.ImmutableList;
import lombok.extern.slf4j.Slf4j;
import org.apache.calcite.plan.RelOptCostImpl;
import org.apache.calcite.plan.hep.HepPlanner;
import org.apache.calcite.plan.hep.HepProgram;
import org.apache.calcite.plan.hep.HepProgramBuilder;
import org.apache.calcite.rel.RelNode;
import org.apache.shardingsphere.infra.optimize.planner.PlannerRules.HEP_RULE;
import org.apache.shardingsphere.infra.optimize.rel.CustomLogicalRelConverter;

@Slf4j
public abstract class AbstractPlanner implements Planner {
    
    protected RelNode rewrite(RelNode logicalRelNode) {
        
        logicalRelNode = CustomLogicalRelConverter.convert(logicalRelNode);
        
        HepProgramBuilder hepProgramBuilder = HepProgram.builder();
        for(HEP_RULE hepRules : HEP_RULE.values()) {
            hepProgramBuilder.addMatchOrder(hepRules.getMatchOrder());
            hepProgramBuilder.addGroupBegin();
            hepProgramBuilder.addRuleCollection(ImmutableList.copyOf(hepRules.getRules()));
            hepProgramBuilder.addGroupEnd();
        }
        HepPlanner hepPlanner = new HepPlanner(hepProgramBuilder.build(), null, true, null, RelOptCostImpl.FACTORY);
        hepPlanner.setRoot(logicalRelNode);
        RelNode rewritedRelNode = hepPlanner.findBestExp();
        log("rewrited plan ", rewritedRelNode);
        return rewritedRelNode;
    }
    
    protected abstract void log(String desc, RelNode relNode);
}
