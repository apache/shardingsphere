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

package org.apache.shardingsphere.integration.data.pipeline.cases.postgresql;

import com.google.common.base.Strings;
import com.zaxxer.hikari.HikariDataSource;
import org.apache.shardingsphere.data.pipeline.spi.ddlgenerator.CreateTableSQLGeneratorFactory;
import org.apache.shardingsphere.infra.database.type.dialect.PostgreSQLDatabaseType;
import org.apache.shardingsphere.infra.exception.ShardingSphereException;
import org.apache.shardingsphere.integration.data.pipeline.cases.entity.DDLGeneratorAssertionEntity;
import org.apache.shardingsphere.integration.data.pipeline.cases.entity.DDLGeneratorAssertionsRootEntity;
import org.apache.shardingsphere.integration.data.pipeline.cases.entity.DDLGeneratorOutputEntity;
import org.apache.shardingsphere.integration.data.pipeline.env.IntegrationTestEnvironment;
import org.apache.shardingsphere.integration.data.pipeline.factory.DatabaseContainerFactory;
import org.apache.shardingsphere.integration.data.pipeline.framework.container.database.DockerDatabaseContainer;
import org.apache.shardingsphere.integration.data.pipeline.framework.param.ScalingParameterized;
import org.apache.shardingsphere.test.integration.env.DataSourceEnvironment;
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
import java.util.LinkedList;
import java.util.Objects;
import java.util.regex.Pattern;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

@RunWith(Parameterized.class)
public final class PostgreSQLDDLGeneratorIT {
    
    private static final String CASE_FILE_PATH = "ddlgenerator.xml";
    
    private static final String PARENT_PATH = "env/scenario/ddlgenerator/postgresql";
    
    private static final String DEFAULT_SCHEMA = "public";
    
    private static final Pattern REPLACE_LINE_SPACE = Pattern.compile("\\s*|\t|\r|\n");
    
    private static final IntegrationTestEnvironment ENV = IntegrationTestEnvironment.getInstance();
    
    private final DockerDatabaseContainer dockerDatabaseContainer;
    
    private final ScalingParameterized parameterized;
    
    private final DDLGeneratorAssertionsRootEntity rootEntity;
    
    public PostgreSQLDDLGeneratorIT(final ScalingParameterized parameterized) {
        this.parameterized = parameterized;
        this.rootEntity = JAXB.unmarshal(Objects.requireNonNull(PostgreSQLDDLGeneratorIT.class.getClassLoader().getResource(parameterized.getScenario())),
                DDLGeneratorAssertionsRootEntity.class);
        this.dockerDatabaseContainer = DatabaseContainerFactory.newInstance(parameterized.getDatabaseType(), parameterized.getDockerImageName());
        dockerDatabaseContainer.start();
    }
    
    @Parameters(name = "{0}")
    public static Collection<ScalingParameterized> getParameters() {
        Collection<ScalingParameterized> result = new LinkedList<>();
        for (String each : ENV.getPostgresVersions()) {
            if (!Strings.isNullOrEmpty(each)) {
                result.add(new ScalingParameterized(new PostgreSQLDatabaseType(), each, String.join("/", PARENT_PATH, CASE_FILE_PATH)));
            }
        }
        return result;
    }
    
    @Test
    public void assertGenerateDDLSQL() throws SQLException {
        DataSource dataSource = createDataSource();
        try (
                Connection connection = dataSource.getConnection();
                Statement statement = connection.createStatement()) {
            int majorVersion = connection.getMetaData().getDatabaseMajorVersion();
            for (DDLGeneratorAssertionEntity each : rootEntity.getAssertions()) {
                statement.execute(each.getInput().getSql());
                String sql = CreateTableSQLGeneratorFactory.findInstance(parameterized.getDatabaseType()).orElseThrow(() -> new ShardingSphereException("Failed to get dialect ddl sql generator"))
                        .generate(each.getInput().getTable(), DEFAULT_SCHEMA, dataSource);
                assertThat(REPLACE_LINE_SPACE.matcher(sql).replaceAll(""), is(REPLACE_LINE_SPACE.matcher(getVersionOutput(each.getOutputs(), majorVersion)).replaceAll("")));
            }
        }
    }
    
    private String getVersionOutput(final Collection<DDLGeneratorOutputEntity> outputs, final int majorVersion) {
        String result = "";
        for (DDLGeneratorOutputEntity each : outputs) {
            if ("default".equals(each.getVersion())) {
                result = each.getSql();
            }
            if (String.valueOf(majorVersion).equals(each.getVersion())) {
                return each.getSql();
            }
        }
        return result;
    }
    
    private DataSource createDataSource() {
        HikariDataSource result = new HikariDataSource();
        result.setDriverClassName(DataSourceEnvironment.getDriverClassName(dockerDatabaseContainer.getDatabaseType()));
        result.setJdbcUrl(dockerDatabaseContainer.getJdbcUrl(dockerDatabaseContainer.getHost(), dockerDatabaseContainer.getFirstMappedPort(), "postgres"));
        result.setUsername("root");
        result.setPassword("root");
        result.setMaximumPoolSize(2);
        result.setTransactionIsolation("TRANSACTION_READ_COMMITTED");
        return result;
    }
    
    @After
    public void stopContainer() {
        dockerDatabaseContainer.stop();
    }
}
