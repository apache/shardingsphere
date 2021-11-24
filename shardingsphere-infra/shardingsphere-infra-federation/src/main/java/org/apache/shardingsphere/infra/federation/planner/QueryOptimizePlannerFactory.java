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

package org.apache.shardingsphere.infra.federation.planner;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.calcite.plan.ConventionTraitDef;
import org.apache.calcite.plan.RelOptPlanner;
import org.apache.calcite.plan.RelOptUtil;
import org.apache.calcite.plan.volcano.VolcanoPlanner;
import org.apache.calcite.rel.RelCollationTraitDef;

/**
 * Query optimize planner factory.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class QueryOptimizePlannerFactory {
    
    /**
     * Create new instance of query optimize planner.
     *
     * @return new instance of query optimize planner
     */
    public static RelOptPlanner newInstance() {
        RelOptPlanner result = createPlanner();
        setUpRules(result);
        return result;
    }
    
    private static RelOptPlanner createPlanner() {
        return new VolcanoPlanner();
    }
    
    private static void setUpRules(final RelOptPlanner planner) {
        planner.addRelTraitDef(ConventionTraitDef.INSTANCE);
        planner.addRelTraitDef(RelCollationTraitDef.INSTANCE);
        RelOptUtil.registerDefaultRules(planner, false, true);
    }
}
