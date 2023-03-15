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

package org.apache.shardingsphere.test.e2e.data.pipeline.cases.createtable;

import org.apache.shardingsphere.data.pipeline.spi.ddlgenerator.CreateTableSQLGenerator;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.database.type.dialect.MySQLDatabaseType;
import org.apache.shardingsphere.infra.database.type.dialect.OpenGaussDatabaseType;
import org.apache.shardingsphere.infra.database.type.dialect.PostgreSQLDatabaseType;
import org.apache.shardingsphere.infra.util.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.test.e2e.data.pipeline.cases.PipelineE2ECondition;
import org.apache.shardingsphere.test.e2e.data.pipeline.entity.CreateTableSQLGeneratorAssertionEntity;
import org.apache.shardingsphere.test.e2e.data.pipeline.entity.CreateTableSQLGeneratorAssertionsRootEntity;
import org.apache.shardingsphere.test.e2e.data.pipeline.entity.CreateTableSQLGeneratorOutputEntity;
import org.apache.shardingsphere.test.e2e.data.pipeline.env.PipelineE2EEnvironment;
import org.apache.shardingsphere.test.e2e.data.pipeline.framework.param.PipelineTestParameter;
import org.apache.shardingsphere.test.e2e.data.pipeline.util.DockerImageVersion;
import org.apache.shardingsphere.test.e2e.env.container.atomic.storage.DockerStorageContainer;
import org.apache.shardingsphere.test.e2e.env.container.atomic.storage.StorageContainerFactory;
import org.apache.shardingsphere.test.e2e.env.container.atomic.storage.config.StorageContainerConfiguration;
import org.apache.shardingsphere.test.e2e.env.container.atomic.storage.config.impl.StorageContainerConfigurationFactory;
import org.apache.shardingsphere.test.e2e.env.container.atomic.storage.config.impl.mysql.MySQLContainerConfigurationFactory;
import org.apache.shardingsphere.test.e2e.env.container.atomic.storage.impl.MySQLContainer;
import org.apache.shardingsphere.test.e2e.env.container.atomic.util.DatabaseTypeUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.condition.EnabledIf;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;

