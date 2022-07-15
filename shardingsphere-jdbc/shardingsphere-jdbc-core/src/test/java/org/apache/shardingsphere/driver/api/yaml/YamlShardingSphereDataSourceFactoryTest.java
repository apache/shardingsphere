package org.apache.shardingsphere.driver.api.yaml;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import javax.sql.DataSource;
import org.apache.shardingsphere.driver.api.ShardingSphereDataSourceFactoryTest;
import org.apache.shardingsphere.driver.jdbc.core.datasource.ShardingSphereDataSource;
import org.apache.shardingsphere.test.mock.MockedDataSource;
import org.junit.Test;

public final class YamlShardingSphereDataSourceFactoryTest {

    @Test
    public void assertCreateDataSourceWithFile() throws Exception {
        URL url = YamlShardingSphereDataSourceFactoryTest.class.getResource("/yaml/configWithDataSourceWithRules.yaml");
        assertNotNull(url);
        File yamlFile = new File(url.toURI());
        DataSource dataSource = YamlShardingSphereDataSourceFactory.createDataSource(yamlFile);
        assertNotNull(dataSource);
        assertTrue(dataSource instanceof ShardingSphereDataSource);
        assertThat(ShardingSphereDataSourceFactoryTest.getDatabaseName(dataSource), is("logic_db"));

    }

    @Test
    public void assertCreateDataSourceWithBytes() throws SQLException, IOException {
        URL url = YamlShardingSphereDataSourceFactoryTest.class.getResource("/yaml/configWithDataSourceWithRules.yaml");
        assertNotNull(url);
        StringBuilder yamlContent = new StringBuilder();
        try (
                FileReader fileReader = new FileReader(url.getFile());
                BufferedReader reader = new BufferedReader(fileReader)) {
            String line;
            while (null != (line = reader.readLine())) {
                yamlContent.append(line).append(System.lineSeparator());
            }
        }

        DataSource dataSource = YamlShardingSphereDataSourceFactory.createDataSource(yamlContent.toString().getBytes());
        assertNotNull(dataSource);
        assertTrue(dataSource instanceof ShardingSphereDataSource);
        assertThat(ShardingSphereDataSourceFactoryTest.getDatabaseName(dataSource), is("logic_db"));
    }

    @Test
    public void assertCreateDataSourceWithoutDataSource() throws Exception {
        URL url = YamlShardingSphereDataSourceFactoryTest.class.getResource("/yaml/configWithoutDataSourceWithRules.yaml");
        assertNotNull(url);
        File yamlFile = new File(url.toURI());
        Map<String, DataSource> dataSourceMap = new HashMap<>();
        dataSourceMap.put("ds_0", new MockedDataSource("jdbc:mock:://localhost:3306/logic_ds_01", "root", "root"));
        dataSourceMap.put("ds_1", new MockedDataSource("jdbc:mock:://localhost:3306/logic_ds_01", "root", "root"));
        DataSource dataSource = YamlShardingSphereDataSourceFactory.createDataSource(dataSourceMap, yamlFile);
        assertNotNull(dataSource);
        assertTrue(dataSource instanceof ShardingSphereDataSource);
        assertThat(ShardingSphereDataSourceFactoryTest.getDatabaseName(dataSource), is("logic_db"));
    }

    @Test
    public void assertCreateDataSourceWithOnlyDataSource() throws Exception {
        URL url = YamlShardingSphereDataSourceFactoryTest.class.getResource("/yaml/configWithoutRules.yaml");
        assertNotNull(url);
        File yamlFile = new File(url.toURI());
        MockedDataSource mockedDataSource = new MockedDataSource("jdbc:mock:://localhost:3306/logic_ds_01", "root", "root");
        DataSource dataSource = YamlShardingSphereDataSourceFactory.createDataSource(mockedDataSource, yamlFile);
        assertNotNull(dataSource);
        assertTrue(dataSource instanceof ShardingSphereDataSource);
        assertThat(ShardingSphereDataSourceFactoryTest.getDatabaseName(dataSource), is("logic_db"));

    }
}
