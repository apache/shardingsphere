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

package org.apache.shardingsphere.sqlfederation.optimizer.context.planner;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.sqlfederation.optimizer.util.SQLFederationPlannerUtil;

import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Optimizer planner context factory.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class OptimizerPlannerContextFactory {
    
    /**
     * Create optimizer planner context map.
     *
     * @param databases databases
     * @return created optimizer planner context map
     */
    public static Map<String, OptimizerPlannerContext> create(final Map<String, ShardingSphereDatabase> databases) {
        Map<String, OptimizerPlannerContext> result = new ConcurrentHashMap<>(databases.size(), 1);
        for (Entry<String, ShardingSphereDatabase> entry : databases.entrySet()) {
            result.put(entry.getKey(), new OptimizerPlannerContext(SQLFederationPlannerUtil.createHepPlanner()));
        }
        return result;
    }
}
