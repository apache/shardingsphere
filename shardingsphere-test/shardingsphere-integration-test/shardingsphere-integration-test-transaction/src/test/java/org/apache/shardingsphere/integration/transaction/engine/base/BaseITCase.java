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

package org.apache.shardingsphere.integration.transaction.engine.base;

import com.zaxxer.hikari.HikariDataSource;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.shardingsphere.data.pipeline.core.util.ThreadUtil;
import org.apache.shardingsphere.infra.database.metadata.url.JdbcUrlAppender;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.database.type.dialect.MySQLDatabaseType;
import org.apache.shardingsphere.infra.database.type.dialect.OpenGaussDatabaseType;
import org.apache.shardingsphere.infra.database.type.dialect.PostgreSQLDatabaseType;
import org.apache.shardingsphere.integration.transaction.engine.command.CommonSQLCommand;
import org.apache.shardingsphere.integration.transaction.engine.constants.TransactionTestConstants;
import org.apache.shardingsphere.integration.transaction.engine.entity.JdbcInfoEntity;
import org.apache.shardingsphere.integration.transaction.env.IntegrationTestEnvironment;
import org.apache.shardingsphere.integration.transaction.env.enums.TransactionITEnvTypeEnum;
import org.apache.shardingsphere.integration.transaction.env.enums.TransactionTestCaseRegistry;
import org.apache.shardingsphere.integration.transaction.framework.container.compose.BaseComposedContainer;
import org.apache.shardingsphere.integration.transaction.framework.container.compose.DockerComposedContainer;
import org.apache.shardingsphere.integration.transaction.framework.container.compose.NativeComposedContainer;
import org.apache.shardingsphere.integration.transaction.framework.container.database.DatabaseContainer;
import org.apache.shardingsphere.integration.transaction.framework.param.TransactionParameterized;
import org.apache.shardingsphere.integration.transaction.util.DatabaseTypeUtil;
import org.apache.shardingsphere.test.integration.env.DataSourceEnvironment;
import org.springframework.dao.DataAccessException;

import javax.sql.DataSource;
import javax.xml.bind.JAXB;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Objects;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

@Slf4j
@Getter(AccessLevel.PROTECTED)
public abstract class BaseITCase {
    
    protected static final IntegrationTestEnvironment ENV = IntegrationTestEnvironment.getInstance();
    
    protected static final JdbcUrlAppender JDBC_URL_APPENDER = new JdbcUrlAppender();
    
    protected static final String TRANSACTION_IT = "transaction_it";
    
    protected static final String DS_0 = TRANSACTION_IT + "_0";
    
    protected static final String DS_1 = TRANSACTION_IT + "_1";
    
    protected static final Collection<String> ALL_DS = Arrays.asList(DS_0, DS_1);
    
    protected static final String SHARDING_DB = "sharding_db";
    
    private final BaseComposedContainer composedContainer;
    
    private final CommonSQLCommand commonSQLCommand;
    
    private final DatabaseType databaseType;
    
    private DataSource dataSource;
    
    @Setter
    private Thread increaseTaskThread;
    
    public BaseITCase(final TransactionParameterized parameterized) {
        databaseType = parameterized.getDatabaseType();
        composedContainer = createAndStartComposedContainer(parameterized);
        commonSQLCommand = getSqlCommand();
        initActualDataSources();
        createProxyDatabase(parameterized.getDatabaseType());
    }
    
