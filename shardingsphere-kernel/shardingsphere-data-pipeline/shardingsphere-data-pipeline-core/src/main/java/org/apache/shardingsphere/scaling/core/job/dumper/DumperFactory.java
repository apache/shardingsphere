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

package org.apache.shardingsphere.scaling.core.job.dumper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import org.apache.shardingsphere.data.pipeline.api.config.ingest.DumperConfiguration;
import org.apache.shardingsphere.data.pipeline.api.ingest.channel.PipelineChannel;
import org.apache.shardingsphere.data.pipeline.api.ingest.position.IngestPosition;
import org.apache.shardingsphere.data.pipeline.core.metadata.loader.PipelineTableMetaDataLoader;
import org.apache.shardingsphere.data.pipeline.spi.ingest.dumper.IncrementalDumper;
import org.apache.shardingsphere.scaling.core.spi.ScalingEntry;
import org.apache.shardingsphere.scaling.core.spi.ScalingEntryFactory;

import java.lang.reflect.Constructor;

/**
 * Dumper factory.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DumperFactory {
    
    /**
     * Create incremental dumper.
     *
     * @param dumperConfig dumper configuration
     * @param position position
     * @param channel channel
     * @param sourceMetaDataLoader metadata loader
     * @return incremental dumper
     */
    @SneakyThrows(ReflectiveOperationException.class)
    public static IncrementalDumper createIncrementalDumper(final DumperConfiguration dumperConfig, final IngestPosition<?> position,
                                                            final PipelineChannel channel, final PipelineTableMetaDataLoader sourceMetaDataLoader) {
        String databaseType = dumperConfig.getDataSourceConfig().getDatabaseType().getType();
        ScalingEntry scalingEntry = ScalingEntryFactory.getInstance(databaseType);
        Constructor<? extends IncrementalDumper> constructor = scalingEntry.getIncrementalDumperClass()
                .getConstructor(DumperConfiguration.class, IngestPosition.class, PipelineChannel.class, PipelineTableMetaDataLoader.class);
        return constructor.newInstance(dumperConfig, position, channel, sourceMetaDataLoader);
    }
}
