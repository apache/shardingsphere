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

package org.apache.shardingsphere.infra.metadata.database.schema;

import org.apache.shardingsphere.infra.metadata.database.schema.decorator.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.database.schema.decorator.model.ShardingSphereTable;
import org.apache.shardingsphere.infra.metadata.database.schema.decorator.model.ShardingSphereView;
import org.junit.Test;

import java.util.Collections;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public final class SchemaManagerTest {
    
    @Test
    public void assertGetToBeDeletedSchemaMetaData() {
        ShardingSphereSchema loadedSchemas = new ShardingSphereSchema(Collections.emptyMap(), Collections.emptyMap());
        ShardingSphereSchema currentSchemas =
                new ShardingSphereSchema(Collections.singletonMap("foo_table", new ShardingSphereTable()), Collections.singletonMap("foo_view", new ShardingSphereView("", "")));
        ShardingSphereSchema actual = SchemaManager.getToBeDeletedSchemaMetaData(loadedSchemas, currentSchemas);
        assertThat(actual.getTables().size(), is(1));
    }
    
    @Test
    public void assertGetToBeAddedTables() {
        Map<String, ShardingSphereTable> actual = SchemaManager.getToBeAddedTables(Collections.singletonMap("foo_table", new ShardingSphereTable()), Collections.emptyMap());
        assertThat(actual.size(), is(1));
        assertTrue(actual.containsKey("foo_table"));
    }
    
    @Test
    public void assertGetToBeDeletedTables() {
        Map<String, ShardingSphereTable> actual = SchemaManager.getToBeDeletedTables(Collections.emptyMap(), Collections.singletonMap("foo_table", new ShardingSphereTable()));
        assertThat(actual.size(), is(1));
        assertTrue(actual.containsKey("foo_table"));
    }
}
