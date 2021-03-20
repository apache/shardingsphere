package org.apache.shardingsphere.infra.optimize.planner.rule;

import org.apache.calcite.plan.Convention;
import org.apache.calcite.rel.RelNode;
import org.apache.calcite.rel.convert.ConverterRule;
import org.apache.calcite.rel.logical.LogicalProject;
import org.apache.shardingsphere.infra.optimize.planner.ShardingSphereConvention;
import org.apache.shardingsphere.infra.optimize.rel.physical.SSProject;

public class SSProjectConverterRule extends ConverterRule {
    
    public static final Config DEFAULT_CONFIG = Config.INSTANCE
            .withConversion(LogicalProject.class, Convention.NONE,
                    ShardingSphereConvention.INSTANCE, SSProjectConverterRule.class.getName())
            .withRuleFactory(SSProjectConverterRule::new);
    
    protected SSProjectConverterRule(final Config config) {
        super(config);
    }
    
    @Override
    public RelNode convert(final RelNode rel) {
        LogicalProject project = (LogicalProject) rel;
        return SSProject.create(convert(project.getInput(), out), project.getProjects(), project.getRowType());
    }
}
