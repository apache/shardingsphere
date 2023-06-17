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
import org.apache.calcite.rel.RelNode;
import org.apache.calcite.rel.core.RelFactories;
import org.apache.calcite.rel.logical.LogicalProject;
import org.apache.calcite.rel.rules.TransformationRule;
import org.apache.calcite.rex.RexInputRef;
import org.apache.calcite.rex.RexNode;
import org.apache.calcite.tools.RelBuilderFactory;
import org.apache.shardingsphere.sqlfederation.compiler.operator.physical.enumerable.EnumerablePushDownTableScan;

import java.util.ArrayList;
import java.util.List;

/**
 * Push project into scan rule.
 */
public final class PushProjectIntoScanRule extends RelOptRule implements TransformationRule {
    
    public static final PushProjectIntoScanRule INSTANCE = new PushProjectIntoScanRule(RelFactories.LOGICAL_BUILDER);
    
    public PushProjectIntoScanRule(final RelBuilderFactory relBuilderFactory) {
        super(operand(LogicalProject.class, operand(EnumerablePushDownTableScan.class, none())), relBuilderFactory, "TranslatableProjectRule");
    }
    
    @Override
    public void onMatch(final RelOptRuleCall call) {
        LogicalProject project = call.rel(0);
        int[] fields = getProjectFields(project.getProjects());
        if (0 == fields.length) {
            return;
        }
        EnumerablePushDownTableScan scan = call.rel(1);
        List<RexNode> expressions = project.getProjects();
        if (fields.length == expressions.size()) {
            call.transformTo(new EnumerablePushDownTableScan(scan.getCluster(), scan.getTable(), scan.getSqlFederationTable(), scan.getFilters(), fields));
        } else {
            EnumerablePushDownTableScan tableScan =
                    new EnumerablePushDownTableScan(scan.getCluster(), scan.getTable(), scan.getSqlFederationTable(), scan.getFilters(), fields, expressions.size(), expressions);
            RelNode logicalProject = LogicalProject.create(tableScan, project.getHints(), project.getProjects(), project.getRowType());
            call.transformTo(logicalProject);
        }
    }
    
    private int[] getProjectFields(final List<RexNode> rexNodes) {
        List<Integer> rexInputRefs = new ArrayList<>();
        for (RexNode exp : rexNodes) {
            if (exp instanceof RexInputRef) {
                rexInputRefs.add(((RexInputRef) exp).getIndex());
            }
        }
        int[] result = new int[rexInputRefs.size()];
        for (int index = 0; index < rexInputRefs.size(); index++) {
            result[index] = rexInputRefs.get(index);
        }
        return result;
    }
}