    protected static Collection<TransactionParameterized> getTransactionParameterizedList(final Class<? extends BaseTransactionITCase> testCaseClass) {
        TransactionTestCaseRegistry currentTestCaseInfo = ENV.getTransactionTestCaseRegistryMap().get(testCaseClass.getName());
        Collection<TransactionParameterized> result = new LinkedList<>();
        if (ENV.getItEnvType() == TransactionITEnvTypeEnum.NONE) {
            return result;
        }
        if (ENV.getItEnvType() == TransactionITEnvTypeEnum.DOCKER) {
            if (TransactionTestConstants.MYSQL.equalsIgnoreCase(currentTestCaseInfo.getDbType())) {
                for (String version : ENV.getMysqlVersions()) {
                    result.add(new TransactionParameterized(new MySQLDatabaseType(), getMysqlDockerImageName(version)));
                }
            } else if (TransactionTestConstants.POSTGRESQL.equalsIgnoreCase(currentTestCaseInfo.getDbType())) {
                for (String version : ENV.getPostgresVersions()) {
                    result.add(new TransactionParameterized(new PostgreSQLDatabaseType(), version));
                }
            } else if (TransactionTestConstants.OPENGAUSS.equalsIgnoreCase(currentTestCaseInfo.getDbType())) {
                for (String version : ENV.getOpenGaussVersions()) {
                    result.add(new TransactionParameterized(new OpenGaussDatabaseType(), version));
                }
            }
        }
        if (ENV.getItEnvType() == TransactionITEnvTypeEnum.NATIVE && StringUtils.equalsIgnoreCase(ENV.getNativeDatabaseType(), "MySQL")) {
            result.add(new TransactionParameterized(new MySQLDatabaseType(), ""));
        }
        return result;
    }
    
    protected static String getMysqlDockerImageName(final String version) {
        return "mysql/mysql-server:" + version;
    }
    
    private CommonSQLCommand getSqlCommand() {
        return JAXB.unmarshal(Objects.requireNonNull(BaseITCase.class.getClassLoader().getResource("env/common/command.xml")), CommonSQLCommand.class);
    }
    
    private BaseComposedContainer createAndStartComposedContainer(final TransactionParameterized parameterized) {
        final BaseComposedContainer composedContainer;
        if (ENV.getItEnvType() == TransactionITEnvTypeEnum.DOCKER) {
            composedContainer = new DockerComposedContainer(parameterized.getDatabaseType(), parameterized.getDockerImageName());
        } else {
            composedContainer = new NativeComposedContainer(parameterized.getDatabaseType());
        }
        composedContainer.start();
        return composedContainer;
    }
    
    @SneakyThrows(SQLException.class)
    private void initActualDataSources() {
        JdbcInfoEntity jdbcInfo = getJdbcInfoEntity();
        String jdbcUrl = getJdbcUrl(jdbcInfo);
        dropDatabases(jdbcUrl, jdbcInfo);
        createDatabases(jdbcUrl, jdbcInfo);
    }
    
    private String getJdbcUrl(final JdbcInfoEntity jdbcInfo) {
        String jdbcUrl;
        if (ENV.getItEnvType() == TransactionITEnvTypeEnum.DOCKER) {
            DockerComposedContainer dockerComposedContainer = (DockerComposedContainer) composedContainer;
            DatabaseContainer databaseContainer = dockerComposedContainer.getDatabaseContainer();
            jdbcUrl = databaseContainer.getJdbcUrl("");
        } else {
            jdbcUrl = DataSourceEnvironment.getURL(databaseType, "localhost", jdbcInfo.getPort());
        }
        return jdbcUrl;
    }
    
    private JdbcInfoEntity getJdbcInfoEntity() {
        JdbcInfoEntity jdbcInfo;
        if (ENV.getItEnvType() == TransactionITEnvTypeEnum.DOCKER) {
            DockerComposedContainer dockerComposedContainer = (DockerComposedContainer) composedContainer;
            DatabaseContainer databaseContainer = dockerComposedContainer.getDatabaseContainer();
            jdbcInfo = new JdbcInfoEntity(databaseContainer.getUsername(), databaseContainer.getPassword(), databaseContainer.getPort());
        } else {
            jdbcInfo = ENV.getActualDatabaseJdbcInfo(getDatabaseType());
        }
        return jdbcInfo;
    }
    
