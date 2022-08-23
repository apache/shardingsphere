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
import org.apache.calcite.tools.RelBuilderFactory;

import java.util.Collections;

/**
 * Planner rule for pushing filters into table scan.
 */
public class TranslatableFilterRule extends RelOptRule {
    
    public static final TranslatableFilterRule INSTANCE = new TranslatableFilterRule(RelFactories.LOGICAL_BUILDER);
    
    public TranslatableFilterRule(final RelBuilderFactory relBuilderFactory) {
        super(operand(LogicalFilter.class, operand(TranslatableTableScan.class, none())), relBuilderFactory, "TranslatableFilterRule");
    }
    
    @Override
    public void onMatch(final RelOptRuleCall call) {
        LogicalFilter filter = call.rel(0);
        TranslatableTableScan scan = call.rel(1);
        call.transformTo(new TranslatableTableScan(scan.getCluster(), scan.getTable(), scan.getTranslatableTable(), Collections.singletonList(filter.getCondition()), scan.getFields()));
    }
}
