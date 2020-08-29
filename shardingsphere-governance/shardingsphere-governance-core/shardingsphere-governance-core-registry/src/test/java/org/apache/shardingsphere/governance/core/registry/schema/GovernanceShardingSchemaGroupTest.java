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

package org.apache.shardingsphere.governance.core.registry.schema;

import org.junit.Test;

import java.util.Arrays;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public final class GovernanceShardingSchemaGroupTest {
    
    @Test
    public void assertAddWithExistedSchemaName() {
        GovernanceShardingSchemaGroup actual = new GovernanceShardingSchemaGroup();
        actual.add(new GovernanceSchema("test_0.ds_0"));
        actual.add(new GovernanceSchema("test_0.ds_1"));
        assertThat(actual.getDataSourceNames("test_0").size(), is(2));
        assertTrue(actual.getDataSourceNames("test_0").contains("ds_0"));
        assertTrue(actual.getDataSourceNames("test_0").contains("ds_1"));
    }
    
    @Test
    public void assertAddWithoutExistedSchemaName() {
        GovernanceShardingSchemaGroup actual = new GovernanceShardingSchemaGroup();
        actual.add(new GovernanceSchema("test_0.ds_0"));
        actual.add(new GovernanceSchema("test_1.ds_1"));
        assertThat(actual.getDataSourceNames("test_0").size(), is(1));
        assertTrue(actual.getDataSourceNames("test_0").contains("ds_0"));
        assertThat(actual.getDataSourceNames("test_1").size(), is(1));
        assertTrue(actual.getDataSourceNames("test_1").contains("ds_1"));
    }
    
    @Test
    public void assertPut() {
        GovernanceShardingSchemaGroup actual = new GovernanceShardingSchemaGroup();
        actual.put("test", Arrays.asList("ds_0", "ds_1"));
        assertThat(actual.getDataSourceNames("test").size(), is(2));
        assertTrue(actual.getDataSourceNames("test").contains("ds_0"));
        assertTrue(actual.getDataSourceNames("test").contains("ds_1"));
    }
}
