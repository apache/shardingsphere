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

package org.apache.shardingsphere.scaling.core.execute.executor.importer;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import org.apache.shardingsphere.scaling.core.config.ImporterConfiguration;
import org.apache.shardingsphere.scaling.core.datasource.DataSourceManager;
import org.apache.shardingsphere.scaling.core.spi.ScalingEntry;
import org.apache.shardingsphere.scaling.core.spi.ScalingEntryLoader;

/**
 * Importer factory.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ImporterFactory {
    
    /**
     * New instance of importer.
     *
     * @param importerConfig rdbms configuration
     * @param dataSourceManager data source factory
     * @return importer
     */
    public static Importer newInstance(final ImporterConfiguration importerConfig, final DataSourceManager dataSourceManager) {
        return newInstance(importerConfig.getDataSourceConfig().getDatabaseType().getName(), importerConfig, dataSourceManager);
    }
    
    /**
     * New instance of importer.
     *
     * @param databaseType database type
     * @param importerConfig rdbms configuration
     * @param dataSourceManager data source factory
     * @return importer
     */
    @SneakyThrows(ReflectiveOperationException.class)
    public static Importer newInstance(final String databaseType, final ImporterConfiguration importerConfig, final DataSourceManager dataSourceManager) {
        ScalingEntry scalingEntry = ScalingEntryLoader.getScalingEntryByDatabaseType(databaseType);
        return scalingEntry.getImporterClass().getConstructor(ImporterConfiguration.class, DataSourceManager.class).newInstance(importerConfig, dataSourceManager);
    }
}
