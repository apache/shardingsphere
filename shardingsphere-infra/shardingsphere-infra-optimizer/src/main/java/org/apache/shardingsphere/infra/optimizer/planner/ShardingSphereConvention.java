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

package org.apache.shardingsphere.infra.optimizer.planner;

import org.apache.calcite.plan.Convention;
import org.apache.calcite.plan.ConventionTraitDef;
import org.apache.calcite.plan.RelOptPlanner;
import org.apache.calcite.plan.RelTrait;
import org.apache.calcite.plan.RelTraitDef;
import org.apache.shardingsphere.infra.optimizer.rel.physical.SSRel;

/**
 * Family of calling conventions that return results as an
 *  {@link SSRel}.
 */
public enum ShardingSphereConvention implements Convention {

    INSTANCE;

    @Override
    public Class getInterface() {
        return SSRel.class;
    }

    @Override
    public String getName() {
        return this.getClass().getName();
    }

    @Override
    public RelTraitDef getTraitDef() {
        return ConventionTraitDef.INSTANCE;
    }

    @Override
    public boolean satisfies(final RelTrait trait) {
        return this == trait;
    }

    @Override
    public void register(final RelOptPlanner planner) {
        PlannerRules.SHARDING_CONVERTER_RULES.forEach(planner::addRule);
    }
}
