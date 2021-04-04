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

import com.google.common.collect.ImmutableList;
import lombok.extern.slf4j.Slf4j;
import org.apache.calcite.plan.RelOptCostImpl;
import org.apache.calcite.plan.hep.HepPlanner;
import org.apache.calcite.plan.hep.HepProgram;
import org.apache.calcite.plan.hep.HepProgramBuilder;
import org.apache.calcite.rel.RelNode;
import org.apache.shardingsphere.infra.optimizer.planner.PlannerRules.HepRules;
import org.apache.shardingsphere.infra.optimizer.rel.CustomLogicalRelConverter;

@Slf4j
public abstract class AbstractPlanner implements Planner {
    
    /**
     * Rewrite(optimize) relational operator(sql) base on RBO rules.
     * @param relnode rational operator
     * @return rewrited rational operator 
     */
    protected RelNode rewrite(final RelNode relnode) {
    
        RelNode logicalRelNode = CustomLogicalRelConverter.convert(relnode);
        
        HepProgramBuilder hepProgramBuilder = HepProgram.builder();
        for (HepRules hepRules : HepRules.values()) {
            hepProgramBuilder.addMatchOrder(hepRules.getMatchOrder());
            hepProgramBuilder.addGroupBegin();
            hepProgramBuilder.addRuleCollection(ImmutableList.copyOf(hepRules.getRules()));
            hepProgramBuilder.addGroupEnd();
        }
        HepPlanner hepPlanner = new HepPlanner(hepProgramBuilder.build(), null, true, null, RelOptCostImpl.FACTORY);
        hepPlanner.setRoot(logicalRelNode);
        RelNode rewritedRelNode = hepPlanner.findBestExp();
        log("rewrited plan ", rewritedRelNode);
        return rewritedRelNode;
    }
    
    protected abstract void log(String desc, RelNode relNode);
}
