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

package org.apache.shardingsphere.governance.core.config;

import org.apache.shardingsphere.infra.database.DefaultSchema;
import org.junit.Test;

import java.util.Collection;
import java.util.Collections;

import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public final class ConfigCenterNodeTest {
    
    private final ConfigCenterNode configurationNode = new ConfigCenterNode();
    
    @Test
    public void assertGetSchemaPath() {
        assertThat(configurationNode.getRulePath(DefaultSchema.LOGIC_NAME), is("/schemas/logic_db/rule"));
    }
    
    @Test
    public void assertGetDataSourcePath() {
        assertThat(configurationNode.getDataSourcePath(DefaultSchema.LOGIC_NAME), is("/schemas/logic_db/datasource"));
    }
    
    @Test
    public void assertGetRulePath() {
        assertThat(configurationNode.getRulePath(DefaultSchema.LOGIC_NAME), is("/schemas/logic_db/rule"));
    }
    
    @Test
    public void assertGetAuthenticationPath() {
        assertThat(configurationNode.getAuthenticationPath(), is("/authentication"));
    }
    
    @Test
    public void assertGetPropsPath() {
        assertThat(configurationNode.getPropsPath(), is("/props"));
    }
    
    @Test
    public void assertGetSchemaName() {
        assertThat(configurationNode.getSchemaName("/schemas/logic_db/rule"), is(DefaultSchema.LOGIC_NAME));
    }
    
    @Test
    public void assertGetAllSchemaConfigPaths() {
        Collection<String> actual = configurationNode.getAllSchemaConfigPaths(Collections.singletonList(DefaultSchema.LOGIC_NAME));
        assertThat(actual.size(), is(4));
        assertThat(actual, hasItems("/schemas"));
        assertThat(actual, hasItems("/schemas/logic_db"));
        assertThat(actual, hasItems("/schemas/logic_db/rule"));
        assertThat(actual, hasItems("/schemas/logic_db/datasource"));
    }
    
    @Test
    public void assertGetSchemaNamePath() {
        assertThat(configurationNode.getSchemaNamePath("sharding_db"), is("/schemas/sharding_db"));
    }
}
