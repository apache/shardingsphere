package org.apache.shardingsphere.infra.database.metadata;

import org.apache.shardingsphere.infra.database.metadata.dialect.H2DataSourceMetaData;
import org.apache.shardingsphere.infra.database.metadata.dialect.MariaDBDataSourceMetaData;
import org.apache.shardingsphere.infra.database.metadata.dialect.MySQLDataSourceMetaData;
import org.apache.shardingsphere.infra.database.metadata.dialect.OracleDataSourceMetaData;
import org.apache.shardingsphere.infra.database.metadata.dialect.PostgreSQLDataSourceMetaData;
import org.apache.shardingsphere.infra.database.metadata.dialect.SQL92DataSourceMetaData;
import org.apache.shardingsphere.infra.database.metadata.dialect.SQLServerDataSourceMetaData;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public final class DataSourceMetaDataTest {

    @Test
    public void assertIsInSameDatabaseInstanceWithMysql() {
        MySQLDataSourceMetaData actual1 = new MySQLDataSourceMetaData("jdbc:mysql://127.0.0.1:9999/ds_1?serverTimezone=UTC&useSSL=false");
        MySQLDataSourceMetaData actual2 = new MySQLDataSourceMetaData("jdbc:mysql://127.0.0.1:9999/ds_0?serverTimezone=UTC&useSSL=false");
        assertTrue(actual1.isInSameDatabaseInstance(actual2));
    }

    @Test
    public void assertIsInSameDatabaseInstanceWithOracle() {
        OracleDataSourceMetaData actual1 = new OracleDataSourceMetaData("jdbc:oracle:thin:@//127.0.0.1:9999/ds_0", "test");
        OracleDataSourceMetaData actual2 = new OracleDataSourceMetaData("jdbc:oracle:thin:@//127.0.0.1:9999/ds_1", "test");
        assertTrue(actual1.isInSameDatabaseInstance(actual2));
    }

    @Test
    public void assertIsInSameDatabaseInstanceWithMariaDB() {
        MariaDBDataSourceMetaData actual1 = new MariaDBDataSourceMetaData("jdbc:mariadb://127.0.0.1:9999/ds_0?serverTimezone=UTC&useSSL=false");
        MariaDBDataSourceMetaData actual2 = new MariaDBDataSourceMetaData("jdbc:mariadb://127.0.0.1:9999/ds_1?serverTimezone=UTC&useSSL=false");
        assertTrue(actual1.isInSameDatabaseInstance(actual2));
    }

    @Test
    public void assertIsInSameDatabaseInstanceWithPostgreSQL() {
        PostgreSQLDataSourceMetaData actual1 = new PostgreSQLDataSourceMetaData("jdbc:postgresql://127.0.0.1/ds_0");
        PostgreSQLDataSourceMetaData actual2 = new PostgreSQLDataSourceMetaData("jdbc:postgresql://127.0.0.1/ds_1");
        assertTrue(actual1.isInSameDatabaseInstance(actual2));
    }

    @Test
    public void assertIsInSameDatabaseInstanceWithSQL92() {
        SQL92DataSourceMetaData actual1 = new SQL92DataSourceMetaData("jdbc:sql92_db:ds_0");
        SQL92DataSourceMetaData actual2 = new SQL92DataSourceMetaData("jdbc:sql92_db:ds_1");
        assertTrue(actual1.isInSameDatabaseInstance(actual2));
    }

    @Test
    public void assertIsInSameDatabaseInstanceWithSQLServer() {
        SQLServerDataSourceMetaData actual1 = new SQLServerDataSourceMetaData("jdbc:microsoft:sqlserver://127.0.0.1:9999;DatabaseName=ds_0");
        SQLServerDataSourceMetaData actual2 = new SQLServerDataSourceMetaData("jdbc:microsoft:sqlserver://127.0.0.1:9999;DatabaseName=ds_1");
        assertTrue(actual1.isInSameDatabaseInstance(actual2));
    }

    @Test
    public void assertFalseIsInSameDatabaseInstanceWithMysqlAndH2() {
        H2DataSourceMetaData actualH2 = new H2DataSourceMetaData("jdbc:h2:mem:ds_0;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false;MODE=MySQL");
        MySQLDataSourceMetaData actualMysql = new MySQLDataSourceMetaData("jdbc:mysql://127.0.0.1:9999/ds_0?serverTimezone=UTC&useSSL=false");
        assertFalse(actualMysql.isInSameDatabaseInstance(actualH2));
    }

    @Test
    public void assertFalseIsInSameDatabaseInstanceWithMysqlAndOracle() {
        OracleDataSourceMetaData actualOracle = new OracleDataSourceMetaData("jdbc:oracle:thin:@//127.0.0.1:9988/ds_0", "test");
        MySQLDataSourceMetaData actualMysql = new MySQLDataSourceMetaData("jdbc:mysql://127.0.0.1:9999/ds_0?serverTimezone=UTC&useSSL=false");
        assertFalse(actualMysql.isInSameDatabaseInstance(actualOracle));
    }

    @Test
    public void assertFalseInSameDatabaseInstanceWithMysqlAndMariaDB() {
        MySQLDataSourceMetaData actualMysql = new MySQLDataSourceMetaData("jdbc:mysql://127.0.0.1:9988/ds_0?serverTimezone=UTC&useSSL=false");
        MariaDBDataSourceMetaData actualMariaDB = new MariaDBDataSourceMetaData("jdbc:mariadb://127.0.0.1:9999/ds_0?serverTimezone=UTC&useSSL=false");
        assertFalse(actualMysql.isInSameDatabaseInstance(actualMariaDB));
    }

    @Test
    public void assertFalseInSameDatabaseInstanceWithMysqlAndPostgreSQL() {
        MySQLDataSourceMetaData actualMysql = new MySQLDataSourceMetaData("jdbc:mysql://127.0.0.1:9988/ds_0?serverTimezone=UTC&useSSL=false");
        PostgreSQLDataSourceMetaData actualPostgreSQL = new PostgreSQLDataSourceMetaData("jdbc:postgresql://127.0.0.1/ds_0");
        assertFalse(actualMysql.isInSameDatabaseInstance(actualPostgreSQL));
    }

    @Test
    public void assertFalseInSameDatabaseInstanceWithMysqlAndSQL92() {
        MySQLDataSourceMetaData actualMysql = new MySQLDataSourceMetaData("jdbc:mysql://127.0.0.1:9988/ds_0?serverTimezone=UTC&useSSL=false");
        SQL92DataSourceMetaData actualSQL92 = new SQL92DataSourceMetaData("jdbc:sql92_db:ds_0");
        assertFalse(actualMysql.isInSameDatabaseInstance(actualSQL92));
    }

    @Test
    public void assertFalseInSameDatabaseInstanceWithMysqlAndSQLServer() {
        MySQLDataSourceMetaData actualMysql = new MySQLDataSourceMetaData("jdbc:mysql://127.0.0.1:9988/ds_0?serverTimezone=UTC&useSSL=false");
        SQLServerDataSourceMetaData actualSQLServer = new SQLServerDataSourceMetaData("jdbc:microsoft:sqlserver://127.0.0.1:9999;DatabaseName=ds_1");
        assertFalse(actualMysql.isInSameDatabaseInstance(actualSQLServer));
    }

    @Test
    public void assertFalseInSameDatabaseInstanceWithH2AndMariaDB() {
        H2DataSourceMetaData actualH2 = new H2DataSourceMetaData("jdbc:h2:mem:ds_0;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false;MODE=MySQL");
        MariaDBDataSourceMetaData actualMariaDB = new MariaDBDataSourceMetaData("jdbc:mariadb://127.0.0.1:9999/ds_0?serverTimezone=UTC&useSSL=false");
        assertFalse(actualH2.isInSameDatabaseInstance(actualMariaDB));
    }

    @Test
    public void assertFalseInSameDatabaseInstanceWithH2AndOracle() {
        H2DataSourceMetaData actualH2 = new H2DataSourceMetaData("jdbc:h2:mem:ds_0;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false;MODE=MySQL");
        OracleDataSourceMetaData actualOracle = new OracleDataSourceMetaData("jdbc:oracle:thin:@//127.0.0.1:9988/ds_0", "test");
        assertFalse(actualH2.isInSameDatabaseInstance(actualOracle));
    }

    @Test
    public void assertFalseInSameDatabaseInstanceWithH2AndPostgreSQL() {
        H2DataSourceMetaData actualH2 = new H2DataSourceMetaData("jdbc:h2:mem:ds_0;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false;MODE=MySQL");
        PostgreSQLDataSourceMetaData actualPostgreSQL = new PostgreSQLDataSourceMetaData("jdbc:postgresql://127.0.0.1/ds_0");
        assertFalse(actualH2.isInSameDatabaseInstance(actualPostgreSQL));
    }

    @Test
    public void assertFalseInSameDatabaseInstanceWithH2AndSQL92() {
        H2DataSourceMetaData actualH2 = new H2DataSourceMetaData("jdbc:h2:mem:ds_0;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false;MODE=MySQL");
        SQL92DataSourceMetaData actualSQL92 = new SQL92DataSourceMetaData("jdbc:sql92_db:ds_0");
        assertFalse(actualH2.isInSameDatabaseInstance(actualSQL92));
    }

    @Test
    public void assertFalseInSameDatabaseInstanceWithH2AndSQLServer() {
        H2DataSourceMetaData actualH2 = new H2DataSourceMetaData("jdbc:h2:mem:ds_0;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false;MODE=MySQL");
        SQLServerDataSourceMetaData actualSQLServer = new SQLServerDataSourceMetaData("jdbc:microsoft:sqlserver://127.0.0.1:9999;DatabaseName=ds_1");
        assertFalse(actualH2.isInSameDatabaseInstance(actualSQLServer));
    }

    @Test
    public void assertFalseInSameDatabaseInstanceWithOracleAndMariaDB() {
        MariaDBDataSourceMetaData actualMariaDB = new MariaDBDataSourceMetaData("jdbc:mariadb://127.0.0.1:9999/ds_0?serverTimezone=UTC&useSSL=false");
        OracleDataSourceMetaData actualOracle = new OracleDataSourceMetaData("jdbc:oracle:thin:@//127.0.0.1:9988/ds_0", "test");
        assertFalse(actualMariaDB.isInSameDatabaseInstance(actualOracle));
    }

    @Test
    public void assertFalseInSameDatabaseInstanceWithOracleAndPostgreSQL() {
        PostgreSQLDataSourceMetaData actualPostgreSQL = new PostgreSQLDataSourceMetaData("jdbc:postgresql://127.0.0.1/ds_0");
        OracleDataSourceMetaData actualOracle = new OracleDataSourceMetaData("jdbc:oracle:thin:@//127.0.0.1:9988/ds_0", "test");
        assertFalse(actualPostgreSQL.isInSameDatabaseInstance(actualOracle));
    }

    @Test
    public void assertFalseInSameDatabaseInstanceWithOracleAndSQL92() {
        SQL92DataSourceMetaData actualSQL92 = new SQL92DataSourceMetaData("jdbc:sql92_db:ds_0");
        OracleDataSourceMetaData actualOracle = new OracleDataSourceMetaData("jdbc:oracle:thin:@//127.0.0.1:9988/ds_0", "test");
        assertFalse(actualSQL92.isInSameDatabaseInstance(actualOracle));
    }

    @Test
    public void assertFalseInSameDatabaseInstanceWithOracleAndSQLServer() {
        SQLServerDataSourceMetaData actualSQLServer = new SQLServerDataSourceMetaData("jdbc:microsoft:sqlserver://127.0.0.1:9999;DatabaseName=ds_1");
        OracleDataSourceMetaData actualOracle = new OracleDataSourceMetaData("jdbc:oracle:thin:@//127.0.0.1:9988/ds_0", "test");
        assertFalse(actualSQLServer.isInSameDatabaseInstance(actualOracle));
    }

    @Test
    public void assertFalseInSameDatabaseInstanceWithPostgreSQLAndSQL92() {
        PostgreSQLDataSourceMetaData actualPostgreSQL = new PostgreSQLDataSourceMetaData("jdbc:postgresql://127.0.0.1/ds_0");
        SQL92DataSourceMetaData actualSQL92 = new SQL92DataSourceMetaData("jdbc:sql92_db:ds_0");
        assertFalse(actualPostgreSQL.isInSameDatabaseInstance(actualSQL92));
    }

    @Test
    public void assertFalseInSameDatabaseInstanceWithPostgreSQLAndSQLServer() {
        PostgreSQLDataSourceMetaData actualPostgreSQL = new PostgreSQLDataSourceMetaData("jdbc:postgresql://127.0.0.1/ds_0");
        SQLServerDataSourceMetaData actualSQLServer = new SQLServerDataSourceMetaData("jdbc:microsoft:sqlserver://127.0.0.1:9999;DatabaseName=ds_1");
        assertFalse(actualPostgreSQL.isInSameDatabaseInstance(actualSQLServer));
    }

    @Test
    public void assertFalseInSameDatabaseInstanceWithSQL92AndSQLServer() {
        SQL92DataSourceMetaData actualSQL92 = new SQL92DataSourceMetaData("jdbc:sql92_db:ds_0");
        SQLServerDataSourceMetaData actualSQLServer = new SQLServerDataSourceMetaData("jdbc:microsoft:sqlserver://127.0.0.1:9999;DatabaseName=ds_1");
        assertFalse(actualSQL92.isInSameDatabaseInstance(actualSQLServer));
    }
}
