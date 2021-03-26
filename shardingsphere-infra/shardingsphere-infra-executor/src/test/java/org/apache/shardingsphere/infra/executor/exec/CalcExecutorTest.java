package org.apache.shardingsphere.infra.executor.exec;

import org.apache.calcite.rel.RelNode;
import org.apache.calcite.rel.logical.LogicalFilter;
import org.apache.calcite.rel.logical.LogicalProject;
import org.apache.calcite.rex.RexProgram;
import org.apache.calcite.sql.fun.SqlStdOperatorTable;
import org.apache.shardingsphere.infra.executor.exec.meta.Row;
import org.apache.shardingsphere.infra.optimize.rel.physical.SSCalc;
import org.apache.shardingsphere.infra.optimize.rel.physical.SSScan;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;

public class CalcExecutorTest extends BaseExecutorTest {
    
    @Before
    public void init() {
        relBuilder = relBuilder();
    }
    
    @Test
    public void testCalcExecutor() {
        RelNode tableScan = relBuilder.scan("t_order_item").build();
        SSScan scan = SSScan.create(relBuilder.getCluster(), tableScan.getTraitSet(), tableScan);
        RelNode relNode = relBuilder.scan("t_order_item")
                .filter(relBuilder.call(SqlStdOperatorTable.EQUALS, relBuilder.field("user_id"), relBuilder.literal(1000)))
                .project(relBuilder.field("order_item_id"), relBuilder.field("order_id"), relBuilder.field("user_id"))
                .build();
        Assert.assertTrue(relNode instanceof LogicalProject);
        Assert.assertTrue(((LogicalProject)relNode).getInput() instanceof LogicalFilter);
        LogicalFilter logicalFilter = (LogicalFilter)((LogicalProject)relNode).getInput();
        RexProgram program = RexProgram.create(scan.getRowType(), ((LogicalProject)relNode).getProjects(), logicalFilter.getCondition(),
                Arrays.asList("order_id", "order_item_id", "user_id"), relBuilder.getRexBuilder());
        SSCalc calc = SSCalc.create(scan, program);
    
        Executor executor = buildExecutor(calc);
        while(executor.moveNext()) {
            Row row = executor.current();
            Assert.assertNotNull(row);
        }
    }
}
