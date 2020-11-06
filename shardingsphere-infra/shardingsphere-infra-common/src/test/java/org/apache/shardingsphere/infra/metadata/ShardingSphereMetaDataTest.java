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

package org.apache.shardingsphere.infra.metadata;

import org.apache.shardingsphere.infra.config.RuleConfiguration;
import org.apache.shardingsphere.infra.metadata.resource.ShardingSphereResource;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.schema.model.datasource.CachedDatabaseMetaData;
import org.apache.shardingsphere.infra.schema.model.datasource.DataSourcesMetaData;
import org.junit.Test;

import javax.sql.DataSource;
import java.util.Collections;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

public final class ShardingSphereMetaDataTest {
    
    @Test
    public void assertIsComplete() {
        ShardingSphereResource resource = new ShardingSphereResource(Collections.singletonMap("ds", mock(DataSource.class)), mock(DataSourcesMetaData.class), mock(CachedDatabaseMetaData.class));
        ShardingSphereMetaData metaData = new ShardingSphereMetaData(
                "name", Collections.singleton(mock(RuleConfiguration.class)), Collections.singleton(mock(ShardingSphereRule.class)), resource, mock(ShardingSphereSchema.class));
        assertTrue(metaData.isComplete());
    }
    
    @Test
    public void assertIsNotCompleteWithoutRule() {
        ShardingSphereResource resource = new ShardingSphereResource(Collections.singletonMap("ds", mock(DataSource.class)), mock(DataSourcesMetaData.class), mock(CachedDatabaseMetaData.class));
        ShardingSphereMetaData metaData = new ShardingSphereMetaData("name", Collections.emptyList(), Collections.emptyList(), resource, mock(ShardingSphereSchema.class));
        assertFalse(metaData.isComplete());
    }
    
    @Test
    public void assertIsNotCompleteWithoutDataSource() {
        ShardingSphereResource resource = new ShardingSphereResource(Collections.emptyMap(), mock(DataSourcesMetaData.class), mock(CachedDatabaseMetaData.class));
        ShardingSphereMetaData metaData = new ShardingSphereMetaData(
                "name", Collections.singleton(mock(RuleConfiguration.class)), Collections.singleton(mock(ShardingSphereRule.class)), resource, mock(ShardingSphereSchema.class));
        assertFalse(metaData.isComplete());
    }
}
