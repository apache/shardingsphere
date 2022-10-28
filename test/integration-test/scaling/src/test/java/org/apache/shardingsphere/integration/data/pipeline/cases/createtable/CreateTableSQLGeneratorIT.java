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

package org.apache.shardingsphere.integration.data.pipeline.cases.createtable;

import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.data.pipeline.spi.ddlgenerator.CreateTableSQLGeneratorFactory;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.database.type.dialect.MySQLDatabaseType;
import org.apache.shardingsphere.infra.database.type.dialect.OpenGaussDatabaseType;
import org.apache.shardingsphere.infra.database.type.dialect.PostgreSQLDatabaseType;
import org.apache.shardingsphere.integration.data.pipeline.entity.CreateTableSQLGeneratorAssertionEntity;
import org.apache.shardingsphere.integration.data.pipeline.entity.CreateTableSQLGeneratorAssertionsRootEntity;
import org.apache.shardingsphere.integration.data.pipeline.entity.CreateTableSQLGeneratorOutputEntity;
import org.apache.shardingsphere.integration.data.pipeline.env.IntegrationTestEnvironment;
import org.apache.shardingsphere.integration.data.pipeline.env.enums.ITEnvTypeEnum;
import org.apache.shardingsphere.integration.data.pipeline.framework.param.ScalingParameterized;
import org.apache.shardingsphere.integration.data.pipeline.util.DockerImageVersion;
import org.apache.shardingsphere.test.integration.env.container.atomic.constants.StorageContainerConstants;
import org.apache.shardingsphere.test.integration.env.container.atomic.storage.DockerStorageContainer;
import org.apache.shardingsphere.test.integration.env.container.atomic.storage.StorageContainerFactory;
import org.apache.shardingsphere.test.integration.env.container.atomic.storage.config.StorageContainerConfiguration;
import org.apache.shardingsphere.test.integration.env.container.atomic.storage.config.impl.StorageContainerConfigurationFactory;
import org.apache.shardingsphere.test.integration.env.container.atomic.storage.config.impl.mysql.MySQLContainerConfigurationFactory;
import org.apache.shardingsphere.test.integration.env.container.atomic.util.DatabaseTypeUtil;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import javax.sql.DataSource;
import javax.xml.bind.JAXB;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Objects;
import java.util.regex.Pattern;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

@RunWith(Parameterized.class)
@Slf4j
public final class CreateTableSQLGeneratorIT {
    
    private static final String POSTGRES_CASE_FILE_PATH = "postgresql/create-table-sql-generator.xml";
    
    private static final String MYSQL_CASE_FILE_PATH = "mysql/create-table-sql-generator.xml";
    
    private static final String OPEN_GAUSS_CASE_FILE_PATH = "opengauss/create-table-sql-generator.xml";
    
    private static final String PARENT_PATH = "env/scenario/createtablegenerator";
    
    private static final String DEFAULT_SCHEMA = "public";
    
    private static final String DEFAULT_DATABASE = "ds";
    
    private static final Pattern REPLACE_LINE_SPACE = Pattern.compile("\\s*|\t|\r|\n");
    
    private static final IntegrationTestEnvironment ENV = IntegrationTestEnvironment.getInstance();
    
    private final DockerStorageContainer storageContainer;
    
    private final ScalingParameterized parameterized;
    
    private final CreateTableSQLGeneratorAssertionsRootEntity rootEntity;
    
    public CreateTableSQLGeneratorIT(final ScalingParameterized parameterized) {
        this.parameterized = parameterized;
        rootEntity = JAXB.unmarshal(
                Objects.requireNonNull(CreateTableSQLGeneratorIT.class.getClassLoader().getResource(parameterized.getScenario())), CreateTableSQLGeneratorAssertionsRootEntity.class);
        DatabaseType databaseType = parameterized.getDatabaseType();
        StorageContainerConfiguration storageContainerConfig = DatabaseTypeUtil.isMySQL(databaseType) && new DockerImageVersion(parameterized.getStorageContainerImage()).getMajorVersion() > 5
                ? MySQLContainerConfigurationFactory.newInstance(null, null, Collections.singletonMap("/env/mysql/mysql8/my.cnf", StorageContainerConstants.MYSQL_CONF_IN_CONTAINER))
                : StorageContainerConfigurationFactory.newInstance(databaseType);
        storageContainer = (DockerStorageContainer) StorageContainerFactory.newInstance(databaseType, parameterized.getStorageContainerImage(), "",
                storageContainerConfig);
        storageContainer.start();
    }
    
    @Parameters(name = "{0}")
    public static Collection<ScalingParameterized> getParameters() {
        Collection<ScalingParameterized> result = new LinkedList<>();
        if (ENV.getItEnvType() == ITEnvTypeEnum.NONE) {
            return result;
        }
        for (String each : ENV.getPostgresVersions()) {
            result.add(new ScalingParameterized(new PostgreSQLDatabaseType(), each, String.join("/", PARENT_PATH, POSTGRES_CASE_FILE_PATH)));
        }
        for (String each : ENV.getMysqlVersions()) {
            result.add(new ScalingParameterized(new MySQLDatabaseType(), each, String.join("/", PARENT_PATH, MYSQL_CASE_FILE_PATH)));
        }
        for (String each : ENV.getOpenGaussVersions()) {
            result.add(new ScalingParameterized(new OpenGaussDatabaseType(), each, String.join("/", PARENT_PATH, OPEN_GAUSS_CASE_FILE_PATH)));
        }
        return result;
    }
    
    @Test
    public void assertGenerateCreateTableSQL() throws SQLException {
        log.info("generate create table sql, parameterized: {}", parameterized);
        initData();
        DataSource dataSource = storageContainer.createAccessDataSource(DEFAULT_DATABASE);
        try (
                Connection connection = dataSource.getConnection();
                Statement statement = connection.createStatement()) {
            int majorVersion = connection.getMetaData().getDatabaseMajorVersion();
            for (CreateTableSQLGeneratorAssertionEntity each : rootEntity.getAssertions()) {
                statement.execute(each.getInput().getSql());
                Collection<String> actualDDLs = CreateTableSQLGeneratorFactory.getInstance(parameterized.getDatabaseType()).generate(dataSource, DEFAULT_SCHEMA, each.getInput().getTable());
                assertIsCorrect(actualDDLs, getVersionOutput(each.getOutputs(), majorVersion));
            }
        }
    }
    
    private void initData() throws SQLException {
        try (Statement statement = storageContainer.createAccessDataSource("").getConnection().createStatement()) {
            statement.execute("CREATE DATABASE " + DEFAULT_DATABASE);
        }
    }
    
    private void assertIsCorrect(final Collection<String> actualSQL, final Collection<String> expectedSQL) {
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
    
    @After
    public void stopContainer() {
        storageContainer.stop();
    }
}
