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

import org.apache.calcite.plan.RelOptRuleCall;
import org.apache.calcite.plan.RelRule;
import org.apache.calcite.rel.logical.LogicalFilter;
import org.apache.calcite.rel.rules.TransformationRule;
import org.apache.shardingsphere.infra.optimizer.rel.logical.LogicalScan;

/**
 * Pushdown LogicalFilter to LogicalScan.
 */
public final class PushFilterToScanRule extends RelRule<PushFilterToScanRule.Config> implements TransformationRule {
    
    public PushFilterToScanRule(final Config config) {
        super(config);
    }
    
    @Override
    public void onMatch(final RelOptRuleCall call) {
        LogicalFilter logicalFilter = call.rel(0);
        LogicalScan logicalScan = call.rel(1);
        logicalScan.pushdown(logicalFilter);
        call.transformTo(logicalScan);
    }
    
    public interface Config extends RelRule.Config {
        Config DEFAULT = EMPTY.withOperandSupplier(b0 -> b0.operand(LogicalFilter.class).inputs(b1 -> b1.operand(LogicalScan.class).anyInputs())).as(Config.class);
    
        @Override
        default PushFilterToScanRule toRule() {
            return new PushFilterToScanRule(this);
        }
    }
}
