package org.apache.shardingsphere.infra.executor.exec;

import org.apache.calcite.rel.RelNode;
import org.apache.calcite.rel.logical.LogicalSort;
import org.apache.shardingsphere.infra.executor.exec.meta.Row;
import org.apache.shardingsphere.infra.optimize.rel.physical.SSLimitSort;
import org.apache.shardingsphere.infra.optimize.rel.physical.SSScan;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.sql.SQLException;
import java.util.Map;

public class LimitSortExecutorTest extends BaseExecutorTest {
    
    @Before
    public void init() {
        relBuilder = relBuilder();
    }
    
    @Test
    public void testLimitSortExecutor() throws SQLException {
        RelNode tableScan = relBuilder.scan("t_order_item").build();
        SSScan scan = SSScan.create(relBuilder.getCluster(), tableScan.getTraitSet(), tableScan);
        
        int offset = 0;
        int fetch = 3;
        RelNode relNode = relBuilder.scan("t_order_item")
                .sortLimit(offset, fetch, relBuilder.field("order_item_id"))
                .build();
    
        Assert.assertTrue(relNode instanceof LogicalSort);
        LogicalSort logicalSort = (LogicalSort) relNode;
        SSLimitSort limitSort = SSLimitSort.create(scan, logicalSort.getCollation(), logicalSort.offset, logicalSort.fetch);
    
        Executor executor = buildExecutor(limitSort);
        Map<String, Integer> columnNameIdxMap = createColumnLabelAndIndexMap(executor.getMetaData());
        int rowCount = 0;
        Comparable pre = null;
        while(executor.moveNext()) {
            Row row = executor.current();
            Comparable value = row.getColumnValue(columnNameIdxMap.get("order_item_id"));
            if (pre != null) {
                Assert.assertTrue(pre.compareTo(value) < 0);
            }
            pre = value;
            rowCount++;
        }
        Assert.assertEquals(fetch, rowCount);
    }
    
    @Test
    public void testTopNExecutor() throws SQLException {
        RelNode tableScan = relBuilder.scan("t_order_item").build();
        SSScan scan = SSScan.create(relBuilder.getCluster(), tableScan.getTraitSet(), tableScan);
    
        int offset = 0;
        int fetch = 3;
        RelNode relNode = relBuilder.scan("t_order_item")
                .sortLimit(offset, fetch, relBuilder.field("order_item_id"))
                .build();
    
        Assert.assertTrue(relNode instanceof LogicalSort);
        LogicalSort logicalSort = (LogicalSort) relNode;
        SSLimitSort limitSort = SSLimitSort.create(scan, logicalSort.getCollation(), logicalSort.offset, logicalSort.fetch);
    
        Executor executor = buildExecutor(limitSort);
        Assert.assertTrue(executor instanceof TopNExecutor);
        Map<String, Integer> columnNameIdxMap = createColumnLabelAndIndexMap(executor.getMetaData());
        Comparable pre = null;
        int rowCount = 0;
        while (executor.moveNext()) {
            Row row = executor.current();
            Comparable value = row.getColumnValue(columnNameIdxMap.get("order_item_id"));
            if (pre != null) {
                Assert.assertTrue(pre.compareTo(value) < 0);
            }
            pre = value;
            rowCount++;
        }
        Assert.assertEquals(fetch, rowCount);
    }
}
