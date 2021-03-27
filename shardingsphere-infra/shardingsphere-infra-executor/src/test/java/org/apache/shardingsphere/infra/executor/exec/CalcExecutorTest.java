/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
