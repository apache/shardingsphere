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

package org.apache.shardingsphere.scaling.core.job.check;

import lombok.SneakyThrows;
import org.apache.shardingsphere.scaling.core.job.JobContext;
import org.apache.shardingsphere.scaling.core.job.check.consistency.DataConsistencyChecker;
import org.apache.shardingsphere.scaling.core.job.check.source.DataSourceChecker;
import org.apache.shardingsphere.scaling.core.spi.ScalingEntry;
import org.apache.shardingsphere.scaling.core.spi.ScalingEntryLoader;

/**
 * Environment checker factory.
 */
public final class EnvironmentCheckerFactory {
    
    
    /**
     * Create data consistency checker instance.
     *
     * @param jobContext job context
     * @return data consistency checker
     */
    @SneakyThrows(ReflectiveOperationException.class)
    public static DataConsistencyChecker newInstance(final JobContext jobContext) {
        ScalingEntry scalingEntry = ScalingEntryLoader.getInstance(jobContext.getJobConfig().getHandleConfig().getDatabaseType());
        return scalingEntry.getEnvironmentCheckerClass().getConstructor().newInstance().getDataConsistencyCheckerClass().getConstructor(JobContext.class).newInstance(jobContext);
    }
    
    /**
     * Create data source checker instance.
     *
     * @param databaseType database type
     * @return data source checker
     */
    @SneakyThrows(ReflectiveOperationException.class)
    public static DataSourceChecker newInstance(final String databaseType) {
        ScalingEntry scalingEntry = ScalingEntryLoader.getInstance(databaseType);
        return scalingEntry.getEnvironmentCheckerClass().getConstructor().newInstance().getDataSourceCheckerClass().getConstructor().newInstance();
    }
}
