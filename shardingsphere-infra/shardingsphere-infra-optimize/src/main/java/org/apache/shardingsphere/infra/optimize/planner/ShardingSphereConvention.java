package org.apache.shardingsphere.infra.optimize.planner;

import org.apache.calcite.plan.Convention;
import org.apache.calcite.plan.ConventionTraitDef;
import org.apache.calcite.plan.RelOptPlanner;
import org.apache.calcite.plan.RelTrait;
import org.apache.calcite.plan.RelTraitDef;
import org.apache.shardingsphere.infra.optimize.rel.physical.SSRel;

/**
 * Family of calling conventions that return results as an
 *  {@link SSRel}.
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
    public boolean satisfies(final RelTrait trait) {
        return this == trait;
    }

    @Override
    public void register(final RelOptPlanner planner) {
        PlannerRules.SHARDING_CONVERTER_RULES.forEach(planner::addRule);
    }
}
