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

package org.apache.shardingsphere.data.pipeline.core.sqlbuilder;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.data.pipeline.spi.sqlbuilder.PipelineSQLBuilder;
import org.apache.shardingsphere.spi.exception.ServiceProviderNotFoundException;
import org.apache.shardingsphere.spi.singleton.TypedSingletonSPIHolder;

/**
 * Pipeline SQL builder factory.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class PipelineSQLBuilderFactory {
    
    private static final TypedSingletonSPIHolder<PipelineSQLBuilder> SQL_BUILDER_SPI_HOLDER = new TypedSingletonSPIHolder<>(PipelineSQLBuilder.class, false);
    
    /**
     * Get SQL builder instance.
     *
     * @param databaseType database type
     * @return SQL builder
     */
    public static PipelineSQLBuilder getSQLBuilder(final String databaseType) {
        return SQL_BUILDER_SPI_HOLDER.get(databaseType).orElseThrow(() -> new ServiceProviderNotFoundException(PipelineSQLBuilder.class, databaseType));
    }
}
