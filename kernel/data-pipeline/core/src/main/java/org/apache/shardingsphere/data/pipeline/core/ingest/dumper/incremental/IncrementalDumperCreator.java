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

package org.apache.shardingsphere.data.pipeline.core.ingest.dumper.incremental;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.data.pipeline.api.type.StandardPipelineDataSourceConfiguration;
import org.apache.shardingsphere.data.pipeline.core.channel.PipelineChannel;
import org.apache.shardingsphere.data.pipeline.core.metadata.loader.PipelineTableMetaDataLoader;
import org.apache.shardingsphere.infra.database.core.spi.DatabaseTypedSPILoader;
import org.apache.shardingsphere.infra.exception.core.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.exception.generic.UnsupportedSQLOperationException;

/**
 * Incremental dumper creator.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class IncrementalDumperCreator {
    
    /**
     * Create incremental dumper.
     *
     * @param dumperContext incremental dumper context
     * @param channel channel
     * @param metaDataLoader meta data loader
     * @return incremental dumper
     */
    public static IncrementalDumper create(final IncrementalDumperContext dumperContext, final PipelineChannel channel, final PipelineTableMetaDataLoader metaDataLoader) {
        ShardingSpherePreconditions.checkState(dumperContext.getCommonContext().getDataSourceConfig() instanceof StandardPipelineDataSourceConfiguration,
                () -> new UnsupportedSQLOperationException("Incremental dumper only support StandardPipelineDataSourceConfiguration"));
        return DatabaseTypedSPILoader.getService(DialectIncrementalDumperCreator.class, dumperContext.getCommonContext().getDataSourceConfig().getDatabaseType())
                .createIncrementalDumper(dumperContext, dumperContext.getCommonContext().getPosition(), channel, metaDataLoader);
    }
}
