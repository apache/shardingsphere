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

import org.apache.shardingsphere.data.pipeline.api.config.rulealtered.ImporterConfiguration;
import org.apache.shardingsphere.data.pipeline.api.ingest.channel.PipelineChannel;
import org.apache.shardingsphere.data.pipeline.api.job.progress.listener.PipelineJobProgressListener;
import org.apache.shardingsphere.data.pipeline.core.datasource.PipelineDataSourceManager;
import org.apache.shardingsphere.data.pipeline.spi.importer.Importer;
import org.apache.shardingsphere.infra.util.spi.annotation.SingletonSPI;
import org.apache.shardingsphere.infra.util.spi.type.typed.TypedSPI;

@SingletonSPI
public interface ImporterCreator extends TypedSPI {
    
    /**
     * Create importer.
     * @param importerConfig importerConfig
     * @param dataSourceManager dataSourceManager
     * @param channel channel
     * @param jobProgressListener jobProgressListener
     * @return importer
     */
    Importer createImporter(ImporterConfiguration importerConfig, PipelineDataSourceManager dataSourceManager, PipelineChannel channel,
                            PipelineJobProgressListener jobProgressListener);
}
