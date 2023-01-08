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

package org.apache.shardingsphere.data.pipeline.cdc.core.importer;

import org.apache.shardingsphere.data.pipeline.api.config.ImporterConfiguration;
import org.apache.shardingsphere.data.pipeline.api.importer.Importer;
import org.apache.shardingsphere.data.pipeline.api.ingest.channel.PipelineChannel;
import org.apache.shardingsphere.data.pipeline.api.job.progress.listener.PipelineJobProgressListener;
import org.apache.shardingsphere.data.pipeline.spi.importer.ImporterCreator;
import org.apache.shardingsphere.data.pipeline.spi.importer.ImporterType;
import org.apache.shardingsphere.data.pipeline.spi.importer.connector.ImporterConnector;

/**
 * CDC importer creator.
 */
public final class CDCImporterCreator implements ImporterCreator {
    
    @Override
    public Importer createImporter(final ImporterConfiguration importerConfig, final ImporterConnector importerConnector, final PipelineChannel channel,
                                   final PipelineJobProgressListener jobProgressListener, final ImporterType importerType) {
        return new CDCImporter(importerConfig, importerConnector, channel, jobProgressListener, importerType);
    }
    
    @Override
    public String getType() {
        return "CDC";
    }
}
