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

package org.apache.shardingsphere.test.e2e.operation.pipeline.cases.createtable;

import org.apache.shardingsphere.data.pipeline.core.sqlbuilder.dialect.DialectPipelineSQLBuilder;
import org.apache.shardingsphere.database.connector.core.spi.DatabaseTypedSPILoader;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.test.e2e.env.container.storage.option.StorageContainerOption;
import org.apache.shardingsphere.test.e2e.env.container.storage.type.DockerStorageContainer;
import org.apache.shardingsphere.test.e2e.operation.pipeline.entity.CreateTableSQLGeneratorAssertionEntity;
import org.apache.shardingsphere.test.e2e.operation.pipeline.entity.CreateTableSQLGeneratorAssertionsRootEntity;
import org.apache.shardingsphere.test.e2e.operation.pipeline.entity.CreateTableSQLGeneratorOutputEntity;
import org.apache.shardingsphere.test.e2e.operation.pipeline.framework.param.PipelineE2ECondition;
import org.apache.shardingsphere.test.e2e.operation.pipeline.framework.param.PipelineE2ESettings;
import org.apache.shardingsphere.test.e2e.operation.pipeline.framework.param.PipelineE2ESettings.PipelineE2EDatabaseSettings;
import org.apache.shardingsphere.test.e2e.operation.pipeline.framework.param.PipelineE2ETestCaseArgumentsProvider;
import org.apache.shardingsphere.test.e2e.operation.pipeline.framework.param.PipelineTestParameter;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.condition.EnabledIf;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;

import javax.sql.DataSource;
import javax.xml.bind.JAXB;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Objects;
import java.util.regex.Pattern;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

@PipelineE2ESettings(database = {
        @PipelineE2EDatabaseSettings(type = "MySQL", scenarioFiles = "env/common/none.xml"),
        @PipelineE2EDatabaseSettings(type = "PostgreSQL", scenarioFiles = "env/common/none.xml"),
        @PipelineE2EDatabaseSettings(type = "openGauss", scenarioFiles = "env/common/none.xml")})
class CreateTableSQLGeneratorIT {
    
    private static final String DEFAULT_SCHEMA = "public";
    
    private static final String DEFAULT_DATABASE = "pipeline_e2e_0";
    
    private static final Pattern REPLACE_LINE_SPACE = Pattern.compile("\\s*|\t|\r|\n");
    
    private DockerStorageContainer storageContainer;
    
    @AfterEach
    void stopContainer() {
        storageContainer.stop();
    }
    
    @ParameterizedTest(name = "{0}")
    @EnabledIf("isEnabled")
    @ArgumentsSource(PipelineE2ETestCaseArgumentsProvider.class)
    void assertGenerateCreateTableSQL(final PipelineTestParameter testParam) throws SQLException {
        startStorageContainer(testParam.getDatabaseType(), testParam.getDatabaseContainerImage());
        String resourcePath = String.format("env/scenario/create-table-generator/%s/create-table-sql-generator.xml", testParam.getDatabaseType().getType().toLowerCase());
        CreateTableSQLGeneratorAssertionsRootEntity rootEntity = JAXB.unmarshal(
                Objects.requireNonNull(Thread.currentThread().getContextClassLoader().getResource(resourcePath)), CreateTableSQLGeneratorAssertionsRootEntity.class);
        DataSource dataSource = storageContainer.createAccessDataSource(DEFAULT_DATABASE);
        try (
                Connection connection = dataSource.getConnection();
                Statement statement = connection.createStatement()) {
            int majorVersion = connection.getMetaData().getDatabaseMajorVersion();
            for (CreateTableSQLGeneratorAssertionEntity each : rootEntity.getAssertions()) {
                statement.execute(each.getInput().getSql());
                DialectPipelineSQLBuilder sqlBuilder = DatabaseTypedSPILoader.getService(DialectPipelineSQLBuilder.class, testParam.getDatabaseType());
                Collection<String> actualDDLs = sqlBuilder.buildCreateTableSQLs(dataSource, DEFAULT_SCHEMA, each.getInput().getTable());
                assertSQL(actualDDLs, getVersionOutput(each.getOutputs(), majorVersion));
            }
        }
    }
    
    private void startStorageContainer(final DatabaseType databaseType, final String databaseContainerImage) {
        storageContainer = new DockerStorageContainer(databaseContainerImage, DatabaseTypedSPILoader.getService(StorageContainerOption.class, databaseType), null);
        storageContainer.start();
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
    
    private static boolean isEnabled(final ExtensionContext context) {
        return PipelineE2ECondition.isEnabled(context);
    }
}
