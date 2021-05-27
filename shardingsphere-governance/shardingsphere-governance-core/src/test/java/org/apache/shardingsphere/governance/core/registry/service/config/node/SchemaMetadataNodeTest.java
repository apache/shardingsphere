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

package org.apache.shardingsphere.governance.core.registry.service.config.node;

import org.apache.shardingsphere.infra.database.DefaultSchema;
import org.junit.Test;

import java.util.Collection;
import java.util.Collections;

import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class SchemaMetadataNodeTest {
    
    @Test
    public void assertGetRulePath() {
        assertThat(SchemaMetadataNode.getRulePath(DefaultSchema.LOGIC_NAME), is("/metadata/logic_db/rules"));
    }
    
    @Test
    public void assertGetSchemaName() {
        assertThat(SchemaMetadataNode.getSchemaName("/metadata/logic_db/rules"), is(DefaultSchema.LOGIC_NAME));
    }
    
    @Test
    public void assertGetAllSchemaConfigPaths() {
        Collection<String> actual = SchemaMetadataNode.getAllSchemaConfigPaths(Collections.singletonList(DefaultSchema.LOGIC_NAME));
        assertThat(actual.size(), is(4));
        assertThat(actual, hasItems("/metadata"));
        assertThat(actual, hasItems("/metadata/logic_db/rules"));
        assertThat(actual, hasItems("/metadata/logic_db/dataSources"));
        assertThat(actual, hasItems("/metadata/logic_db/schema"));
    }
    
    @Test
    public void assertGetSchemaNamePath() {
        assertThat(SchemaMetadataNode.getSchemaNamePath("sharding_db"), is("/metadata/sharding_db"));
    }
    
    @Test
    public void assertGetMetadataSchemaPath() {
        assertThat(SchemaMetadataNode.getMetadataSchemaPath("sharding_db"), is("/metadata/sharding_db/schema"));
    }
}