    private void dropDatabases(final String jdbcUrl, final JdbcInfoEntity jdbcInfo) throws SQLException {
        try (Connection connection = DriverManager.getConnection(jdbcUrl, jdbcInfo.getUsername(), jdbcInfo.getPassword())) {
            for (String each : ALL_DS) {
                executeWithLog(connection, String.format("DROP DATABASE IF EXISTS %s", each));
            }
        }
    }
    
    private void createDatabases(final String jdbcUrl, final JdbcInfoEntity jdbcInfo) throws SQLException {
        try (Connection connection = DriverManager.getConnection(jdbcUrl, jdbcInfo.getUsername(), jdbcInfo.getPassword())) {
            for (String each : ALL_DS) {
                try {
                    executeWithLog(connection, String.format("CREATE DATABASE %s", each));
                } catch (final SQLException ex) {
                    log.error("Error occurred when create database. error msg={}", ex.getMessage());
                }
            }
        }
    }
    
    @SneakyThrows(SQLException.class)
    protected Connection getProxyConnection() {
        String jdbcUrl = composedContainer.getProxyJdbcUrl(SHARDING_DB);
        return DriverManager.getConnection(jdbcUrl, "root", "root");
    }
    
    protected void createProxyDatabase(final DatabaseType databaseType) {
        String jdbcUrl = getProxyJdbcUrl(databaseType);
        try (Connection connection = DriverManager.getConnection(jdbcUrl, "root", "root")) {
            if (ENV.getItEnvType() == TransactionITEnvTypeEnum.NATIVE) {
                executeWithLog(connection, "DROP DATABASE IF EXISTS " + SHARDING_DB);
            }
            executeWithLog(connection, "CREATE DATABASE " + SHARDING_DB);
        } catch (final SQLException ex) {
            throw new IllegalStateException(ex);
        }
        dataSource = getProxyDataSource(SHARDING_DB);
    }
    
    private String getProxyJdbcUrl(final DatabaseType databaseType) {
        String defaultDatabaseName = "";
        if (DatabaseTypeUtil.isPostgreSQL(databaseType) || DatabaseTypeUtil.isOpenGauss(databaseType)) {
            defaultDatabaseName = "postgres";
        }
        String jdbcUrl = composedContainer.getProxyJdbcUrl(defaultDatabaseName);
        if (DatabaseTypeUtil.isPostgreSQL(databaseType) || DatabaseTypeUtil.isOpenGauss(databaseType)) {
            jdbcUrl = JDBC_URL_APPENDER.appendQueryProperties(jdbcUrl, getPostgreSQLQueryProperties());
        }
        return jdbcUrl;
    }
    
    protected DataSource getProxyDataSource(final String databaseName) {
        HikariDataSource result = new HikariDataSource();
        result.setDriverClassName(DataSourceEnvironment.getDriverClassName(getDatabaseType()));
        result.setJdbcUrl(composedContainer.getProxyJdbcUrl(databaseName));
        result.setUsername("root");
        result.setPassword("root");
        result.setMaximumPoolSize(2);
        result.setTransactionIsolation("TRANSACTION_READ_COMMITTED");
        return result;
    }
    
    protected boolean waitShardingAlgorithmEffect(final int maxWaitTimes) {
        long startTime = System.currentTimeMillis();
        int waitTimes = 0;
        do {
            int result = countWithLog("SHOW SHARDING ALGORITHMS");
            if (result >= 5) {
                log.info("waitShardingAlgorithmEffect time consume: {}", System.currentTimeMillis() - startTime);
                return true;
            }
            ThreadUtil.sleep(2, TimeUnit.SECONDS);
            waitTimes++;
        } while (waitTimes <= maxWaitTimes);
        return false;
    }
    
    @SneakyThrows
    protected void addResources() {
        if (databaseType instanceof MySQLDatabaseType) {
            try (Connection connection = DriverManager.getConnection(getComposedContainer().getProxyJdbcUrl(""), "root", "root")) {
                executeWithLog(connection, "USE sharding_db");
                addResources(connection);
            }
        } else {
            Properties queryProps = getPostgreSQLQueryProperties();
            try (Connection connection = DriverManager.getConnection(JDBC_URL_APPENDER.appendQueryProperties(getComposedContainer().getProxyJdbcUrl("sharding_db"), queryProps), "root", "root")) {
                addResources(connection);
            }
        }
        int resourceCount = countWithLog("SHOW DATABASE RESOURCES FROM sharding_db");
        assertThat(resourceCount, is(2));
    }
    
