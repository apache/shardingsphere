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
        /*while (executor.moveNext()) {
            Row row = executor.current();
            Comparable value = row.getColumnValue(columnNameIdxMap.get("order_item_id"));
            if (pre != null) {
                Assert.assertTrue(pre.compareTo(value) < 0);
            }
            pre = value;
            rowCount++;
        }
        Assert.assertEquals(fetch, rowCount);*/
        executor.close();
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
        /*while (executor.moveNext()) {
            Row row = executor.current();
            Comparable value = row.getColumnValue(columnNameIdxMap.get("order_item_id"));
            if (pre != null) {
                Assert.assertTrue(pre.compareTo(value) < 0);
            }
            pre = value;
            rowCount++;
        }
        Assert.assertEquals(fetch, rowCount);*/
        executor.close();
    }
}
