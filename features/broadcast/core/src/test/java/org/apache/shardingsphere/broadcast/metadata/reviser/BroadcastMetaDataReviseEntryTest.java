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

package org.apache.shardingsphere.broadcast.metadata.reviser;

import org.apache.shardingsphere.broadcast.metadata.reviser.schema.BroadcastSchemaTableAggregationReviser;
import org.apache.shardingsphere.broadcast.rule.BroadcastRule;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.config.props.ConfigurationPropertyKey;
import org.apache.shardingsphere.infra.metadata.database.schema.reviser.MetaDataReviseEntry;
import org.apache.shardingsphere.infra.spi.type.ordered.OrderedSPILoader;
import org.apache.shardingsphere.infra.util.props.PropertiesBuilder;
import org.apache.shardingsphere.infra.util.props.PropertiesBuilder.Property;
import org.junit.jupiter.api.Test;
import org.mockito.internal.configuration.plugins.Plugins;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertTrue;

class BroadcastMetaDataReviseEntryTest {
    
    private final BroadcastMetaDataReviseEntry reviseEntry = (BroadcastMetaDataReviseEntry) OrderedSPILoader.getServicesByClass(
            MetaDataReviseEntry.class, Collections.singleton(BroadcastRule.class)).get(BroadcastRule.class);
    
    @Test
    void assertGetSchemaTableAggregationReviser() throws ReflectiveOperationException {
        Optional<?> actual = reviseEntry.getSchemaTableAggregationReviser(
                new ConfigurationProperties(PropertiesBuilder.build(new Property(ConfigurationPropertyKey.CHECK_TABLE_METADATA_ENABLED.getKey(), Boolean.TRUE.toString()))));
        assertTrue(actual.isPresent());
        assertTrue((boolean) Plugins.getMemberAccessor().get(BroadcastSchemaTableAggregationReviser.class.getDeclaredField("checkTableMetaDataEnabled"), actual.get()));
    }
}
