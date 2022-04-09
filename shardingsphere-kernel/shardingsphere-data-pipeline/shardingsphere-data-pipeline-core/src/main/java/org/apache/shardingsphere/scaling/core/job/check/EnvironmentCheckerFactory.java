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
import org.apache.shardingsphere.data.pipeline.api.config.rulealtered.JobConfiguration;
import org.apache.shardingsphere.data.pipeline.core.check.consistency.DataConsistencyChecker;
import org.apache.shardingsphere.data.pipeline.core.check.consistency.DataConsistencyCheckerImpl;
import org.apache.shardingsphere.data.pipeline.core.prepare.datasource.DataSourcePreparer;
import org.apache.shardingsphere.scaling.core.spi.ScalingEntry;
import org.apache.shardingsphere.scaling.core.spi.ScalingEntryFactory;

/**
 * Environment checker factory.
 */
public final class EnvironmentCheckerFactory {
    
    /**
     * Create data consistency checker instance.
     *
     * @param jobConfig job configuration
     * @return data consistency checker
     */
    public static DataConsistencyChecker newInstance(final JobConfiguration jobConfig) {
        return new DataConsistencyCheckerImpl(jobConfig);
    }
    
    /**
     * Create data source preparer instance.
     *
     * @param databaseType database type
     * @return data source preparer
     */
    @SneakyThrows(ReflectiveOperationException.class)
    public static DataSourcePreparer getDataSourcePreparer(final String databaseType) {
        ScalingEntry scalingEntry = ScalingEntryFactory.getInstance(databaseType);
        Class<? extends DataSourcePreparer> preparerClass = scalingEntry.getEnvironmentCheckerClass().getConstructor().newInstance().getDataSourcePreparerClass();
        return null == preparerClass ? null : preparerClass.getConstructor().newInstance();
    }
}
