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

package org.apache.shardingsphere.infra.metadata.database.schema.decorator.reviser.schema;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.metadata.database.schema.decorator.reviser.table.TableMetaDataReviseEngine;
import org.apache.shardingsphere.infra.metadata.database.schema.decorator.spi.MetaDataReviseEntry;
import org.apache.shardingsphere.infra.metadata.database.schema.loader.model.SchemaMetaData;
import org.apache.shardingsphere.infra.metadata.database.schema.loader.model.TableMetaData;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.util.spi.type.typed.TypedSPILoader;

import javax.sql.DataSource;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Schema meta data revise engine.
 *
 * @param <T> type of rule
 */
@RequiredArgsConstructor
public final class SchemaMetaDataReviseEngine<T extends ShardingSphereRule> {
    
    private final T rule;
    
    private final ConfigurationProperties props;
    
    private final DatabaseType databaseType;
    
    private final DataSource dataSource;
    
    /**
     * Revise schema meta data.
     *
     * @param originalMetaData original schema meta data
     * @return revised schema data
     */
    @SuppressWarnings("unchecked")
    public SchemaMetaData revise(final SchemaMetaData originalMetaData) {
        @SuppressWarnings("rawtypes")
        Optional<MetaDataReviseEntry> reviseEntry = TypedSPILoader.findService(MetaDataReviseEntry.class, rule.getClass().getSimpleName());
        if (!reviseEntry.isPresent()) {
            return originalMetaData;
        }
        @SuppressWarnings("rawtypes")
        TableMetaDataReviseEngine<T> tableMetaDataReviseEngine = new TableMetaDataReviseEngine<>(rule, databaseType, dataSource, reviseEntry.get());
        Optional<? extends SchemaTableAggregationReviser<T>> aggregationReviser = reviseEntry.get().getSchemaTableAggregationReviser(rule, props);
        if (!aggregationReviser.isPresent()) {
            return new SchemaMetaData(originalMetaData.getName(), originalMetaData.getTables().stream().map(tableMetaDataReviseEngine::revise).collect(Collectors.toList()));
        }
        for (TableMetaData each : originalMetaData.getTables()) {
            aggregationReviser.get().add(tableMetaDataReviseEngine.revise(each));
        }
        return new SchemaMetaData(originalMetaData.getName(), aggregationReviser.get().aggregate(rule));
    }
}
