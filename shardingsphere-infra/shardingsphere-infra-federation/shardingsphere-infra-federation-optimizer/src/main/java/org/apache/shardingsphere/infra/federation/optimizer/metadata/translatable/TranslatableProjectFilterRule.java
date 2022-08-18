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
import org.apache.calcite.rel.core.RelFactories;
import org.apache.calcite.rel.logical.LogicalFilter;
import org.apache.calcite.rel.logical.LogicalProject;
import org.apache.calcite.rex.RexInputRef;
import org.apache.calcite.rex.RexNode;
import org.apache.calcite.tools.RelBuilderFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Planner rule for pushing projections and filters into table scan.
 */
public class TranslatableProjectFilterRule extends RelOptRule {
    
    public static final TranslatableProjectFilterRule INSTANCE =
            new TranslatableProjectFilterRule(RelFactories.LOGICAL_BUILDER);
    
    /**
     * Creates a TranslatableProjectFilterRule.
     *
     * @param relBuilderFactory Builder for relational expressions
     */
    public TranslatableProjectFilterRule(final RelBuilderFactory relBuilderFactory) {
        super(
                operand(LogicalProject.class,
                        operand(LogicalFilter.class,
                                operand(TranslatableTableScan.class, none()))),
                relBuilderFactory,
                "TranslatableProjectFilterRule");
    }
    
    @Override
    public void onMatch(final RelOptRuleCall call) {
        LogicalProject project = call.rel(0);
        LogicalFilter filter = call.rel(1);
        TranslatableTableScan scan = call.rel(2);
        RexNode filterNode = filter.getCondition();
        List filters = new ArrayList();
        filters.add(filterNode);
        int[] fields = getProjectFields(project.getProjects());
        if (fields == null) {
            return;
        }
        call.transformTo(
                new TranslatableTableScan(
                        scan.getCluster(),
                        scan.getTable(),
                        scan.getTranslatableTable(),
                        filters,
                        fields));
    }
    
    private int[] getProjectFields(final List<RexNode> exps) {
        final int[] result = new int[exps.size()];
        for (int i = 0; i < exps.size(); i++) {
            final RexNode exp = exps.get(i);
            if (exp instanceof RexInputRef) {
                result[i] = ((RexInputRef) exp).getIndex();
            } else {
                return null;
            }
        }
        return result;
    }
}
