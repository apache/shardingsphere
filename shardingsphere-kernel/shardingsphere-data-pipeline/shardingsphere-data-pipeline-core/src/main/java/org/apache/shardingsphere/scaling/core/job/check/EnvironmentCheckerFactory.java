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
import org.apache.shardingsphere.data.pipeline.core.prepare.datasource.DataSourcePreparer;
import org.apache.shardingsphere.scaling.core.spi.ScalingEntry;
import org.apache.shardingsphere.scaling.core.spi.ScalingEntryFactory;

import java.util.Optional;

/**
 * Environment checker factory.
 */
public final class EnvironmentCheckerFactory {
    
    /**
     * Create data source preparer instance.
     *
     * @param databaseType database type
     * @return data source preparer
     */
    @SneakyThrows(ReflectiveOperationException.class)
    public static Optional<DataSourcePreparer> getDataSourcePreparer(final String databaseType) {
        ScalingEntry scalingEntry = ScalingEntryFactory.getInstance(databaseType);
        Class<? extends DataSourcePreparer> preparerClass = scalingEntry.getEnvironmentCheckerClass().getConstructor().newInstance().getDataSourcePreparerClass();
        return null == preparerClass ? Optional.empty() : Optional.of(preparerClass.getConstructor().newInstance());
    }
}
