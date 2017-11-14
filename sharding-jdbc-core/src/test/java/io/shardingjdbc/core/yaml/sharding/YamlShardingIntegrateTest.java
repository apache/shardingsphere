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

package io.shardingjdbc.core.yaml.sharding;

import com.google.common.base.Function;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import io.shardingjdbc.core.api.ConfigMapContext;
import io.shardingjdbc.core.api.ShardingDataSourceFactory;
import io.shardingjdbc.core.yaml.AbstractYamlDataSourceTest;
import lombok.RequiredArgsConstructor;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import javax.sql.DataSource;
import java.io.File;
import java.io.IOException;
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
public class YamlShardingIntegrateTest extends AbstractYamlDataSourceTest {
    
    private final String filePath;
    
    private final boolean hasDataSource;
    
    @Parameterized.Parameters(name = "{index}:{0}-{1}")
    public static Collection init() {
        return Arrays.asList(new Object[][]{
                {"/yaml/integrate/sharding/configWithDataSourceWithoutProps.yaml", true},
                {"/yaml/integrate/sharding/configWithoutDataSourceWithoutProps.yaml", false},
                {"/yaml/integrate/sharding/configWithDataSourceWithProps.yaml", true},
                {"/yaml/integrate/sharding/configWithoutDataSourceWithProps.yaml", false},
        });
    }
    
    @Test
    public void assertWithDataSource() throws SQLException, URISyntaxException, IOException {
        File yamlFile = new File(YamlShardingIntegrateTest.class.getResource(filePath).toURI());
        DataSource dataSource;
        if (hasDataSource) {
            dataSource = ShardingDataSourceFactory.createDataSource(yamlFile);
        } else {
            dataSource = ShardingDataSourceFactory.createDataSource(Maps.asMap(Sets.newHashSet("db0", "db1"), new Function<String, DataSource>() {
                @Override
                public DataSource apply(final String key) {
                    return createDataSource(key);
                }
            }), yamlFile);
        }
        Map<String, Object> configMap = new ConcurrentHashMap<>();
        configMap.put("key1", "value1");
        assertThat(ConfigMapContext.getInstance().getShardingConfig(), is(configMap));
        try (Connection conn = dataSource.getConnection();
             Statement stm = conn.createStatement()) {
            stm.execute(String.format("INSERT INTO t_order(user_id,status) values(%d, %s)", 10, "'insert'"));
            stm.executeQuery("SELECT * FROM t_order");
            stm.executeQuery("SELECT * FROM t_order_item");
            stm.executeQuery("SELECT * FROM config");
        }
    }
}
