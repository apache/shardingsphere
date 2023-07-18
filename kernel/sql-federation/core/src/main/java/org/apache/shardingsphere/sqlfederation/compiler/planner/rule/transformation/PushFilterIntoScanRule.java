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

package org.apache.shardingsphere.sqlfederation.compiler.planner.rule.transformation;

import org.apache.calcite.plan.RelOptRuleCall;
import org.apache.calcite.plan.RelRule;
import org.apache.calcite.rel.logical.LogicalFilter;
import org.apache.calcite.rel.rules.TransformationRule;
import org.apache.calcite.rex.RexCall;
import org.apache.calcite.rex.RexNode;
import org.apache.shardingsphere.sqlfederation.compiler.operator.logical.LogicalScan;
import org.immutables.value.Value;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;

/**
 * Push filter into scan rule.
 */
@Value.Enclosing
public final class PushFilterIntoScanRule extends RelRule<PushFilterIntoScanRule.Config> implements TransformationRule {
    
    private static final Collection<String> SYSTEM_SCHEMAS = new HashSet<>(Arrays.asList("information_schema", "performance_schema", "mysql", "sys", "shardingsphere", "pg_catalog"));
    
    private static final String CORRELATE_REFERENCE = "$cor";
    
    private PushFilterIntoScanRule(final Config config) {
        super(config);
    }
    
    @Override
    public boolean matches(final RelOptRuleCall call) {
        LogicalScan logicalScan = call.rel(1);
        for (String each : logicalScan.getTable().getQualifiedName()) {
            if (SYSTEM_SCHEMAS.contains(each.toLowerCase())) {
                return false;
            }
        }
        LogicalFilter logicalFilter = call.rel(0);
        RexNode condition = logicalFilter.getCondition();
        return !(condition instanceof RexCall) || !containsCorrelate(((RexCall) condition).getOperands());
    }
    
    private boolean containsCorrelate(final Collection<RexNode> operands) {
        for (RexNode each : operands) {
            if (each.toString().contains(CORRELATE_REFERENCE)) {
                return true;
            }
        }
        return false;
    }
    
    @Override
    public void onMatch(final RelOptRuleCall call) {
        LogicalFilter logicalFilter = call.rel(0);
        LogicalScan logicalScan = call.rel(1);
        logicalScan.pushDown(logicalFilter);
        call.transformTo(logicalScan);
    }
    
    @Value.Immutable
    public interface Config extends RelRule.Config {
        
        Config DEFAULT = ImmutablePushFilterIntoScanRule.Config.builder().description(PushFilterIntoScanRule.class.getSimpleName())
                .operandSupplier(b0 -> b0.operand(LogicalFilter.class).inputs(b1 -> b1.operand(LogicalScan.class).anyInputs())).build();
        
        @Override
        default PushFilterIntoScanRule toRule() {
            return new PushFilterIntoScanRule(this);
        }
    }
}
