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

import com.cedarsoftware.util.CaseInsensitiveSet;
import org.apache.calcite.plan.RelOptRuleCall;
import org.apache.calcite.plan.RelRule;
import org.apache.calcite.rel.logical.LogicalProject;
import org.apache.calcite.rel.rules.TransformationRule;
import org.apache.calcite.rex.RexCall;
import org.apache.calcite.rex.RexNode;
import org.apache.calcite.rex.RexSubQuery;
import org.apache.shardingsphere.sqlfederation.compiler.rel.operator.logical.LogicalScan;
import org.immutables.value.Value;

import java.util.Arrays;
import java.util.Collection;

/**
 * Push project into scan rule.
 */
@Value.Enclosing
public final class PushProjectIntoScanRule extends RelRule<PushProjectIntoScanRule.Config> implements TransformationRule {
    
    private static final Collection<String> SYSTEM_SCHEMAS = new CaseInsensitiveSet<>(Arrays.asList("information_schema", "performance_schema", "mysql", "sys", "shardingsphere", "pg_catalog"));
    
    private static final String CASE_FUNCTION_NAME = "CAST";
    
    private PushProjectIntoScanRule(final Config config) {
        super(config);
    }
    
    @Override
    public boolean matches(final RelOptRuleCall call) {
        LogicalScan logicalScan = call.rel(1);
        for (String each : logicalScan.getTable().getQualifiedName()) {
            if (SYSTEM_SCHEMAS.contains(each)) {
                return false;
            }
        }
        LogicalProject logicalProject = call.rel(0);
        for (RexNode each : logicalProject.getProjects()) {
            if (each instanceof RexSubQuery || containsCastFunction(each)) {
                return false;
            }
        }
        return true;
    }
    
    private boolean containsCastFunction(final RexNode rexNode) {
        return rexNode instanceof RexCall && CASE_FUNCTION_NAME.equalsIgnoreCase(((RexCall) rexNode).getOperator().getName());
    }
    
    @Override
    public void onMatch(final RelOptRuleCall call) {
        LogicalProject logicalProject = call.rel(0);
        LogicalScan logicalScan = call.rel(1);
        logicalScan.pushDown(logicalProject);
        call.transformTo(logicalScan);
    }
    
    @Value.Immutable
    public interface Config extends RelRule.Config {
        
        Config DEFAULT = ImmutablePushProjectIntoScanRule.Config.builder().description(PushProjectIntoScanRule.class.getSimpleName())
                .operandSupplier(b0 -> b0.operand(LogicalProject.class).inputs(b1 -> b1.operand(LogicalScan.class).anyInputs())).build();
        
        @Override
        default PushProjectIntoScanRule toRule() {
            return new PushProjectIntoScanRule(this);
        }
    }
}
