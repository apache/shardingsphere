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

package org.apache.shardingsphere.schedule.core.job.statistics.collect.listener;

import org.apache.shardingsphere.mode.manager.listener.StatisticsCollectJobCronUpdateListener;
import org.apache.shardingsphere.schedule.core.job.statistics.collect.StatisticsCollectJobWorker;

/**
 * Statistics collect schedule job cron update listener.
 */
public final class StatisticsCollectScheduleJobCronUpdateListener implements StatisticsCollectJobCronUpdateListener {
    
    @Override
    public void updated() {
        try {
            new StatisticsCollectJobWorker().updateJobConfiguration();
            // CHECKSTYLE:OFF
        } catch (final Exception ignored) {
            // CHECKSTYLE:ON
        }
    }
}
