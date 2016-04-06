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

package com.dangdang.ddframe.rdb.sharding.config.yaml;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Collection;
import javax.sql.DataSource;

import com.dangdang.ddframe.rdb.sharding.config.yaml.api.YamlShardingDataSource;
import com.google.common.base.Function;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import lombok.RequiredArgsConstructor;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(Parameterized.class)
@RequiredArgsConstructor
public class YamlShardingConfigTest extends AbstractYamlShardingDataSourceTest {
    
    private final String filePath;
    
    private final boolean hasDataSource;
    
    @Parameterized.Parameters(name = "{index}:{0}-{1}")
    public static Collection init() {
        return Arrays.asList(new Object[][]{
                {"/configWithDataSourceWithoutProps.yaml", true},
                {"/configWithoutDataSourceWithoutProps.yaml", false},
                {"/configWithDataSourceWithProps.yaml", true},
                {"/configWithoutDataSourceWithProps.yaml", false},
        });
    }
    
    @Test
    public void testWithDataSource() throws SQLException, URISyntaxException, IOException {
        File yamlFile = new File(YamlShardingConfigTest.class.getResource(filePath).toURI());
        DataSource dataSource;
        if (hasDataSource) {
            dataSource = new YamlShardingDataSource(yamlFile);
        } else {
            dataSource = new YamlShardingDataSource(Maps.asMap(Sets.newHashSet("db0", "db1"), new Function<String, DataSource>() {
                @Override
                public DataSource apply(final String key) {
                    return createDataSource(key);
                }
            }), yamlFile);
        }
        
        try (Connection conn = dataSource.getConnection();
             Statement stm = conn.createStatement()) {
            stm.executeQuery("SELECT * FROM t_order");
            stm.executeQuery("SELECT * FROM t_order_item");
            stm.executeQuery("SELECT * FROM config");
        }
    }
    
}
