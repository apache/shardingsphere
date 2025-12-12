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
import org.apache.shardingsphere.database.connector.core.spi.DatabaseTypedSPILoader;
import org.apache.shardingsphere.infra.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.exception.generic.UnsupportedSQLOperationException;

/**
 * Incremental dumper creator.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class IncrementalDumperCreator {
    
    /**
     * Create incremental dumper.
     *
     * @param param create incremental dumper parameter
     * @return incremental dumper
     */
    public static IncrementalDumper create(final CreateIncrementalDumperParameter param) {
        ShardingSpherePreconditions.checkState(param.getContext().getCommonContext().getDataSourceConfig() instanceof StandardPipelineDataSourceConfiguration,
                () -> new UnsupportedSQLOperationException("Incremental dumper only support StandardPipelineDataSourceConfiguration"));
        return DatabaseTypedSPILoader.getService(DialectIncrementalDumperCreator.class, param.getContext().getCommonContext().getDataSourceConfig().getDatabaseType()).create(param);
    }
}
