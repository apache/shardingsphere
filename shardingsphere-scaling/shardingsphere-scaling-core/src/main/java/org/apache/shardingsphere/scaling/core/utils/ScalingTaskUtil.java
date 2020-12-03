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

package org.apache.shardingsphere.scaling.core.utils;

import org.apache.shardingsphere.scaling.core.constant.ScalingConstant;
import org.apache.shardingsphere.scaling.core.job.position.FinishedPosition;
import org.apache.shardingsphere.scaling.core.job.task.ScalingTask;
import org.apache.shardingsphere.scaling.core.job.task.inventory.InventoryTask;

import java.util.List;

/**
 * Scaling task util.
 */
public final class ScalingTaskUtil {
    
    /**
     * All inventory tasks is finished.
     *
     * @param inventoryTasks to check inventory tasks
     * @return is finished
     */
    public static boolean allInventoryTasksFinished(final List<ScalingTask> inventoryTasks) {
        return inventoryTasks.stream().allMatch(each -> ((InventoryTask) each).getPositionManager().getPosition() instanceof FinishedPosition);
    }
    
    /**
     * Get scaling listener path.
     *
     * @param paths sub paths.
     * @return path.
     */
    public static String getScalingListenerPath(final Object... paths) {
        StringBuilder result = new StringBuilder(ScalingConstant.SCALING_LISTENER_PATH);
        for (Object each : paths) {
            result.append("/").append(each);
        }
        return result.toString();
    }
}
