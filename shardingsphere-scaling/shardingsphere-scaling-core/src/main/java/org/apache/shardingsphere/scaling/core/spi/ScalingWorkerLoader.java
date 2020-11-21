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

package org.apache.shardingsphere.scaling.core.spi;

import org.apache.shardingsphere.governance.core.yaml.config.YamlGovernanceConfiguration;
import org.apache.shardingsphere.governance.core.yaml.swapper.GovernanceConfigurationYamlSwapper;
import org.apache.shardingsphere.governance.repository.api.config.GovernanceConfiguration;
import org.apache.shardingsphere.infra.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.scaling.core.config.ScalingContext;

import java.util.Collection;
import java.util.Optional;

/**
 * Scaling worker loader.
 */
public final class ScalingWorkerLoader {
    
    /**
     * Init scaling worker.
     *
     * @return worker type
     */
    public static Optional<String> initScalingWorker() {
        ShardingSphereServiceLoader.register(ScalingWorker.class);
        YamlGovernanceConfiguration distributedScalingService = ScalingContext.getInstance().getServerConfig().getDistributedScalingService();
        if (null != distributedScalingService) {
            GovernanceConfiguration governanceConfiguration = new GovernanceConfigurationYamlSwapper().swapToObject(distributedScalingService);
            Collection<ScalingWorker> scalingWorkers = ShardingSphereServiceLoader.newServiceInstances(ScalingWorker.class);
            for (ScalingWorker each : scalingWorkers) {
                each.init(governanceConfiguration);
                return Optional.of(each.getType());
            }
        }
        return Optional.empty();
    }
}
