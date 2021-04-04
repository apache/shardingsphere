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
import org.apache.calcite.rel.logical.LogicalSort;
import org.apache.shardingsphere.infra.optimizer.planner.ShardingSphereConvention;
import org.apache.shardingsphere.infra.optimizer.rel.physical.SSSort;

public class SSSortConverterRule extends ConverterRule {
    
    public static final Config DEFAULT_CONFIG = Config.INSTANCE
            .withConversion(LogicalSort.class, Convention.NONE,
                    ShardingSphereConvention.INSTANCE, SSSortConverterRule.class.getName())
            .withRuleFactory(SSSortConverterRule::new);
    
    protected SSSortConverterRule(final Config config) {
        super(config);
    }
    
    @Override
    public final RelNode convert(final RelNode rel) {
        LogicalSort sort = (LogicalSort) rel;
        if (sort.fetch != null || sort.offset != null) {
            return null;
        }
        return SSSort.create(convert(sort.getInput(), out), sort.getCollation());
    }
}
