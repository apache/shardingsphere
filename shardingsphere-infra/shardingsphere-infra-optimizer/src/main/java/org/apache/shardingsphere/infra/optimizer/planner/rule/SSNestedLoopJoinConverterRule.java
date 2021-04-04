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

package org.apache.shardingsphere.infra.optimizer.planner.rule;

import org.apache.calcite.plan.Convention;
import org.apache.calcite.rel.RelNode;
import org.apache.calcite.rel.convert.ConverterRule;
import org.apache.calcite.rel.logical.LogicalJoin;
import org.apache.shardingsphere.infra.optimizer.planner.ShardingSphereConvention;
import org.apache.shardingsphere.infra.optimizer.rel.physical.SSNestedLoopJoin;

import java.util.ArrayList;
import java.util.List;

public final class SSNestedLoopJoinConverterRule extends ConverterRule {
    
    public static final Config DEFAULT_CONFIG = Config.INSTANCE
            .withConversion(LogicalJoin.class, Convention.NONE,
                    ShardingSphereConvention.INSTANCE, SSNestedLoopJoinConverterRule.class.getName())
            .withRuleFactory(SSNestedLoopJoinConverterRule::new);
    
    protected SSNestedLoopJoinConverterRule(final Config config) {
        super(config);
    }
    
    @Override
    public RelNode convert(final RelNode rel) {
        LogicalJoin join = (LogicalJoin) rel;
        List<RelNode> newInputs = new ArrayList<>();
        for (RelNode input : join.getInputs()) {
            RelNode newInput = input;
            if (!(input.getConvention() instanceof ShardingSphereConvention)) {
                newInput = convert(input, input.getTraitSet().replace(ShardingSphereConvention.INSTANCE));
            }
            newInputs.add(newInput);
        }
        final RelNode left = newInputs.get(0);
        final RelNode right = newInputs.get(1);
        return SSNestedLoopJoin.create(left, right, join.getCondition(), join.getVariablesSet(), join.getJoinType());
    }
}
