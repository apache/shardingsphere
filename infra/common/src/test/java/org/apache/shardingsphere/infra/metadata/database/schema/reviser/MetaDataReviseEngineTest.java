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

package org.apache.shardingsphere.infra.metadata.database.schema.reviser;

import org.apache.shardingsphere.database.connector.core.metadata.data.model.SchemaMetaData;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.metadata.database.schema.builder.GenericSchemaBuilderMaterial;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Map;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

class MetaDataReviseEngineTest {
    
    private final MetaDataReviseEngine engine = new MetaDataReviseEngine(Collections.emptyList(), mock(DatabaseType.class));
    
    @Test
    void assertRevise() {
        Map<String, ShardingSphereSchema> actual = engine.revise(Collections.singletonMap("foo_schema", new SchemaMetaData("foo_schema", Collections.emptyList())), createBuilderMaterial());
        assertThat(actual.size(), is(1));
        ShardingSphereSchema schema = actual.get("foo_schema");
        assertThat(schema.getName(), is("foo_schema"));
        assertTrue(schema.getAllTables().isEmpty());
        assertTrue(schema.getAllViews().isEmpty());
    }
    
    @Test
    void assertReviseWithEmptySchemaMetaDataMap() {
        Map<String, ShardingSphereSchema> actual = engine.revise(Collections.emptyMap(), createBuilderMaterial());
        assertThat(actual.size(), is(1));
        ShardingSphereSchema schema = actual.get("default_schema");
        assertThat(schema.getName(), is("default_schema"));
        assertTrue(schema.getAllTables().isEmpty());
        assertTrue(schema.getAllViews().isEmpty());
        assertTrue(schema.isEmpty());
    }
    
    private GenericSchemaBuilderMaterial createBuilderMaterial() {
        return new GenericSchemaBuilderMaterial(Collections.emptyMap(), Collections.emptyList(), new ConfigurationProperties(new Properties()), "default_schema");
    }
}
