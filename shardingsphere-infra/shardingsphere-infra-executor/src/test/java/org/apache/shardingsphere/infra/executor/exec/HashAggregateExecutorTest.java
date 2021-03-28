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
        executor.close();
    }
    
}
