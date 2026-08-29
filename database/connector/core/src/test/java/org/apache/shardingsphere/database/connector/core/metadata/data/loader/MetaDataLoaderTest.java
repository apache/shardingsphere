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

package org.apache.shardingsphere.database.connector.core.metadata.data.loader;

import lombok.SneakyThrows;
import org.apache.shardingsphere.database.connector.core.metadata.data.model.SchemaMetaData;
import org.apache.shardingsphere.database.connector.core.metadata.data.model.TableMetaData;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.junit.jupiter.api.Test;
import org.mockito.internal.configuration.plugins.Plugins;

import javax.sql.DataSource;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isA;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class MetaDataLoaderTest {
    
    private final DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "FIXTURE");
    
    @Test
    void assertLoadWithDialectLoader() throws Exception {
        MetaDataLoaderMaterial material = new MetaDataLoaderMaterial(Collections.singleton("foo_tbl"), "dialect_success", mock(DataSource.class, RETURNS_DEEP_STUBS), databaseType, "foo_db");
        DialectMetaDataLoader dialectMetaDataLoader = mock(DialectMetaDataLoader.class);
        when(dialectMetaDataLoader.getType()).thenReturn(databaseType);
        when(dialectMetaDataLoader.load(material)).thenReturn(Collections.singleton(
                new SchemaMetaData("foo_db", Collections.singleton(new TableMetaData("FOO_TBL", Collections.emptyList(), Collections.emptyList(), Collections.emptyList())))));
        try (AutoCloseable ignored = registerDialectMetaDataLoader(dialectMetaDataLoader)) {
            Map<String, SchemaMetaData> actual = MetaDataLoader.load(Collections.singleton(material));
            TableMetaData actualTable = actual.get("foo_db").getTables().iterator().next();
            assertThat(actualTable.getName(), is("FOO_TBL"));
            assertThat(actualTable.getStorageUnitName(), is("dialect_success"));
        }
    }
    
    @Test
    void assertLoadWithDialectLoaderSQLException() throws Exception {
        MetaDataLoaderMaterial material = new MetaDataLoaderMaterial(Collections.singleton("foo_tbl"), "dialect_sql_exception", mock(DataSource.class, RETURNS_DEEP_STUBS), databaseType, "foo_db");
        DialectMetaDataLoader dialectMetaDataLoader = mock(DialectMetaDataLoader.class);
        when(dialectMetaDataLoader.getType()).thenReturn(databaseType);
        SQLException expected = new SQLException("Dialect metadata loading failed.");
        when(dialectMetaDataLoader.load(any(MetaDataLoaderMaterial.class))).thenThrow(expected);
        try (AutoCloseable ignored = registerDialectMetaDataLoader(dialectMetaDataLoader)) {
            SQLException actual = assertThrows(SQLException.class, () -> MetaDataLoader.load(Collections.singleton(material)));
            assertThat(actual, is(expected));
        }
    }
    
    @Test
    void assertLoadWithNullDialectMetadata() throws Exception {
        MetaDataLoaderMaterial material = new MetaDataLoaderMaterial(Collections.singleton("foo_tbl"), "dialect_null", mock(DataSource.class, RETURNS_DEEP_STUBS), databaseType, "foo_db");
        DialectMetaDataLoader dialectMetaDataLoader = mock(DialectMetaDataLoader.class);
        when(dialectMetaDataLoader.getType()).thenReturn(databaseType);
        when(dialectMetaDataLoader.load(material)).thenReturn(null);
        try (AutoCloseable ignored = registerDialectMetaDataLoader(dialectMetaDataLoader)) {
            SQLException actual = assertThrows(SQLException.class, () -> MetaDataLoader.load(Collections.singleton(material)));
            assertThat(actual.getMessage(), is("Schema metadata is null for storage unit 'dialect_null'."));
        }
    }
    
    @Test
    void assertLoadWithEmptyDialectMetadata() throws Exception {
        MetaDataLoaderMaterial material = new MetaDataLoaderMaterial(Collections.singleton("foo_tbl"), "dialect_empty", mock(DataSource.class, RETURNS_DEEP_STUBS), databaseType, "foo_db");
        DialectMetaDataLoader dialectMetaDataLoader = mock(DialectMetaDataLoader.class);
        when(dialectMetaDataLoader.getType()).thenReturn(databaseType);
        when(dialectMetaDataLoader.load(material)).thenReturn(Collections.emptyList());
        try (AutoCloseable ignored = registerDialectMetaDataLoader(dialectMetaDataLoader)) {
            SQLException actual = assertThrows(SQLException.class, () -> MetaDataLoader.load(Collections.singleton(material)));
            assertThat(actual.getMessage(), is("Schema metadata is empty for storage unit 'dialect_empty', tables [foo_tbl]."));
        }
    }
    
    @Test
    void assertLoadWithIncompleteDialectMetadata() throws Exception {
        MetaDataLoaderMaterial material = new MetaDataLoaderMaterial(Collections.singleton("foo_tbl"), "dialect_incomplete", mock(DataSource.class, RETURNS_DEEP_STUBS), databaseType, "foo_db");
        DialectMetaDataLoader dialectMetaDataLoader = mock(DialectMetaDataLoader.class);
        when(dialectMetaDataLoader.getType()).thenReturn(databaseType);
        when(dialectMetaDataLoader.load(material)).thenReturn(Collections.singleton(new SchemaMetaData("foo_db", Collections.emptyList())));
        try (AutoCloseable ignored = registerDialectMetaDataLoader(dialectMetaDataLoader)) {
            SQLException actual = assertThrows(SQLException.class, () -> MetaDataLoader.load(Collections.singleton(material)));
            assertThat(actual.getMessage(), is("Schema metadata is incomplete for storage unit 'dialect_incomplete', missing tables [foo_tbl]."));
        }
    }
    
    @Test
    void assertLoadWithMissingMetadataInOneStorageUnit() throws Exception {
        MetaDataLoaderMaterial loadedMaterial = new MetaDataLoaderMaterial(Collections.singleton("foo_tbl"), "foo_ds_1", mock(DataSource.class, RETURNS_DEEP_STUBS), databaseType, "foo_db");
        MetaDataLoaderMaterial missingMaterial = new MetaDataLoaderMaterial(Collections.singleton("foo_tbl"), "foo_ds_2", mock(DataSource.class, RETURNS_DEEP_STUBS), databaseType, "foo_db");
        DialectMetaDataLoader dialectMetaDataLoader = mock(DialectMetaDataLoader.class);
        when(dialectMetaDataLoader.getType()).thenReturn(databaseType);
        when(dialectMetaDataLoader.load(any(MetaDataLoaderMaterial.class))).thenAnswer(invocation -> {
            MetaDataLoaderMaterial material = invocation.getArgument(0);
            return "foo_ds_1".equals(material.getStorageUnitName())
                    ? createSchemaMetaData("foo_tbl")
                    : Collections.singleton(new SchemaMetaData("foo_db", Collections.emptyList()));
        });
        try (AutoCloseable ignored = registerDialectMetaDataLoader(dialectMetaDataLoader)) {
            SQLException actual = assertThrows(SQLException.class, () -> MetaDataLoader.load(Arrays.asList(loadedMaterial, missingMaterial)));
            assertThat(actual.getMessage(), is("Schema metadata is incomplete for storage unit 'foo_ds_2', missing tables [foo_tbl]."));
        }
    }
    
    @Test
    void assertLoadWithMergedSchemaMetaData() throws SQLException {
        MetaDataLoaderMaterial firstMaterial = new MetaDataLoaderMaterial(Collections.emptyList(), "foo_ds_1", mock(DataSource.class, RETURNS_DEEP_STUBS), databaseType, "foo_db");
        MetaDataLoaderMaterial secondMaterial = new MetaDataLoaderMaterial(Collections.emptyList(), "foo_ds_2", mock(DataSource.class, RETURNS_DEEP_STUBS), databaseType, "foo_db");
        Map<String, SchemaMetaData> actual = MetaDataLoader.load(Arrays.asList(firstMaterial, secondMaterial));
        assertThat(actual.size(), is(1));
        assertTrue(actual.get("foo_db").getTables().isEmpty());
    }
    
    @Test
    void assertLoadWithDefaultLoader() throws SQLException {
        MetaDataLoaderMaterial material = new MetaDataLoaderMaterial(Collections.singleton("t_order"), "foo_ds", mock(DataSource.class, RETURNS_DEEP_STUBS), databaseType, "foo_db");
        SQLException actual = assertThrows(SQLException.class, () -> MetaDataLoader.load(Collections.singleton(material)));
        assertThat(actual.getMessage(), is("Table metadata not found for table 't_order' in storage unit 'foo_ds'."));
    }
    
    @Test
    void assertLoadWhenInterrupted() throws Exception {
        MetaDataLoaderMaterial material = new MetaDataLoaderMaterial(Collections.emptyList(), "foo_ds", mock(DataSource.class, RETURNS_DEEP_STUBS), databaseType, "foo_db");
        CountDownLatch latch = new CountDownLatch(1);
        DialectMetaDataLoader dialectMetaDataLoader = mock(DialectMetaDataLoader.class);
        when(dialectMetaDataLoader.getType()).thenReturn(databaseType);
        when(dialectMetaDataLoader.load(any(MetaDataLoaderMaterial.class))).thenAnswer(invocation -> {
            latch.await(5L, TimeUnit.SECONDS);
            return Collections.singleton(new SchemaMetaData("foo_db", Collections.emptyList()));
        });
        try (AutoCloseable ignored = registerDialectMetaDataLoader(dialectMetaDataLoader)) {
            Thread.currentThread().interrupt();
            try {
                SQLException actual = assertThrows(SQLException.class, () -> MetaDataLoader.load(Collections.singleton(material)));
                assertThat(actual.getMessage(), is("Interrupted while loading schema metadata."));
                assertTrue(Thread.currentThread().isInterrupted());
            } finally {
                Thread.interrupted();
                latch.countDown();
            }
        }
    }
    
    @Test
    void assertCancelRemainingTasksWhenExecutionExceptionOccurs() throws Exception {
        MetaDataLoaderMaterial failingMaterial = new MetaDataLoaderMaterial(Collections.emptyList(), "failing_ds", mock(DataSource.class, RETURNS_DEEP_STUBS), databaseType, "foo_db");
        MetaDataLoaderMaterial blockedMaterial = new MetaDataLoaderMaterial(Collections.emptyList(), "blocked_ds", mock(DataSource.class, RETURNS_DEEP_STUBS), databaseType, "foo_db");
        CountDownLatch blockedTaskStarted = new CountDownLatch(1);
        CountDownLatch blockedTaskInterrupted = new CountDownLatch(1);
        CountDownLatch blockedTaskRelease = new CountDownLatch(1);
        DialectMetaDataLoader dialectMetaDataLoader = mock(DialectMetaDataLoader.class);
        when(dialectMetaDataLoader.getType()).thenReturn(databaseType);
        when(dialectMetaDataLoader.load(any(MetaDataLoaderMaterial.class))).thenAnswer(invocation -> loadForCancellation(invocation.getArgument(0), blockedTaskStarted,
                blockedTaskInterrupted, blockedTaskRelease));
        try (AutoCloseable ignored = registerDialectMetaDataLoader(dialectMetaDataLoader)) {
            assertThrows(SQLException.class, () -> MetaDataLoader.load(Arrays.asList(failingMaterial, blockedMaterial)));
            assertTrue(blockedTaskInterrupted.await(5L, TimeUnit.SECONDS));
        } finally {
            blockedTaskRelease.countDown();
        }
    }
    
    private Collection<SchemaMetaData> loadForCancellation(final MetaDataLoaderMaterial material, final CountDownLatch blockedTaskStarted,
                                                           final CountDownLatch blockedTaskInterrupted, final CountDownLatch blockedTaskRelease) throws SQLException {
        if ("failing_ds".equals(material.getStorageUnitName())) {
            try {
                if (!blockedTaskStarted.await(5L, TimeUnit.SECONDS)) {
                    throw new SQLException("Blocked metadata task did not start.");
                }
            } catch (final InterruptedException ex) {
                Thread.currentThread().interrupt();
                throw new SQLException("Failing metadata task was interrupted.", ex);
            }
            throw new SQLException("Metadata load failed.");
        }
        blockedTaskStarted.countDown();
        try {
            blockedTaskRelease.await();
        } catch (final InterruptedException ex) {
            blockedTaskInterrupted.countDown();
            Thread.currentThread().interrupt();
            throw new SQLException("Metadata loading task was interrupted.", ex);
        }
        return Collections.singleton(new SchemaMetaData("foo_db", Collections.emptyList()));
    }
    
    private Collection<SchemaMetaData> createSchemaMetaData(final String tableName) {
        return Collections.singleton(new SchemaMetaData("foo_db", Collections.singleton(
                new TableMetaData(tableName, Collections.emptyList(), Collections.emptyList(), Collections.emptyList()))));
    }
    
    @Test
    void assertLoadWhenExecutionExceptionCauseIsSQLException() throws SQLException {
        DataSource dataSource = mock(DataSource.class);
        when(dataSource.getConnection()).thenReturn(mock(Connection.class, RETURNS_DEEP_STUBS)).thenThrow(SQLException.class);
        MetaDataLoaderMaterial material = new MetaDataLoaderMaterial(Collections.singleton("t_order"), "foo_ds", dataSource, databaseType, "foo_db");
        SQLException ex = assertThrows(SQLException.class, () -> MetaDataLoader.load(Collections.singleton(material)));
        assertNull(ex.getCause());
    }
    
    @Test
    void assertLoadWhenExecutionExceptionCauseIsNotSQLException() throws SQLException {
        DataSource dataSource = mock(DataSource.class);
        when(dataSource.getConnection()).thenReturn(mock(Connection.class, RETURNS_DEEP_STUBS)).thenThrow(IllegalStateException.class);
        MetaDataLoaderMaterial material = new MetaDataLoaderMaterial(Collections.singleton("t_order"), "foo_ds", dataSource, databaseType, "foo_db");
        SQLException ex = assertThrows(SQLException.class, () -> MetaDataLoader.load(Collections.singleton(material)));
        assertThat(ex.getCause().getCause(), isA(IllegalStateException.class));
    }
    
    @SneakyThrows(ReflectiveOperationException.class)
    private AutoCloseable registerDialectMetaDataLoader(final DialectMetaDataLoader service) {
        Map<Class<?>, Object> registeredServices = getRegisteredServices();
        Object original = registeredServices.put(DialectMetaDataLoader.class, createRegisteredService(service));
        return () -> restoreDialectMetaDataLoader(registeredServices, original);
    }
    
    @SuppressWarnings("unchecked")
    private Map<Class<?>, Object> getRegisteredServices() throws ReflectiveOperationException {
        Field registeredServicesField = ShardingSphereServiceLoader.class.getDeclaredField("REGISTERED_SERVICES");
        return (Map<Class<?>, Object>) Plugins.getMemberAccessor().get(registeredServicesField, ShardingSphereServiceLoader.class);
    }
    
    @SuppressWarnings("unchecked")
    private Object createRegisteredService(final DialectMetaDataLoader service) throws ReflectiveOperationException {
        Class<?> registeredServiceClass = Class.forName("org.apache.shardingsphere.infra.spi.RegisteredShardingSphereSPI");
        Constructor<?> constructor = registeredServiceClass.getDeclaredConstructor(Class.class);
        Object result = Plugins.getMemberAccessor().newInstance(constructor, DialectMetaDataLoader.class);
        Field servicesField = registeredServiceClass.getDeclaredField("services");
        Collection<DialectMetaDataLoader> services = (Collection<DialectMetaDataLoader>) Plugins.getMemberAccessor().get(servicesField, result);
        services.clear();
        services.add(service);
        return result;
    }
    
    private void restoreDialectMetaDataLoader(final Map<Class<?>, Object> registeredServices, final Object original) {
        if (null == original) {
            registeredServices.remove(DialectMetaDataLoader.class);
        } else {
            registeredServices.put(DialectMetaDataLoader.class, original);
        }
    }
}
