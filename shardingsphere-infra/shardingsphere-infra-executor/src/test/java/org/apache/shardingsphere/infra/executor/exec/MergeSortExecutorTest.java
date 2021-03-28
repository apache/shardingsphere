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
import org.apache.calcite.rex.RexBuilder;
import org.apache.shardingsphere.infra.executor.exec.meta.Row;
import org.apache.shardingsphere.infra.optimize.rel.physical.SSMergeSort;
import org.apache.shardingsphere.infra.optimize.rel.physical.SSScan;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.Map;

public class MergeSortExecutorTest extends BaseExecutorTest {
    
    @Before
    public void init() {
        relBuilder = relBuilder();
    }
    
    @Test
    public void testMergeSortExecutor() throws SQLException {
        RelNode tableScan = relBuilder.scan("t_order_item").sort(relBuilder.field("order_item_id")).build();
        Assert.assertTrue(tableScan instanceof LogicalSort);
        SSScan scan = SSScan.create(relBuilder.getCluster(), tableScan.getTraitSet(), tableScan);
    
        int offset = 1;
        int fetch = 3;
        RexBuilder rexBuilder = relBuilder.getRexBuilder();
        SSMergeSort mergeSort = SSMergeSort.create(scan.getTraitSet(), scan, ((LogicalSort) tableScan).getCollation(),
                rexBuilder.makeBigintLiteral(new BigDecimal(offset)), rexBuilder.makeBigintLiteral(new BigDecimal(fetch)));
        int rowCount = 0;
        Executor executor = buildExecutor(mergeSort);
        Map<String, Integer> columNameIndexMap = createColumnLabelAndIndexMap(executor.getMetaData());
        Comparable pre = null;
        while (executor.moveNext()) {
            Row row = executor.current();
            Comparable value = row.getColumnValue(columNameIndexMap.get("order_item_id"));
            if (pre == null) {
                pre = value;
            } else {
                Assert.assertTrue(pre.compareTo(value) <= 0);
            }
            rowCount++;
        }
    
        Assert.assertEquals(fetch, rowCount);
        executor.close();
    }
}