import javax.sql.DataSource;
import javax.xml.bind.JAXB;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public final class CreateTableSQLGeneratorIT {
    
    private static final String POSTGRES_CASE_FILE_PATH = "postgresql/create-table-sql-generator.xml";
    
    private static final String MYSQL_CASE_FILE_PATH = "mysql/create-table-sql-generator.xml";
    
    private static final String OPEN_GAUSS_CASE_FILE_PATH = "opengauss/create-table-sql-generator.xml";
    
    private static final String PARENT_PATH = "env/scenario/create-table-generator";
    
    private static final String DEFAULT_SCHEMA = "public";
    
    private static final String DEFAULT_DATABASE = "pipeline_it_0";
    
    private static final Pattern REPLACE_LINE_SPACE = Pattern.compile("\\s*|\t|\r|\n");
    
    private DockerStorageContainer storageContainer;
    
    @AfterEach
    public void stopContainer() {
        storageContainer.stop();
    }
    
    @ParameterizedTest(name = "{0}")
    @EnabledIf("isEnabled")
    @ArgumentsSource(TestCaseArgumentsProvider.class)
    public void assertGenerateCreateTableSQL(final PipelineTestParameter testParam) throws SQLException {
        startStorageContainer(testParam.getDatabaseType(), testParam.getStorageContainerImage());
        CreateTableSQLGeneratorAssertionsRootEntity rootEntity = JAXB.unmarshal(
                Objects.requireNonNull(CreateTableSQLGeneratorIT.class.getClassLoader().getResource(testParam.getScenario())), CreateTableSQLGeneratorAssertionsRootEntity.class);
        DataSource dataSource = storageContainer.createAccessDataSource(DEFAULT_DATABASE);
        try (
                Connection connection = dataSource.getConnection();
                Statement statement = connection.createStatement()) {
            int majorVersion = connection.getMetaData().getDatabaseMajorVersion();
            for (CreateTableSQLGeneratorAssertionEntity each : rootEntity.getAssertions()) {
                statement.execute(each.getInput().getSql());
                Collection<String> actualDDLs = TypedSPILoader.getService(
                        CreateTableSQLGenerator.class, testParam.getDatabaseType().getType()).generate(dataSource, DEFAULT_SCHEMA, each.getInput().getTable());
                assertSQL(actualDDLs, getVersionOutput(each.getOutputs(), majorVersion));
            }
        }
    }
    
    private void startStorageContainer(final DatabaseType databaseType, final String storageContainerImage) {
        StorageContainerConfiguration storageContainerConfig = createStorageContainerConfiguration(databaseType, storageContainerImage);
        storageContainer = (DockerStorageContainer) StorageContainerFactory.newInstance(databaseType, storageContainerImage, "", storageContainerConfig);
        storageContainer.start();
    }
    
    private StorageContainerConfiguration createStorageContainerConfiguration(final DatabaseType databaseType, final String storageContainerImage) {
        if (DatabaseTypeUtil.isMySQL(databaseType)) {
            int majorVersion = new DockerImageVersion(storageContainerImage).getMajorVersion();
            Map<String, String> mountedResources = Collections.singletonMap(String.format("/env/mysql/mysql%s/my.cnf", majorVersion), MySQLContainer.MYSQL_CONF_IN_CONTAINER);
            return MySQLContainerConfigurationFactory.newInstance(null, null, mountedResources);
        }
        return StorageContainerConfigurationFactory.newInstance(databaseType);
    }
    
    private void assertSQL(final Collection<String> actualSQL, final Collection<String> expectedSQL) {
        Iterator<String> expected = expectedSQL.iterator();
        for (String each : actualSQL) {
            assertThat(REPLACE_LINE_SPACE.matcher(each).replaceAll(""), is(REPLACE_LINE_SPACE.matcher(expected.next()).replaceAll("")));
        }
    }
    
    private Collection<String> getVersionOutput(final Collection<CreateTableSQLGeneratorOutputEntity> outputs, final int majorVersion) {
        Collection<String> result = new LinkedList<>();
        for (CreateTableSQLGeneratorOutputEntity each : outputs) {
            if ("default".equals(each.getVersion())) {
                result = each.getMultiSQL();
            }
            if (String.valueOf(majorVersion).equals(each.getVersion())) {
                return each.getMultiSQL();
            }
        }
        return result;
    }
    
    private static boolean isEnabled() {
        return PipelineE2ECondition.isEnabled(new MySQLDatabaseType(), new PostgreSQLDatabaseType(), new OpenGaussDatabaseType());
    }
    
    private static class TestCaseArgumentsProvider implements ArgumentsProvider {
        
        @Override
        public Stream<? extends Arguments> provideArguments(final ExtensionContext extensionContext) {
            Collection<PipelineTestParameter> result = new LinkedList<>();
            for (String each : PipelineE2EEnvironment.getInstance().listStorageContainerImages(new MySQLDatabaseType())) {
                result.add(new PipelineTestParameter(new MySQLDatabaseType(), each, String.join("/", PARENT_PATH, MYSQL_CASE_FILE_PATH)));
            }
            for (String each : PipelineE2EEnvironment.getInstance().listStorageContainerImages(new PostgreSQLDatabaseType())) {
                result.add(new PipelineTestParameter(new PostgreSQLDatabaseType(), each, String.join("/", PARENT_PATH, POSTGRES_CASE_FILE_PATH)));
            }
            for (String each : PipelineE2EEnvironment.getInstance().listStorageContainerImages(new OpenGaussDatabaseType())) {
                result.add(new PipelineTestParameter(new OpenGaussDatabaseType(), each, String.join("/", PARENT_PATH, OPEN_GAUSS_CASE_FILE_PATH)));
            }
            return result.stream().map(Arguments::of);
        }
    }
}
