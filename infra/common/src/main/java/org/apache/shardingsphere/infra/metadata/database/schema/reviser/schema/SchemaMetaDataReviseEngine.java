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

package org.apache.shardingsphere.infra.metadata.database.schema.reviser.schema;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.database.connector.core.metadata.data.model.SchemaMetaData;
import org.apache.shardingsphere.database.connector.core.metadata.data.model.TableMetaData;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.metadata.database.schema.reviser.MetaDataReviseEntry;
import org.apache.shardingsphere.infra.metadata.database.schema.reviser.table.TableMetaDataReviseEngine;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.spi.type.ordered.OrderedSPILoader;

import java.util.Collection;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Schema meta data revise engine.
 */
@RequiredArgsConstructor
public final class SchemaMetaDataReviseEngine {
    
    private final Collection<ShardingSphereRule> rules;
    
    private final ConfigurationProperties props;
    
    /**
     * Revise schema meta data.
     *
     * @param originalMetaData original schema meta data
     * @return revised schema meta data
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    public SchemaMetaData revise(final SchemaMetaData originalMetaData) {
        SchemaMetaData result = originalMetaData;
        for (Entry<ShardingSphereRule, MetaDataReviseEntry> entry : OrderedSPILoader.getServices(MetaDataReviseEntry.class, rules).entrySet()) {
            result = revise(result, entry.getKey(), entry.getValue());
        }
        return result;
    }
    
    private <T extends ShardingSphereRule> SchemaMetaData revise(final SchemaMetaData originalMetaData, final T rule, final MetaDataReviseEntry<T> reviseEntry) {
        TableMetaDataReviseEngine<T> tableMetaDataReviseEngine = new TableMetaDataReviseEngine<>(rule, reviseEntry);
        Optional<? extends SchemaTableAggregationReviser<T>> aggregationReviser = reviseEntry.getSchemaTableAggregationReviser(props);
        if (!aggregationReviser.isPresent()) {
            return new SchemaMetaData(originalMetaData.getName(), originalMetaData.getTables().stream().map(tableMetaDataReviseEngine::revise).collect(Collectors.toList()));
        }
        for (TableMetaData each : originalMetaData.getTables()) {
            aggregationReviser.get().add(tableMetaDataReviseEngine.revise(each));
        }
        return new SchemaMetaData(originalMetaData.getName(), aggregationReviser.get().aggregate(rule));
    }
}
