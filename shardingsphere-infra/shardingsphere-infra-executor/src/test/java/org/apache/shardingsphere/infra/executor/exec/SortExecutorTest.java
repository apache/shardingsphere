package org.apache.shardingsphere.infra.executor.exec;

import org.apache.calcite.rel.RelNode;
import org.apache.calcite.rel.logical.LogicalSort;
import org.apache.calcite.sql.fun.SqlStdOperatorTable;
import org.apache.shardingsphere.infra.executor.exec.meta.Row;
import org.apache.shardingsphere.infra.optimize.rel.physical.SSScan;
import org.apache.shardingsphere.infra.optimize.rel.physical.SSSort;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class SortExecutorTest extends BaseExecutorTest {
    
    @Before
    public void init() {
        relBuilder = relBuilder();
    }
    
    @Test
    public void testSortExecutor() {
        RelNode tableScan = relBuilder.scan("t_order_item").build();
        SSScan scan = SSScan.create(relBuilder.getCluster(), tableScan.getTraitSet(), tableScan);
    
        RelNode relNode = relBuilder.scan("t_order_item")
                .filter(relBuilder.call(SqlStdOperatorTable.EQUALS, relBuilder.field("user_id"), relBuilder.literal(1000)))
                .project(relBuilder.field("order_item_id"), relBuilder.field("order_id"), relBuilder.field("user_id"))
                .sort(0)
                .build();
    
        Assert.assertTrue(relNode instanceof LogicalSort);
        LogicalSort logicalSort = (LogicalSort) relNode;
        SSSort sort = SSSort.create(scan, logicalSort.getCollation());
    
        Executor executor = buildExecutor(sort);
        while(executor.moveNext()) {
            Row row = executor.current();
            Assert.assertNotNull(row);
        }
    }
}
