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

package org.apache.shardingsphere.infra.federation.optimizer.metadata.translatable;

import org.apache.calcite.plan.RelOptRule;
import org.apache.calcite.plan.RelOptRuleCall;
import org.apache.calcite.rel.RelNode;
import org.apache.calcite.rel.core.RelFactories;
import org.apache.calcite.rel.logical.LogicalProject;
import org.apache.calcite.rex.RexInputRef;
import org.apache.calcite.rex.RexNode;
import org.apache.calcite.tools.RelBuilderFactory;

import java.util.LinkedList;
import java.util.List;

/**
 * Planner rule for pushing projections into table scan.
 */
public class TranslatableProjectRule extends RelOptRule {
    
    public static final TranslatableProjectRule INSTANCE = new TranslatableProjectRule(RelFactories.LOGICAL_BUILDER);
    
    public TranslatableProjectRule(final RelBuilderFactory relBuilderFactory) {
        super(operand(LogicalProject.class, operand(TranslatableTableScan.class, none())), relBuilderFactory, "TranslatableProjectRule");
    }
    
    @Override
    public void onMatch(final RelOptRuleCall call) {
        LogicalProject project = call.rel(0);
        TranslatableTableScan scan = call.rel(1);
        int[] fields = getProjectFields(project.getProjects());
        List expressions = project.getProjects();
        int number = project.getProjects().size();
        if (fields.length == 0) {
            return;
        } else if (fields.length == number) {
            call.transformTo(new TranslatableTableScan(scan.getCluster(), scan.getTable(), scan.getTranslatableTable(), scan.getFilters(), fields));
        } else {
            TranslatableTableScan tableScan = new TranslatableTableScan(scan.getCluster(), scan.getTable(), scan.getTranslatableTable(), scan.getFilters(), fields, number, expressions);
            RelNode logicalProject = LogicalProject.create(tableScan, project.getHints(), project.getProjects(), project.getRowType());
            call.transformTo(logicalProject);
        }
    }
    
    private int[] getProjectFields(final List<RexNode> rexNodes) {
        List<Integer> rexInputRefs = new LinkedList<>();
        for (int index = 0; index < rexNodes.size(); index++) {
            RexNode exp = rexNodes.get(index);
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
