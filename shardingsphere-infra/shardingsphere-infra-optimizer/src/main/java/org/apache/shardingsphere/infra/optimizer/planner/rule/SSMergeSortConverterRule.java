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
import org.apache.shardingsphere.infra.optimizer.planner.ShardingSphereConvention;
import org.apache.shardingsphere.infra.optimizer.rel.logical.LogicalMergeSort;
import org.apache.shardingsphere.infra.optimizer.rel.physical.SSMergeSort;

public final class SSMergeSortConverterRule extends ConverterRule {
    
    public static final Config DEFAULT_CONFIG = Config.INSTANCE
            .withConversion(LogicalMergeSort.class, Convention.NONE,
                    ShardingSphereConvention.INSTANCE, SSMergeSortConverterRule.class.getName())
            .withRuleFactory(SSMergeSortConverterRule::new);
    
    protected SSMergeSortConverterRule(final Config config) {
        super(config);
    }
    
    @Override
    public RelNode convert(final RelNode rel) {
        LogicalMergeSort mergeSort = (LogicalMergeSort) rel;
        return SSMergeSort.create(mergeSort.getTraitSet(), convert(mergeSort.getInput(), out), 
                mergeSort.getCollation(), mergeSort.offset, mergeSort.fetch);
    }
}
