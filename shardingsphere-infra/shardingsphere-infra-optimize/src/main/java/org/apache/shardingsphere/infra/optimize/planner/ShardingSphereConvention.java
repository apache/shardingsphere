package org.apache.shardingsphere.infra.optimize.planner;

import org.apache.calcite.plan.Convention;
import org.apache.calcite.plan.ConventionTraitDef;
import org.apache.calcite.plan.RelOptPlanner;
import org.apache.calcite.plan.RelTrait;
import org.apache.calcite.plan.RelTraitDef;
import org.apache.shardingsphere.infra.optimize.rel.physical.SSRel;

/**
 * Family of calling conventions that return results as an
 *  {@link org.apache.calcite.linq4j.Enumerable}.
 */
public enum ShardingSphereConvention implements Convention {

    INSTANCE;

    @Override
    public Class getInterface() {
        return SSRel.class;
    }

    @Override
    public String getName() {
        return this.getClass().getName();
    }

    @Override
    public RelTraitDef getTraitDef() {
        return ConventionTraitDef.INSTANCE;
    }

    @Override
    public boolean satisfies(RelTrait trait) {
        return this == trait;
    }

    @Override
    public void register(RelOptPlanner planner) {
        PlannerRules.SHARDING_CONVERTER_RULES.forEach(planner::addRule);
    }
}
