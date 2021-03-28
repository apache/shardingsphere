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
import org.apache.calcite.rel.logical.LogicalSort;
import org.apache.calcite.sql.fun.SqlStdOperatorTable;
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
        /*while (executor.moveNext()) {
            Row row = executor.current();
            Assert.assertNotNull(row);
        }*/
        executor.close();
    }
}
