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

package org.apache.shardingsphere.mode.metadata;

import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.database.core.type.DatabaseType;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.statistics.ShardingSphereStatistics;
import org.apache.shardingsphere.infra.metadata.statistics.builder.ShardingSphereStatisticsFactory;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.mode.metadata.persist.MetaDataPersistFacade;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class MetaDataContextsTest {
    
    @Test
    void assertGetDefaultMetaData() {
        ShardingSphereDatabase database = new ShardingSphereDatabase("foo_db", TypedSPILoader.getService(DatabaseType.class, "FIXTURE"), mock(), mock(), Collections.emptyList());
        ShardingSphereMetaData metaData = new ShardingSphereMetaData(Collections.singleton(database), mock(), mock(), new ConfigurationProperties(new Properties()));
        assertThat(new MetaDataContexts(metaData, ShardingSphereStatisticsFactory.create(metaData, new ShardingSphereStatistics())).getMetaData().getDatabase("foo_db"), is(database));
    }
    
    @Test
    void assertUpdateMetaDataContexts() {
        ShardingSphereMetaData metaData = mock(ShardingSphereMetaData.class);
        ShardingSphereStatistics statistics = mock(ShardingSphereStatistics.class);
        MetaDataContexts metaDataContexts = new MetaDataContexts(metaData, statistics);
        ShardingSphereMetaData newMetaData = mock(ShardingSphereMetaData.class);
        ShardingSphereStatistics newStatistics = mock(ShardingSphereStatistics.class);
        metaDataContexts.update(new MetaDataContexts(newMetaData, newStatistics));
        assertThat(metaDataContexts.getMetaData(), is(newMetaData));
        assertThat(metaDataContexts.getStatistics(), is(newStatistics));
        verify(metaData).close();
    }
    
    @Test
    void assertUpdateMetaData() {
        ShardingSphereMetaData metaData = mock(ShardingSphereMetaData.class);
        ShardingSphereStatistics statistics = mock(ShardingSphereStatistics.class);
        MetaDataContexts metaDataContexts = new MetaDataContexts(metaData, statistics);
        ShardingSphereMetaData newMetaData = mock(ShardingSphereMetaData.class);
        MetaDataPersistFacade metaDataPersistFacade = mock(MetaDataPersistFacade.class, RETURNS_DEEP_STUBS);
        when(metaDataPersistFacade.getStatisticsService().load(metaData)).thenReturn(mock(ShardingSphereStatistics.class));
        metaDataContexts.update(newMetaData, metaDataPersistFacade);
        assertThat(metaDataContexts.getMetaData(), is(newMetaData));
        verify(metaData).close();
    }
}
