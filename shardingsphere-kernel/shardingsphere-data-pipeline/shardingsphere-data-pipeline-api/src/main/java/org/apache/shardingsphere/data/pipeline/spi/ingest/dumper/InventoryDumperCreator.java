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

package org.apache.shardingsphere.data.pipeline.spi.ingest.dumper;

import org.apache.shardingsphere.data.pipeline.api.config.ingest.InventoryDumperConfiguration;
import org.apache.shardingsphere.data.pipeline.api.ingest.channel.PipelineChannel;
import org.apache.shardingsphere.data.pipeline.api.ingest.dumper.InventoryDumper;
import org.apache.shardingsphere.data.pipeline.api.metadata.loader.PipelineTableMetaDataLoader;
import org.apache.shardingsphere.infra.util.spi.annotation.SingletonSPI;
import org.apache.shardingsphere.infra.util.spi.type.required.RequiredSPI;
import org.apache.shardingsphere.infra.util.spi.type.typed.TypedSPI;

import javax.sql.DataSource;

/**
 * Inventory dumper creator.
 */
@SingletonSPI
public interface InventoryDumperCreator extends TypedSPI, RequiredSPI {
    
    /**
     * Create inventory dumper.
     *
     * @param inventoryDumperConfig inventory dumper configuration
     * @param channel chanel
     * @param sourceDataSource source data source
     * @param sourceMetaDataLoader source meta data loader
     * @return inventory dumper
     */
    InventoryDumper createInventoryDumper(InventoryDumperConfiguration inventoryDumperConfig, PipelineChannel channel,
                                          DataSource sourceDataSource, PipelineTableMetaDataLoader sourceMetaDataLoader);
}
