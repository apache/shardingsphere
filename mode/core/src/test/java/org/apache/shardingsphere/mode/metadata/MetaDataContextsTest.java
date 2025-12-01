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

import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.resource.ResourceMetaData;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.infra.metadata.statistics.ShardingSphereStatistics;
import org.apache.shardingsphere.infra.metadata.statistics.builder.ShardingSphereStatisticsFactory;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.mode.metadata.persist.MetaDataPersistFacade;
import org.apache.shardingsphere.mode.metadata.persist.statistics.StatisticsPersistService;
import org.apache.shardingsphere.test.infra.framework.extension.mock.AutoMockExtension;
import org.apache.shardingsphere.test.infra.framework.extension.mock.StaticMockSettings;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.Collections;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(AutoMockExtension.class)
@StaticMockSettings(ShardingSphereStatisticsFactory.class)
class MetaDataContextsTest {
    
    @Test
    void assertGetMetaData() {
        ShardingSphereMetaData metaData = createMetaData("foo_db");
        assertThat(new MetaDataContexts(metaData, new ShardingSphereStatistics()).getMetaData(), is(metaData));
    }
    
    @Test
    void assertGetStatistics() {
        ShardingSphereStatistics statistics = new ShardingSphereStatistics();
        assertThat(new MetaDataContexts(createMetaData("foo_db"), statistics).getStatistics(), is(statistics));
    }
    
    @Test
    void assertUpdateWithNewMetaDataContexts() {
        MetaDataContexts metaDataContexts = new MetaDataContexts(createMetaData("foo_db"), new ShardingSphereStatistics());
        MetaDataContexts newMetaDataContexts = new MetaDataContexts(createMetaData("bar_db"), new ShardingSphereStatistics());
        metaDataContexts.update(newMetaDataContexts);
        assertThat(metaDataContexts.getMetaData(), is(newMetaDataContexts.getMetaData()));
        assertThat(metaDataContexts.getStatistics(), is(newMetaDataContexts.getStatistics()));
    }
    
    @Test
    void assertUpdateWithPersistFacade() {
        ShardingSphereMetaData metaData = createMetaData("foo_db");
        ShardingSphereStatistics loadedStatistics = new ShardingSphereStatistics();
        ShardingSphereStatistics refreshedStatistics = new ShardingSphereStatistics();
        StatisticsPersistService statisticsPersistService = mock(StatisticsPersistService.class);
        MetaDataPersistFacade metaDataPersistFacade = mock(MetaDataPersistFacade.class);
        when(metaDataPersistFacade.getStatisticsService()).thenReturn(statisticsPersistService);
        when(statisticsPersistService.load(metaData)).thenReturn(loadedStatistics);
        MetaDataContexts metaDataContexts = new MetaDataContexts(createMetaData("bar_db"), new ShardingSphereStatistics());
        when(ShardingSphereStatisticsFactory.create(metaData, loadedStatistics)).thenReturn(refreshedStatistics);
        metaDataContexts.update(metaData, metaDataPersistFacade);
        verify(statisticsPersistService).load(metaData);
        assertThat(metaDataContexts.getMetaData(), is(metaData));
        assertThat(metaDataContexts.getStatistics(), is(refreshedStatistics));
    }
    
    private ShardingSphereMetaData createMetaData(final String databaseName) {
        DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "FIXTURE");
        ShardingSphereDatabase database = new ShardingSphereDatabase(
                databaseName, databaseType, new ResourceMetaData(Collections.emptyMap()), new RuleMetaData(Collections.emptyList()), Collections.emptyList());
        return new ShardingSphereMetaData(
                Collections.singleton(database), new ResourceMetaData(Collections.emptyMap()), new RuleMetaData(Collections.emptyList()), new ConfigurationProperties(new Properties()));
    }
}
