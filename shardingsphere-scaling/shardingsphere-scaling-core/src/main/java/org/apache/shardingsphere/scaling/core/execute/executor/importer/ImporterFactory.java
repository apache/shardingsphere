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

import lombok.SneakyThrows;
import org.apache.shardingsphere.scaling.core.config.RdbmsConfiguration;
import org.apache.shardingsphere.scaling.core.spi.ScalingEntry;
import org.apache.shardingsphere.scaling.core.spi.ScalingEntryLoader;
import org.apache.shardingsphere.scaling.core.datasource.DataSourceManager;

/**
 * Importer factory.
 */
public final class ImporterFactory {
    
    /**
     * New instance of importer.
     *
     * @param rdbmsConfiguration rdbms configuration
     * @param dataSourceManager data source factory
     * @return importer
     */
    public static Importer newInstance(final RdbmsConfiguration rdbmsConfiguration, final DataSourceManager dataSourceManager) {
        return newInstance(rdbmsConfiguration.getDataSourceConfiguration().getDatabaseType().getName(), rdbmsConfiguration, dataSourceManager);
    }
    
    /**
     * New instance of importer.
     *
     * @param databaseType database type
     * @param rdbmsConfiguration rdbms configuration
     * @param dataSourceManager data source factory
     * @return importer
     */
    @SneakyThrows
    public static Importer newInstance(final String databaseType, final RdbmsConfiguration rdbmsConfiguration, final DataSourceManager dataSourceManager) {
        ScalingEntry scalingEntry = ScalingEntryLoader.getScalingEntryByDatabaseType(databaseType);
        return scalingEntry.getImporterClass().getConstructor(RdbmsConfiguration.class, DataSourceManager.class).newInstance(rdbmsConfiguration, dataSourceManager);
    }
}
