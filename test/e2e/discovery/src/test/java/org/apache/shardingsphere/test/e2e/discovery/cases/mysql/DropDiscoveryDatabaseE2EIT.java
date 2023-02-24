package org.apache.shardingsphere.test.e2e.discovery.cases.mysql;

import org.apache.shardingsphere.infra.database.type.dialect.MySQLDatabaseType;
import org.apache.shardingsphere.test.e2e.discovery.cases.DatabaseClusterEnvironmentFactory;
import org.apache.shardingsphere.test.e2e.discovery.cases.base.BaseDiscoveryE2EIT;
import org.apache.shardingsphere.test.e2e.discovery.command.DropDiscoveryDatabaseDistSQLCommand;
import org.apache.shardingsphere.test.e2e.discovery.framework.parameter.DiscoveryTestParameter;
import org.awaitility.Awaitility;
import org.awaitility.Durations;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import javax.xml.bind.JAXB;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Objects;

@RunWith(Parameterized.class)
public final class DropDiscoveryDatabaseE2EIT extends BaseDiscoveryE2EIT {
    
    private final DropDiscoveryDatabaseDistSQLCommand discoveryDistSQLCommand;
    
    public DropDiscoveryDatabaseE2EIT(final DiscoveryTestParameter testParam) {
        super(testParam);
        discoveryDistSQLCommand = JAXB.unmarshal(Objects.requireNonNull(BaseDiscoveryE2EIT.class.getClassLoader().getResource("env/common/drop-discovery-database-command.xml")),
                DropDiscoveryDatabaseDistSQLCommand.class);
    
    }
    
    @Parameterized.Parameters(name = "{0}")
    public static Collection<DiscoveryTestParameter> getTestParameters() {
        Collection<DiscoveryTestParameter> result = new LinkedList<>();
        MySQLDatabaseType databaseType = new MySQLDatabaseType();
        for (String each : ENV.listStorageContainerImages(databaseType)) {
            result.add(new DiscoveryTestParameter(databaseType, each, "drop_database_discovery"));
        }
        return result;
    }
    
    @Test
    public void assertDropDiscoveryDatabase() throws SQLException {
        DatabaseClusterEnvironmentFactory.newInstance("MySQL.MGR", getMappedDataSources());
        initDiscoveryEnvironment();
        dropDatabaseDiscoveryDatabase();
        createReadWriteSplittingDatabase();
        registerStorageUnit();
    }
    
    private void dropDatabaseDiscoveryDatabase() throws SQLException {
        try (
                Connection connection = getProxyDataSource().getConnection();
                Statement statement = connection.createStatement()) {
            statement.execute(discoveryDistSQLCommand.getDropDatabase().getExecuteSQL());
            Awaitility.await().atMost(Durations.FIVE_SECONDS).until(() -> assertDropSQL(statement, discoveryDistSQLCommand.getDropDatabase().getAssertionSQL()));
        }
    }
    
    private boolean assertDropSQL(final Statement statement, final String assertionSQL) {
        try (ResultSet resultSet = statement.executeQuery(assertionSQL)) {
            return false;
        } catch (final SQLException ignored) {
            return true;
        }
    }
    
    private void createReadWriteSplittingDatabase() throws SQLException {
        try (
                Connection connection = getProxyDataSource().getConnection();
                Statement statement = connection.createStatement()) {
            statement.execute(discoveryDistSQLCommand.getCreateDatabase().getExecuteSQL());
            Awaitility.await().atMost(Durations.FIVE_SECONDS).until(() -> assertCreateSQL(statement, discoveryDistSQLCommand.getCreateDatabase().getAssertionSQL()));
        }
    }
    
    private boolean assertCreateSQL(final Statement statement, final String assertionSQL) {
        try (ResultSet resultSet = statement.executeQuery(assertionSQL)) {
            return true;
        } catch (final SQLException ignored) {
            return false;
        }
    }
    
    private void registerStorageUnit() throws SQLException {
        try (
                Connection connection = getProxyDataSource().getConnection();
                Statement statement = connection.createStatement()) {
            statement.execute(discoveryDistSQLCommand.getRegisterStorageUnits().getExecuteSQL());
        }
    }
}
