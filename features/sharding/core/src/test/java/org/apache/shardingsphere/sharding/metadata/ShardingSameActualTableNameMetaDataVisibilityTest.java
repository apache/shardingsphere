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

package org.apache.shardingsphere.sharding.metadata;

import lombok.SneakyThrows;
import org.apache.shardingsphere.database.connector.core.metadata.data.loader.DialectMetaDataLoader;
import org.apache.shardingsphere.database.connector.core.metadata.data.loader.MetaDataLoaderMaterial;
import org.apache.shardingsphere.database.connector.core.metadata.data.model.ColumnMetaData;
import org.apache.shardingsphere.database.connector.core.metadata.data.model.SchemaMetaData;
import org.apache.shardingsphere.database.connector.core.metadata.data.model.TableMetaData;
import org.apache.shardingsphere.database.connector.core.metadata.database.datatype.DataTypeRegistry;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.instance.ComputeNodeInstanceContext;
import org.apache.shardingsphere.infra.metadata.database.resource.unit.StorageUnit;
import org.apache.shardingsphere.infra.metadata.database.schema.builder.GenericSchemaBuilder;
import org.apache.shardingsphere.infra.metadata.database.schema.builder.GenericSchemaBuilderMaterial;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereTable;
import org.apache.shardingsphere.infra.metadata.identifier.DatabaseIdentifierContextFactory;
import org.apache.shardingsphere.infra.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingTableRuleConfiguration;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.test.infra.fixture.jdbc.MockedDataSource;
import org.apache.shardingsphere.test.infra.framework.extension.mock.AutoMockExtension;
import org.apache.shardingsphere.test.infra.framework.extension.mock.StaticMockSettings;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.internal.configuration.plugins.Plugins;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import javax.sql.DataSource;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(AutoMockExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@StaticMockSettings(DataTypeRegistry.class)
class ShardingSameActualTableNameMetaDataVisibilityTest {
    
    private final DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "FIXTURE");
    
    @Test
    void assertBuildKeepsBothLogicTablesWhenSameActualTableNameInDifferentStorageUnits() throws Exception {
        DialectMetaDataLoader dialectMetaDataLoader = mock(DialectMetaDataLoader.class);
        when(dialectMetaDataLoader.getType()).thenReturn(databaseType);
        when(dialectMetaDataLoader.load(any(MetaDataLoaderMaterial.class))).thenAnswer(invocation -> createDialectLoadedSchema(invocation.getArgument(0)));
        try (AutoCloseable ignored = registerDialectMetaDataLoader(dialectMetaDataLoader)) {
            assertBothLogicTablesVisible(new Properties());
        }
    }
    
    @Test
    void assertBuildKeepsBothLogicTablesWhenCheckTableMetaDataEnabled() throws Exception {
        DialectMetaDataLoader dialectMetaDataLoader = mock(DialectMetaDataLoader.class);
        when(dialectMetaDataLoader.getType()).thenReturn(databaseType);
        when(dialectMetaDataLoader.load(any(MetaDataLoaderMaterial.class))).thenAnswer(invocation -> createDialectLoadedSchema(invocation.getArgument(0)));
        Properties props = new Properties();
        props.setProperty("check-table-metadata-enabled", "true");
        try (AutoCloseable ignored = registerDialectMetaDataLoader(dialectMetaDataLoader)) {
            assertBothLogicTablesVisible(props);
        }
    }
    
    private void assertBothLogicTablesVisible(final Properties props) throws SQLException {
        ShardingRule rule = createShardingRule();
        GenericSchemaBuilderMaterial material = createMaterial(rule, props);
        Map<String, ShardingSphereSchema> actual = GenericSchemaBuilder.build(Arrays.asList("t_order0", "t_order1"), databaseType, material);
        Collection<String> actualTableNames = actual.get(material.getDefaultSchemaName()).getAllTables().stream().map(ShardingSphereTable::getName).collect(Collectors.toList());
        assertThat(actual.size(), is(1));
        assertThat(actualTableNames.size(), is(2));
        assertThat(actualTableNames, containsInAnyOrder("t_order0", "t_order1"));
    }
    
    private Collection<SchemaMetaData> createDialectLoadedSchema(final MetaDataLoaderMaterial material) {
        return Collections.singleton(new SchemaMetaData(material.getDefaultSchemaName(), Collections.singletonList(createPhysicalTableMetaData())));
    }
    
    private TableMetaData createPhysicalTableMetaData() {
        Collection<ColumnMetaData> columns = Arrays.asList(
                new ColumnMetaData("order_id", Types.BIGINT, true, false, true, true, false, false),
                new ColumnMetaData("user_id", Types.INTEGER, false, false, true, true, false, false),
                new ColumnMetaData("status", Types.VARCHAR, false, false, true, true, false, false));
        return new TableMetaData("t_order", columns, Collections.emptyList(), Collections.emptyList());
    }
    
    private GenericSchemaBuilderMaterial createMaterial(final ShardingRule rule, final Properties props) {
        Map<String, StorageUnit> storageUnits = new LinkedHashMap<>(2, 1F);
        storageUnits.put("ds_0", createStorageUnit());
        storageUnits.put("ds_1", createStorageUnit());
        return new GenericSchemaBuilderMaterial(storageUnits, Collections.singleton(rule), new ConfigurationProperties(props),
                "sharding_db", DatabaseIdentifierContextFactory.createDefault());
    }
    
    private StorageUnit createStorageUnit() {
        StorageUnit result = mock(StorageUnit.class);
        when(result.getStorageType()).thenReturn(databaseType);
        when(result.getDataSource()).thenReturn(new MockedDataSource());
        return result;
    }
    
    private ShardingRule createShardingRule() {
        ShardingRuleConfiguration ruleConfig = new ShardingRuleConfiguration();
        ruleConfig.getTables().add(new ShardingTableRuleConfiguration("t_order0", "ds_0.t_order"));
        ruleConfig.getTables().add(new ShardingTableRuleConfiguration("t_order1", "ds_1.t_order"));
        ComputeNodeInstanceContext computeNodeInstanceContext = mock(ComputeNodeInstanceContext.class);
        when(computeNodeInstanceContext.getWorkerId()).thenReturn(0);
        Map<String, DataSource> dataSources = new LinkedHashMap<>(2, 1F);
        dataSources.put("ds_0", new MockedDataSource());
        dataSources.put("ds_1", new MockedDataSource());
        return new ShardingRule(ruleConfig, dataSources, computeNodeInstanceContext, Collections.emptyList());
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
