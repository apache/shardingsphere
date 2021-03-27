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
import org.apache.calcite.rel.core.JoinRelType;
import org.apache.calcite.rel.type.RelDataTypeField;
import org.apache.calcite.rex.RexBuilder;
import org.apache.calcite.rex.RexNode;
import org.apache.calcite.sql.fun.SqlStdOperatorTable;
import org.apache.shardingsphere.infra.executor.exec.meta.Row;
import org.apache.shardingsphere.infra.optimize.rel.physical.SSNestedLoopJoin;
import org.apache.shardingsphere.infra.optimize.rel.physical.SSScan;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class NestedLoopJoinExecutorTest extends BaseExecutorTest {
    
    @Before
    public void init() {
        relBuilder = relBuilder();
    }
    
    @Test
    public void testInnerJoin() {
        relBuilder.scan("t_order");
        List<RexNode> orderPredicates = new ArrayList<>();
        orderPredicates.add(relBuilder.call(SqlStdOperatorTable.EQUALS, relBuilder.field("user_id"), relBuilder.literal(10)));
        relBuilder.filter(orderPredicates);
        RelNode orderRel = relBuilder.build();
        SSScan orderScan = SSScan.create(relBuilder.getCluster(), orderRel.getTraitSet(), orderRel);
        
        relBuilder.scan("t_user");
        List<RexNode> userPredicates = new ArrayList<>();
        userPredicates.add(relBuilder.call(SqlStdOperatorTable.EQUALS, relBuilder.field("user_id"), relBuilder.literal(10)));
        relBuilder.filter(userPredicates);
        RelNode userRel = relBuilder.build();
        SSScan userScan = SSScan.create(relBuilder.getCluster(), userRel.getTraitSet(), userRel);
    
        RexBuilder rexBuilder = relBuilder.getRexBuilder();
        RelDataTypeField userUserIdField = getField(userRel, "user_id");
        RelDataTypeField orderUserIdField = getField(orderRel, "user_id");
        RexNode joinCondition = rexBuilder.makeCall(SqlStdOperatorTable.EQUALS, 
                rexBuilder.makeInputRef(userUserIdField.getType(), userUserIdField.getIndex()), 
                rexBuilder.makeInputRef(orderUserIdField.getType(), userRel.getRowType().getFieldCount() + orderUserIdField.getIndex()));
        SSNestedLoopJoin nestedLoopJoin = SSNestedLoopJoin.create(userScan, orderScan, joinCondition, Collections.emptySet(), JoinRelType.INNER);
        
        int rowCount = 0;
        Executor executor = buildExecutor(nestedLoopJoin);
        while(executor.moveNext()) {
            Row row = executor.current();
            Assert.assertNotNull(row);
            rowCount++;
        }
        Assert.assertEquals(3, rowCount);
    }
    
    private RelDataTypeField getField(RelNode relNode, String field) {
        return relNode.getRowType().getField(field, false, false);
    }
}
