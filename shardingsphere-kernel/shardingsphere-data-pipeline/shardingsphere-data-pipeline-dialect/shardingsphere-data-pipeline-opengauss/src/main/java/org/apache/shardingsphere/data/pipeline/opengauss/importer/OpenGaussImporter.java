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

package org.apache.shardingsphere.data.pipeline.opengauss.importer;

import org.apache.shardingsphere.data.pipeline.api.config.rulealtered.ImporterConfiguration;
import org.apache.shardingsphere.data.pipeline.api.ingest.channel.PipelineChannel;
import org.apache.shardingsphere.data.pipeline.api.metadata.LogicTableName;
import org.apache.shardingsphere.data.pipeline.core.datasource.PipelineDataSourceManager;
import org.apache.shardingsphere.data.pipeline.core.importer.AbstractImporter;

/**
 * Importer of openGauss.
 */
public final class OpenGaussImporter extends AbstractImporter {
    
    public OpenGaussImporter(final ImporterConfiguration importerConfig, final PipelineDataSourceManager dataSourceManager, final PipelineChannel channel) {
        super(importerConfig, dataSourceManager, channel);
    }
    
    @Override
    protected String getSchemaName(final String logicTableName) {
        return getImporterConfig().getSchemaName(new LogicTableName(logicTableName));
    }
}