    protected void addResources(final Connection connection) throws SQLException {
        String addSourceResource = commonSQLCommand.getSourceAddResourceTemplate()
                .replace("${user}", ENV.getActualDataSourceUsername(databaseType))
                .replace("${password}", ENV.getActualDataSourcePassword(databaseType))
                .replace("${ds0}", getActualJdbcUrlTemplate(DS_0))
                .replace("${ds1}", getActualJdbcUrlTemplate(DS_1));
        executeWithLog(connection, addSourceResource);
    }
    
    private String getActualJdbcUrlTemplate(final String databaseName) {
        if (ENV.getItEnvType() == TransactionITEnvTypeEnum.DOCKER) {
            final DatabaseContainer databaseContainer = ((DockerComposedContainer) composedContainer).getDatabaseContainer();
            return DataSourceEnvironment.getURL(getDatabaseType(), "db.host", databaseContainer.getPort(), databaseName);
        } else {
            return DataSourceEnvironment.getURL(getDatabaseType(), "127.0.0.1", ENV.getActualDataSourceDefaultPort(databaseType), databaseName);
        }
    }
    
    protected void initShardingAlgorithm() throws SQLException {
        Connection connection = getProxyConnection();
        executeWithLog(connection, commonSQLCommand.getCreateDatabaseShardingAlgorithm());
        executeWithLog(connection, commonSQLCommand.getCreateDatabaseIdShardingAlgorithm());
        executeWithLog(connection, commonSQLCommand.getCreateOrderShardingAlgorithm());
        executeWithLog(connection, commonSQLCommand.getCreateOrderItemShardingAlgorithm());
        executeWithLog(connection, commonSQLCommand.getCreateAccountShardingAlgorithm());
    }
    
    protected void createSchema(final String schemaName) throws SQLException {
        Connection connection = getProxyConnection();
        executeWithLog(connection, String.format("CREATE SCHEMA %s", schemaName));
    }
    
    @SneakyThrows(SQLException.class)
    protected int countWithLog(final String sql) {
        Connection connection = getProxyConnection();
        int retryNumber = 0;
        while (retryNumber <= 3) {
            try {
                Statement statement = connection.createStatement();
                ResultSet rs = statement.executeQuery(sql);
                int result = 0;
                while (rs.next()) {
                    result++;
                }
                return result;
            } catch (final DataAccessException ex) {
                log.error("Data access error.", ex);
            }
            ThreadUtil.sleep(2, TimeUnit.SECONDS);
            retryNumber++;
        }
        throw new RuntimeException("can't get result from proxy");
    }
    
    protected void executeSqlListWithLog(final Connection conn, final String... sqlList) throws SQLException {
        for (String sql : sqlList) {
            conn.createStatement().execute(sql);
            ThreadUtil.sleep(1, TimeUnit.SECONDS);
        }
    }
    
    protected void executeWithLog(final Connection connection, final String sql) throws SQLException {
        log.info("Connection execute:{}.", sql);
        connection.createStatement().execute(sql);
        ThreadUtil.sleep(1, TimeUnit.SECONDS);
    }
    
    protected void executeUpdateWithLog(final Connection connection, final String sql) throws SQLException {
        log.info("Connection execute update:{}.", sql);
        connection.createStatement().executeUpdate(sql);
        ThreadUtil.sleep(1, TimeUnit.SECONDS);
    }
    
    /**
     * Get query properties by database type.
     *
     * @return query properties
     */
    public static Properties getPostgreSQLQueryProperties() {
        Properties result = new Properties();
        result.put("preferQueryMode", "extendedForPrepared");
        return result;
    }
}
