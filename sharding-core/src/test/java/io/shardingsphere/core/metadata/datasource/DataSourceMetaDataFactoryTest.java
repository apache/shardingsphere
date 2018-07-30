package io.shardingsphere.core.metadata.datasource;

import io.shardingsphere.core.constant.DatabaseType;
import io.shardingsphere.core.metadata.datasource.dialect.H2DataSourceMetaData;
import io.shardingsphere.core.metadata.datasource.dialect.MySQLDataSourceMetaData;
import io.shardingsphere.core.metadata.datasource.dialect.OracleDataSourceMetaData;
import io.shardingsphere.core.metadata.datasource.dialect.PostgreSQLDataSourceMetaData;
import io.shardingsphere.core.metadata.datasource.dialect.SQLServerDataSourceMetaData;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class DataSourceMetaDataFactoryTest {
    
    @Test
    public void assertAllNewInstance() {
        assertTrue((DataSourceMetaDataFactory.newInstance(DatabaseType.MySQL, "jdbc:mysql://127.0.0.1:3306/db_0") instanceof MySQLDataSourceMetaData));
        assertTrue((DataSourceMetaDataFactory.newInstance(DatabaseType.Oracle, "jdbc:oracle:thin:@//127.0.0.1:3306/ds_0") instanceof OracleDataSourceMetaData));
        assertTrue((DataSourceMetaDataFactory.newInstance(DatabaseType.PostgreSQL, "jdbc:postgresql://127.0.0.1:3306/ds_0") instanceof PostgreSQLDataSourceMetaData));
        assertTrue((DataSourceMetaDataFactory.newInstance(DatabaseType.SQLServer, "jdbc:microsoft:sqlserver://127.0.0.1:3306;DatabaseName=ds_0") instanceof SQLServerDataSourceMetaData));
        assertTrue((DataSourceMetaDataFactory.newInstance(DatabaseType.H2, "jdbc:h2:mem:ds_0;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false;MODE=MySQL") instanceof H2DataSourceMetaData));
    }
}