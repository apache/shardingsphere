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
import org.apache.calcite.rel.RelNode;
import org.apache.calcite.rel.core.Sort;
import org.apache.calcite.rel.logical.LogicalSort;
import org.apache.shardingsphere.infra.optimizer.planner.ShardingSphereConvention;
import org.apache.shardingsphere.infra.optimizer.rel.physical.SSLimitSort;

public final class SSLimitSortConverterRule extends RelRule<SSLimitSortConverterRule.Config> {
    
    protected SSLimitSortConverterRule(final Config config) {
        super(config);
    }
    
    @Override
    public void onMatch(final RelOptRuleCall call) {
        final LogicalSort sort = call.rel(0);
        RelNode input = sort.getInput();
        final Sort o = SSLimitSort.create(
                convert(input, input.getTraitSet().replace(ShardingSphereConvention.INSTANCE)),
                sort.getCollation(),
                sort.offset, sort.fetch
        );
    
        call.transformTo(o);
    }
    
    public interface Config extends RelRule.Config {
        SSLimitSortConverterRule.Config DEFAULT = EMPTY.withOperandSupplier(
            b0 -> b0.operand(LogicalSort.class).predicate(sort -> sort.fetch != null).anyInputs())
                .as(SSLimitSortConverterRule.Config.class);
        
        @Override default SSLimitSortConverterRule toRule() {
            return new SSLimitSortConverterRule(this);
        }
    }
}
