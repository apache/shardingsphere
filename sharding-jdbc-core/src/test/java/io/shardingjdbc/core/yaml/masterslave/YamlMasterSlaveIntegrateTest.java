/*
 * Copyright 1999-2015 dangdang.com.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingjdbc.core.yaml.masterslave;

import com.google.common.base.Function;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import io.shardingjdbc.core.api.ConfigMapContext;
import io.shardingjdbc.core.api.MasterSlaveDataSourceFactory;
import io.shardingjdbc.core.yaml.AbstractYamlDataSourceTest;
import lombok.RequiredArgsConstructor;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import javax.sql.DataSource;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

@RunWith(Parameterized.class)
@RequiredArgsConstructor
public class YamlMasterSlaveIntegrateTest extends AbstractYamlDataSourceTest {
    
    private final String filePath;
    
    private final boolean hasDataSource;
    
    private final boolean isByteArray;
    
    @Parameterized.Parameters(name = "{index}:{0}-hasDataSource:{1}-isByteArray:{2}")
    public static Collection init() {
        return Arrays.asList(new Object[][]{
                {"/yaml/integrate/ms/configWithMasterSlaveDataSourceWithoutProps.yaml", true, true},
                {"/yaml/integrate/ms/configWithMasterSlaveDataSourceWithoutProps.yaml", true, false},
                {"/yaml/integrate/ms/configWithMasterSlaveDataSourceWithoutProps.yaml", false, true},
                {"/yaml/integrate/ms/configWithMasterSlaveDataSourceWithoutProps.yaml", false, false},
        });
    }
    
    @Test
    public void assertWithDataSource() throws SQLException, URISyntaxException, IOException {
        File yamlFile = new File(YamlMasterSlaveIntegrateTest.class.getResource(filePath).toURI());
        DataSource dataSource;
        if (hasDataSource) {
            if (isByteArray) {
                dataSource = MasterSlaveDataSourceFactory.createDataSource(toByteArray(yamlFile.getPath()));
            } else {
                dataSource = MasterSlaveDataSourceFactory.createDataSource(yamlFile);
            }
        } else {
            if (isByteArray) {
                dataSource = MasterSlaveDataSourceFactory.createDataSource(Maps.asMap(Sets.newHashSet("db_master", "db_slave_0", "db_slave_1"), new Function<String, DataSource>() {
                    @Override
                    public DataSource apply(final String key) {
                        return createDataSource(key);
                    }
                }), toByteArray(yamlFile.getPath()));
            } else {
                dataSource = MasterSlaveDataSourceFactory.createDataSource(Maps.asMap(Sets.newHashSet("db_master", "db_slave_0", "db_slave_1"), new Function<String, DataSource>() {
                    @Override
                    public DataSource apply(final String key) {
                        return createDataSource(key);
                    }
                }), yamlFile);
            }
        }
        Map<String, Object> configMap = new ConcurrentHashMap<>();
        configMap.put("key1", "value1");
        assertThat(ConfigMapContext.getInstance().getMasterSlaveConfig(), is(configMap));
        try (Connection conn = dataSource.getConnection();
             Statement stm = conn.createStatement()) {
            stm.executeQuery("SELECT * FROM t_order");
            stm.executeQuery("SELECT * FROM t_order_item");
            stm.executeQuery("SELECT * FROM t_config");
        }
    }
    
    private byte[] toByteArray(final String filePath) throws IOException {
        final InputStream in = new FileInputStream(filePath);
        ByteArrayOutputStream result = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024 * 4];
        int index;
        while ((index = in.read(buffer)) != -1) {
            result.write(buffer, 0, index);
        }
        return result.toByteArray();
    }
}
