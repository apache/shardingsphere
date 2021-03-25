package org.apache.shardingsphere.infra.executor.exec;

import org.apache.calcite.rel.RelNode;
import org.apache.calcite.rex.RexBuilder;
import org.apache.shardingsphere.infra.optimize.rel.physical.SSScan;
import org.junit.Before;
import org.junit.Test;

public class HashAggregateExecutorTest extends BaseExecutorTest {
    
    @Before
    public void init() {
        relBuilder = relBuilder();
    }
    
    @Test
    public void test() {
        RelNode tableScan = relBuilder.scan("t_order_item").build();
        SSScan scan = SSScan.create(relBuilder.getCluster(), tableScan.getTraitSet(), tableScan);
    
        RexBuilder rexBuilder = relBuilder.getRexBuilder();
        /*GroupKey groupByKey = relBuilder.groupKey();
        SSHashAggregate hashAggregate = SSHashAggregate.create(relBuilder.getCluster(), scan.getTraitSet(), scan, groupByKey)
        */
        //buildExecutor();
    }
    
}
