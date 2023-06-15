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

import org.apache.calcite.plan.RelOptRule;
import org.apache.calcite.plan.RelOptRuleCall;
import org.apache.calcite.rel.core.RelFactories;
import org.apache.calcite.rel.logical.LogicalFilter;
import org.apache.calcite.rel.rules.TransformationRule;
import org.apache.calcite.rex.RexCall;
import org.apache.calcite.rex.RexNode;
import org.apache.calcite.tools.RelBuilderFactory;
import org.apache.shardingsphere.sqlfederation.compiler.operator.physical.enumerable.EnumerablePushDownTableScan;

import java.util.Collections;
import java.util.regex.Pattern;

/**
 * Push filter into scan rule.
 */
public final class PushFilterIntoScanRule extends RelOptRule implements TransformationRule {
    
    public static final PushFilterIntoScanRule INSTANCE = new PushFilterIntoScanRule(RelFactories.LOGICAL_BUILDER);
    
    private static final Pattern CONDITION_PATTERN = Pattern.compile("\\$[A-Za-z]");
    
    private static final Pattern CONDITION_FUNCTION_PATTERN = Pattern.compile("[A-Za-z_]+\\.[A-Za-z_]+\\(.*\\)");
    
    private static final Pattern CONDITION_COMPLEX_PATTERN = Pattern.compile("NEGATED POSIX REGEX CASE SENSITIVE");
    
    public PushFilterIntoScanRule(final RelBuilderFactory relBuilderFactory) {
        super(operand(LogicalFilter.class, operand(EnumerablePushDownTableScan.class, none())), relBuilderFactory, "TranslatableFilterRule");
    }
    
    @Override
    public boolean matches(final RelOptRuleCall call) {
        LogicalFilter filter = call.rel(0);
        RexCall condition = (RexCall) filter.getCondition();
        for (RexNode each : condition.getOperands()) {
            if (CONDITION_PATTERN.matcher(each.toString()).find()
                    || CONDITION_FUNCTION_PATTERN.matcher(each.toString()).find()
                    || CONDITION_COMPLEX_PATTERN.matcher(each.toString()).find()) {
                return false;
            }
        }
        return true;
    }
    
    @Override
    public void onMatch(final RelOptRuleCall call) {
        LogicalFilter filter = call.rel(0);
        EnumerablePushDownTableScan scan = call.rel(1);
        call.transformTo(new EnumerablePushDownTableScan(scan.getCluster(), scan.getTable(), scan.getSqlFederationTable(), Collections.singletonList(filter.getCondition()), scan.getFields()));
    }
}
