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

package org.apache.shardingsphere.test.integration.framework.container.compose;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Cluster tinker.
 *
 * @deprecated remove me when integration tests support all scenarios
 */
@Deprecated
public final class ClusterTinker {
    
    private static final Set<String> CLUSTER_SCENARIOS;
    
    static {
        CLUSTER_SCENARIOS = new LinkedHashSet<>();
        CLUSTER_SCENARIOS.add("empty_rules");
    }
    
    /**
     * Is support cluster.
     *
     * @param scenario scenario
     * @return is supported or not
     */
    public static boolean isSupportCluster(final String scenario) {
        return CLUSTER_SCENARIOS.contains(scenario);
    }
}
