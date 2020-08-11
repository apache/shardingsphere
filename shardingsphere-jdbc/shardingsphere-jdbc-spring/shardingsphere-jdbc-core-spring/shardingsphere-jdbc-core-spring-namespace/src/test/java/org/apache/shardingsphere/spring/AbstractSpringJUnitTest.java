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

package org.apache.shardingsphere.spring;

import lombok.Getter;
import lombok.SneakyThrows;
import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.shardingsphere.driver.jdbc.core.datasource.ShardingSphereDataSource;
import org.apache.shardingsphere.infra.config.RuleConfiguration;
import org.apache.shardingsphere.infra.database.DefaultSchema;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.kernel.context.SchemaContextsBuilder;
import org.h2.tools.RunScript;
import org.junit.Before;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;

import javax.annotation.Resource;
import javax.sql.DataSource;
import java.io.File;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@TestExecutionListeners(inheritListeners = false, listeners = {DependencyInjectionTestExecutionListener.class, DirtiesContextTestExecutionListener.class})
public abstract class AbstractSpringJUnitTest extends AbstractJUnit4SpringContextTests {
    
    @Resource
    @Getter
    private ShardingSphereDataSource shardingSphereDataSource;
    
    private final ClassLoader classLoader = AbstractSpringJUnitTest.class.getClassLoader();
    
    @Before
    public void createSchema() throws SQLException {
        for (String each : getSchemaFiles()) {
            RunScript.execute(createDataSource(each).getConnection(), new InputStreamReader(Objects.requireNonNull(classLoader.getResourceAsStream(each)), StandardCharsets.UTF_8));
        }
        reInitMetaData();
    }
    
    private DataSource createDataSource(final String dataSetFile) {
        BasicDataSource result = new BasicDataSource();
        result.setDriverClassName("org.h2.Driver");
        result.setUrl(String.format("jdbc:h2:mem:%s;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false;MODE=MYSQL", getFileName(dataSetFile)));
        result.setUsername("sa");
        result.setPassword("");
        result.setMaxTotal(100);
        return result;
    }
    
    private String getFileName(final String dataSetFile) {
        String fileName = new File(dataSetFile).getName();
        if (-1 == fileName.lastIndexOf('.')) {
            return fileName;
        }
        return fileName.substring(0, fileName.lastIndexOf('.'));
    }
    
    protected List<String> getSchemaFiles() {
        return Arrays.asList("schema/dbtbl_0.sql", "schema/dbtbl_1.sql");
    }
    
    @SneakyThrows(SQLException.class)
    private void reInitMetaData() {
        SchemaContextsBuilder schemaContextsBuilder = createSchemaContextsBuilder();
        setFieldValue(shardingSphereDataSource.getSchemaContexts().getDefaultSchemaContext().getSchema(), "metaData", 
                schemaContextsBuilder.build().getDefaultSchemaContext().getSchema().getMetaData());
    }
    
    private SchemaContextsBuilder createSchemaContextsBuilder() {
        Map<String, Map<String, DataSource>> dataSources = Collections.singletonMap(DefaultSchema.LOGIC_NAME, shardingSphereDataSource.getDataSourceMap());
        DatabaseType databaseType = shardingSphereDataSource.getSchemaContexts().getDatabaseType();
        Map<String, Collection<RuleConfiguration>> configurations = 
                Collections.singletonMap(DefaultSchema.LOGIC_NAME, shardingSphereDataSource.getSchemaContexts().getDefaultSchemaContext().getSchema().getConfigurations());
        return new SchemaContextsBuilder(dataSources, databaseType, configurations, shardingSphereDataSource.getSchemaContexts().getProps().getProps());
    }
    
    @SneakyThrows(ReflectiveOperationException.class)
    private void setFieldValue(final Object object, final String name, final Object value) {
        Optional<Field> field = getField(object, name);
        if (field.isPresent()) {
            field.get().setAccessible(true);
            field.get().set(object, value);
        }
    }
    
    private Optional<Field> getField(final Object object, final String name) {
        Class<?> clazz = object.getClass();
        Optional<Field> result = Optional.empty();
        while (!result.isPresent() && null != clazz) {
            try {
                result = Optional.of(clazz.getDeclaredField(name));
            } catch (final NoSuchFieldException ignored) {
            }
            clazz = clazz.getSuperclass();
        }
        return result;
    }
}
