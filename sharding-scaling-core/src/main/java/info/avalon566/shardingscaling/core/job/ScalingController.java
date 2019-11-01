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

package info.avalon566.shardingscaling.core.job;

import info.avalon566.shardingscaling.core.config.SyncConfiguration;

import java.util.ArrayList;
import java.util.List;

/**
 * Scaling controller.
 *
 * @author avalon566
 */
public class ScalingController {

    private final List<SyncConfiguration> syncConfigurations;

    private final List<DataNodeMigrateController> dataNodeMigrateControllers;

    public ScalingController(final List<SyncConfiguration> syncConfigurations) {
        this.syncConfigurations = syncConfigurations;
        this.dataNodeMigrateControllers = new ArrayList<>(this.syncConfigurations.size());
    }

    /**
     * Start data nodes migrate.
     */
    public void start() {
        for (SyncConfiguration syncConfiguration : syncConfigurations) {
            DataNodeMigrateController dataNodeMigrateController = new DataNodeMigrateController(syncConfiguration);
            dataNodeMigrateController.start();
            dataNodeMigrateControllers.add(dataNodeMigrateController);
        }
    }

    /**
     * Stop data nodes migrate.
     */
    public void stop() {
        for (DataNodeMigrateController dataNodeMigrateController : dataNodeMigrateControllers) {
            dataNodeMigrateController.stop();
        }
    }

    /**
     * Get data nodes migrate progresses.
     *
     * @return data nodes migrate progress
     */
    public List<MigrateProgress> getProgresses() {
        List<MigrateProgress> result = new ArrayList<>(this.syncConfigurations.size());
        for (DataNodeMigrateController dataNodeMigrateController : dataNodeMigrateControllers) {
            result.add(dataNodeMigrateController.getProgress());
        }
        return result;
    }
}
