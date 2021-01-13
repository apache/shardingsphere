package org.apache.shardingsphere.driver.common.base;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import org.apache.shardingsphere.driver.api.yaml.YamlShardingSphereDataSourceFactory;
import org.apache.shardingsphere.driver.jdbc.core.connection.ShardingSphereConnection;
import org.apache.shardingsphere.driver.jdbc.core.datasource.ShardingSphereDataSource;
import org.h2.tools.RunScript;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;

import javax.sql.DataSource;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.File;
import java.sql.SQLException;
import java.util.*;

public class AbstractShardingSphereDataSourceForCalciteTest extends AbstractSQLTest {

    private static ShardingSphereDataSource dataSource;

    private static final List<String> CALCITE_DB_NAMES = Arrays.asList("jdbc_0");

    private static final String CONFIG_CALCITE = "config-calcite.yaml";

    @BeforeClass
    public static void initCalciteDataSource() throws SQLException, IOException {
        if (null != dataSource) {
            return;
        }
        dataSource = (ShardingSphereDataSource) YamlShardingSphereDataSourceFactory.createDataSource(getDataSourceMap(), getFile(CONFIG_CALCITE));
    }

    private static Map<String, DataSource> getDataSourceMap() {
        return Maps.filterKeys(getDatabaseTypeMap().values().iterator().next(), CALCITE_DB_NAMES::contains);
    }

    private static File getFile(final String fileName) {
        return new File(Preconditions.checkNotNull(
                AbstractShardingSphereDataSourceForShardingTest.class.getClassLoader().getResource(fileName), "file resource `%s` must not be null.", fileName).getFile());
    }

    protected final ShardingSphereDataSource getShardingSphereDataSource() {
        return dataSource;
    }

    @AfterClass
    public static void clear() throws Exception {
        if (null == dataSource) {
            return;
        }
        dataSource.close();
        dataSource = null;
    }
}
