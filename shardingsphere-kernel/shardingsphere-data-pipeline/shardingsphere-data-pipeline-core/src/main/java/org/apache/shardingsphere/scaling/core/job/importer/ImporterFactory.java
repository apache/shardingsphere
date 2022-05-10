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

package org.apache.shardingsphere.scaling.core.job.importer;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import org.apache.shardingsphere.data.pipeline.api.config.rulealtered.ImporterConfiguration;
import org.apache.shardingsphere.data.pipeline.api.ingest.channel.PipelineChannel;
import org.apache.shardingsphere.data.pipeline.core.datasource.PipelineDataSourceManager;
import org.apache.shardingsphere.data.pipeline.spi.importer.Importer;
import org.apache.shardingsphere.scaling.core.spi.ScalingEntry;
import org.apache.shardingsphere.scaling.core.spi.ScalingEntryFactory;

import java.lang.reflect.Constructor;

/**
 * Importer factory.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ImporterFactory {
    
    /**
     * Create importer.
     *
     * @param importerConfig importer configuration
     * @param dataSourceManager data source manager
     * @param channel channel
     * @return importer
     */
    @SneakyThrows(ReflectiveOperationException.class)
    public static Importer createImporter(final ImporterConfiguration importerConfig, final PipelineDataSourceManager dataSourceManager, final PipelineChannel channel) {
        String databaseType = importerConfig.getDataSourceConfig().getDatabaseType().getType();
        ScalingEntry scalingEntry = ScalingEntryFactory.getInstance(databaseType);
        Constructor<? extends Importer> constructor = scalingEntry.getImporterClass().getConstructor(ImporterConfiguration.class, PipelineDataSourceManager.class, PipelineChannel.class);
        return constructor.newInstance(importerConfig, dataSourceManager, channel);
    }
}
