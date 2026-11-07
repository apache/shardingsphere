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
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereColumn;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereTable;
import org.apache.shardingsphere.infra.metadata.statistics.ShardingSphereStatistics;
import org.apache.shardingsphere.infra.metadata.statistics.builder.ShardingSphereStatisticsFactory;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.mode.metadata.manager.database.DatabaseMetaDataManager;
import org.apache.shardingsphere.mode.metadata.persist.MetaDataPersistFacade;
import org.apache.shardingsphere.mode.metadata.persist.statistics.StatisticsPersistService;
import org.apache.shardingsphere.test.infra.framework.extension.mock.AutoMockExtension;
import org.apache.shardingsphere.test.infra.framework.extension.mock.StaticMockSettings;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.sql.Types;
import java.util.Collections;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
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
                databaseName, databaseType, new ResourceMetaData(Collections.emptyMap()), new RuleMetaData(Collections.emptyList()), Collections.emptyList(),
                new ConfigurationProperties(new Properties()));
        return new ShardingSphereMetaData(
                Collections.singleton(database), new ResourceMetaData(Collections.emptyMap()), new RuleMetaData(Collections.emptyList()), new ConfigurationProperties(new Properties()));
    }
    
    @Test
    void assertUpdateWithChangedTable() {
        ShardingSphereTable previousTable = new ShardingSphereTable("foo_tbl", Collections.emptyList(), Collections.emptyList(), Collections.emptyList());
        MetaDataContexts metaDataContexts = new MetaDataContexts(createBindingMetaData(previousTable), new ShardingSphereStatistics());
        ShardingSphereTable changedTable = new ShardingSphereTable("foo_tbl",
                Collections.singleton(new ShardingSphereColumn("foo_col", Types.INTEGER, false, false, false, true, false, true)), Collections.emptyList(), Collections.emptyList());
        ShardingSphereMetaData metaData = createBindingMetaData(changedTable);
        metaDataContexts.update(new MetaDataContexts(metaData, new ShardingSphereStatistics()));
        assertTrue(previousTable.isBindingInvalidated());
        assertFalse(changedTable.isBindingInvalidated());
        assertThat(metaDataContexts.getMetaData(), sameInstance(metaData));
    }
    
    private ShardingSphereMetaData createBindingMetaData(final ShardingSphereTable table) {
        DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "FIXTURE");
        ShardingSphereDatabase database = new ShardingSphereDatabase("foo_db", databaseType, new ResourceMetaData(Collections.emptyMap()), new RuleMetaData(Collections.emptyList()),
                Collections.singleton(new ShardingSphereSchema("foo_schema", databaseType, Collections.singleton(table), Collections.emptyList())),
                new ConfigurationProperties(new Properties()));
        return new ShardingSphereMetaData(Collections.singleton(database),
                new ResourceMetaData(Collections.emptyMap()), new RuleMetaData(Collections.emptyList()), new ConfigurationProperties(new Properties()));
    }
    
    @Test
    void assertUpdateWithPersistFacadeAndUnchangedTable() {
        ShardingSphereTable previousTable = new ShardingSphereTable("foo_tbl", Collections.emptyList(), Collections.emptyList(), Collections.emptyList());
        MetaDataContexts metaDataContexts = new MetaDataContexts(createBindingMetaData(previousTable), new ShardingSphereStatistics());
        ShardingSphereTable equivalentTable = new ShardingSphereTable("foo_tbl", Collections.emptyList(), Collections.emptyList(), Collections.emptyList());
        ShardingSphereMetaData metaData = createBindingMetaData(equivalentTable);
        MetaDataPersistFacade persistFacade = mock(MetaDataPersistFacade.class, RETURNS_DEEP_STUBS);
        ShardingSphereStatistics loadedStatistics = new ShardingSphereStatistics();
        when(persistFacade.getStatisticsService().load(metaData)).thenReturn(loadedStatistics);
        when(ShardingSphereStatisticsFactory.create(metaData, loadedStatistics)).thenReturn(new ShardingSphereStatistics());
        metaDataContexts.update(metaData, persistFacade);
        assertFalse(previousTable.isBindingInvalidated());
        assertThat(metaDataContexts.getMetaData(), sameInstance(metaData));
        metaData.getDatabase("foo_db").dropSchema("foo_schema");
        assertTrue(previousTable.isBindingInvalidated());
    }
    
    @Test
    void assertUpdateWithPreviouslyPublishedSnapshot() {
        ShardingSphereTable previousTable = new ShardingSphereTable("foo_tbl", Collections.emptyList(), Collections.emptyList(), Collections.emptyList());
        ShardingSphereMetaData previousMetaData = createBindingMetaData(previousTable);
        MetaDataContexts previousContexts = new MetaDataContexts(previousMetaData, new ShardingSphereStatistics());
        MetaDataContexts metaDataContexts = new MetaDataContexts(previousMetaData, new ShardingSphereStatistics());
        ShardingSphereTable changedTable = new ShardingSphereTable("foo_tbl",
                Collections.singleton(new ShardingSphereColumn("foo_col", Types.INTEGER, false, false, false, true, false, true)), Collections.emptyList(), Collections.emptyList());
        metaDataContexts.update(new MetaDataContexts(createBindingMetaData(changedTable), new ShardingSphereStatistics()));
        ShardingSphereTable equivalentTable = new ShardingSphereTable("foo_tbl", Collections.emptyList(), Collections.emptyList(), Collections.emptyList());
        metaDataContexts.update(new MetaDataContexts(createBindingMetaData(equivalentTable), new ShardingSphereStatistics()));
        metaDataContexts.update(previousContexts);
        assertTrue(previousTable.isBindingInvalidated());
        assertThat(previousMetaData.getDatabase("foo_db").getSchema("foo_schema").getTable("foo_tbl"), sameInstance(previousTable));
        ShardingSphereTable actualTable = metaDataContexts.getMetaData().getDatabase("foo_db").getSchema("foo_schema").getTable("foo_tbl");
        assertFalse(actualTable.isBindingInvalidated());
        metaDataContexts.getMetaData().getDatabase("foo_db").getSchema("foo_schema").removeTable("foo_tbl");
        assertTrue(actualTable.isBindingInvalidated());
        assertTrue(equivalentTable.isBindingInvalidated());
        assertTrue(previousTable.isBindingInvalidated());
    }
    
    @Test
    void assertUpdateSameContextsWithRetiredSnapshot() {
        ShardingSphereTable previousTable = new ShardingSphereTable("foo_tbl", Collections.emptyList(), Collections.emptyList(), Collections.emptyList());
        ShardingSphereMetaData previousMetaData = createBindingMetaData(previousTable);
        MetaDataContexts metaDataContexts = new MetaDataContexts(previousMetaData, new ShardingSphereStatistics());
        ShardingSphereTable changedTable = new ShardingSphereTable("foo_tbl",
                Collections.singleton(new ShardingSphereColumn("foo_col", Types.INTEGER, false, false, false, true, false, true)), Collections.emptyList(), Collections.emptyList());
        new MetaDataContexts(previousMetaData, new ShardingSphereStatistics()).update(new MetaDataContexts(createBindingMetaData(changedTable), new ShardingSphereStatistics()));
        metaDataContexts.update(metaDataContexts);
        assertTrue(previousTable.isBindingInvalidated());
        assertThat(previousMetaData.getDatabase("foo_db").getSchema("foo_schema").getTable("foo_tbl"), sameInstance(previousTable));
        assertFalse(metaDataContexts.getMetaData().getDatabase("foo_db").getSchema("foo_schema").getTable("foo_tbl").isBindingInvalidated());
    }
    
    @Test
    void assertUpdateWithConcurrentPersistPublication() throws Exception {
        ShardingSphereTable previousTable = new ShardingSphereTable("foo_tbl", Collections.emptyList(), Collections.emptyList(), Collections.emptyList());
        MetaDataContexts metaDataContexts = new MetaDataContexts(createBindingMetaData(previousTable), new ShardingSphereStatistics());
        ShardingSphereTable equivalentTable = new ShardingSphereTable("foo_tbl", Collections.emptyList(), Collections.emptyList(), Collections.emptyList());
        ShardingSphereMetaData equivalentMetaData = spy(createBindingMetaData(equivalentTable));
        CountDownLatch inherited = new CountDownLatch(1);
        CountDownLatch publish = new CountDownLatch(1);
        pauseAfterInheritance(equivalentMetaData, inherited, publish);
        ShardingSphereTable changedTable = new ShardingSphereTable("foo_tbl",
                Collections.singleton(new ShardingSphereColumn("foo_col", Types.INTEGER, false, false, false, true, false, true)), Collections.emptyList(), Collections.emptyList());
        ShardingSphereMetaData changedMetaData = createBindingMetaData(changedTable);
        MetaDataPersistFacade persistFacade = mock(MetaDataPersistFacade.class, RETURNS_DEEP_STUBS);
        when(persistFacade.getStatisticsService().load(changedMetaData)).thenReturn(new ShardingSphereStatistics());
        ExecutorService executor = Executors.newFixedThreadPool(2);
        try {
            CompletableFuture<Void> firstUpdate = CompletableFuture.runAsync(() -> metaDataContexts.update(new MetaDataContexts(equivalentMetaData, new ShardingSphereStatistics())), executor);
            assertTrue(inherited.await(5L, TimeUnit.SECONDS));
            CountDownLatch secondUpdateFinished = new CountDownLatch(1);
            CompletableFuture<Void> secondUpdate = CompletableFuture.runAsync(() -> {
                metaDataContexts.update(changedMetaData, persistFacade);
                secondUpdateFinished.countDown();
            }, executor);
            secondUpdateFinished.await(1L, TimeUnit.SECONDS);
            publish.countDown();
            firstUpdate.get(5L, TimeUnit.SECONDS);
            secondUpdate.get(5L, TimeUnit.SECONDS);
            assertTrue(previousTable.isBindingInvalidated());
            assertFalse(metaDataContexts.getMetaData().getDatabase("foo_db").getSchema("foo_schema").getTable("foo_tbl").isBindingInvalidated());
        } finally {
            publish.countDown();
            executor.shutdownNow();
            assertTrue(executor.awaitTermination(5L, TimeUnit.SECONDS));
        }
    }
    
    private void pauseAfterInheritance(final ShardingSphereMetaData metaData, final CountDownLatch inherited, final CountDownLatch publish) {
        doAnswer(invocation -> {
            invocation.callRealMethod();
            inherited.countDown();
            assertTrue(publish.await(5L, TimeUnit.SECONDS));
            return null;
        }).when(metaData).inheritBindings(any(ShardingSphereMetaData.class));
    }
    
    @Test
    void assertUpdateWithConcurrentTableChange() throws Exception {
        ShardingSphereTable previousTable = new ShardingSphereTable("foo_tbl", Collections.emptyList(), Collections.emptyList(), Collections.emptyList());
        MetaDataContexts metaDataContexts = new MetaDataContexts(createBindingMetaData(previousTable), new ShardingSphereStatistics());
        ShardingSphereTable equivalentTable = new ShardingSphereTable("foo_tbl", Collections.emptyList(), Collections.emptyList(), Collections.emptyList());
        ShardingSphereMetaData equivalentMetaData = spy(createBindingMetaData(equivalentTable));
        CountDownLatch inherited = new CountDownLatch(1);
        CountDownLatch publish = new CountDownLatch(1);
        pauseAfterInheritance(equivalentMetaData, inherited, publish);
        ShardingSphereTable changedTable = new ShardingSphereTable("foo_tbl",
                Collections.singleton(new ShardingSphereColumn("foo_col", Types.INTEGER, false, false, false, true, false, true)), Collections.emptyList(), Collections.emptyList());
        DatabaseMetaDataManager databaseMetaDataManager = new DatabaseMetaDataManager(metaDataContexts, mock(MetaDataPersistFacade.class));
        ExecutorService executor = Executors.newFixedThreadPool(2);
        try {
            CompletableFuture<Void> firstUpdate = CompletableFuture.runAsync(() -> metaDataContexts.update(new MetaDataContexts(equivalentMetaData, new ShardingSphereStatistics())), executor);
            assertTrue(inherited.await(5L, TimeUnit.SECONDS));
            CountDownLatch tableUpdateFinished = new CountDownLatch(1);
            CompletableFuture<Void> tableUpdate = CompletableFuture.runAsync(() -> {
                databaseMetaDataManager.alterTable("foo_db", "foo_schema", changedTable);
                tableUpdateFinished.countDown();
            }, executor);
            tableUpdateFinished.await(1L, TimeUnit.SECONDS);
            publish.countDown();
            firstUpdate.get(5L, TimeUnit.SECONDS);
            tableUpdate.get(5L, TimeUnit.SECONDS);
            assertTrue(previousTable.isBindingInvalidated());
            assertFalse(metaDataContexts.getMetaData().getDatabase("foo_db").getSchema("foo_schema").getTable("foo_tbl").isBindingInvalidated());
        } finally {
            publish.countDown();
            executor.shutdownNow();
            assertTrue(executor.awaitTermination(5L, TimeUnit.SECONDS));
        }
    }
}
