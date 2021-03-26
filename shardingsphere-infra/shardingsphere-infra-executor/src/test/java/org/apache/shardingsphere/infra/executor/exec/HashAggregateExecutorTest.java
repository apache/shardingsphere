package org.apache.shardingsphere.infra.executor.exec;

import org.apache.calcite.rel.RelNode;
import org.apache.calcite.rel.logical.LogicalAggregate;
import org.apache.shardingsphere.infra.executor.exec.meta.Row;
import org.apache.shardingsphere.infra.optimize.rel.physical.SSHashAggregate;
import org.apache.shardingsphere.infra.optimize.rel.physical.SSScan;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class HashAggregateExecutorTest extends BaseExecutorTest {
    
    @Before
    public void init() {
        relBuilder = relBuilder();
    }
    
    @Test
    public void testHashAggregateExecutor() {
        RelNode tableScan = relBuilder.scan("t_order_item").build();
        SSScan scan = SSScan.create(relBuilder.getCluster(), tableScan.getTraitSet(), tableScan);
    
        RelNode aggRelNode = relBuilder.scan("t_order_item")
                .aggregate(relBuilder.groupKey("order_id"), relBuilder.count(false, "C"))
                .build();
        Assert.assertTrue(aggRelNode instanceof LogicalAggregate);
        LogicalAggregate logicalAggregate = (LogicalAggregate) aggRelNode;
        SSHashAggregate hashAggregate = SSHashAggregate.create(relBuilder.getCluster(), scan.getTraitSet(), scan, 
                logicalAggregate.getGroupSet(), logicalAggregate.getGroupSets(), logicalAggregate.getAggCallList());
        
        Executor executor = buildExecutor(hashAggregate);
        while (executor.moveNext()) {
            Row row = executor.current();
            Assert.assertNotNull(row);
        }
    }
    
}
