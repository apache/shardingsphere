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

package org.apache.shardingsphere.scaling.core.job.preparer.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.scaling.core.job.task.ScalingTask;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * Scaling job prepare util.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class JobPrepareUtil {
    
    /**
     * Group inventory data tasks by task number.
     *
     * @param taskNumber task number
     * @param allInventoryDataTasks all inventory data tasks
     * @return task group list
     */
    public static List<Collection<ScalingTask>> groupInventoryDataTasks(final int taskNumber, final List<ScalingTask> allInventoryDataTasks) {
        List<Collection<ScalingTask>> result = new ArrayList<>(taskNumber);
        for (int i = 0; i < taskNumber; i++) {
            result.add(new LinkedList<>());
        }
        for (int i = 0; i < allInventoryDataTasks.size(); i++) {
            result.get(i % taskNumber).add(allInventoryDataTasks.get(i));
        }
        return result;
    }
}
