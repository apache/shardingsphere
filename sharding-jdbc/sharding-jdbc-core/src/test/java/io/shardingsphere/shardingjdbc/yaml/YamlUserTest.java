/*
 * Copyright 2016-2018 shardingsphere.io.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingsphere.shardingjdbc.yaml;

import io.shardingsphere.api.HintManager;
import io.shardingsphere.shardingjdbc.api.yaml.YamlShardingDataSourceFactory;
import io.shardingsphere.shardingjdbc.yaml.exception.MyException;
import lombok.RequiredArgsConstructor;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import javax.sql.DataSource;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Collection;

@RunWith(Parameterized.class)
@RequiredArgsConstructor
@Ignore
public class YamlUserTest {

    private final String filePath;

    private final boolean hasDataSource;

    @Parameterized.Parameters(name = "{index}:{0}-{1}")
    public static Collection init() {
        return Arrays.asList(new Object[][]{
                {"/integrate/yaml/user.yaml", true},
        });
    }

   @Test
    public void assertWithDataSource() throws SQLException, URISyntaxException, IOException, ReflectiveOperationException {
        File yamlFile = new File(YamlUserTest.class.getResource(filePath).toURI());
        DataSource dataSource = YamlShardingDataSourceFactory.createDataSource(yamlFile);
        Connection conn = dataSource.getConnection();
        Statement stm = conn.createStatement();
        
        try {
            HintManager.getInstance().addTableShardingValue("t_order", 1);
//            ResultSet resultSet = stm.executeQuery("insert into t_order(order_id, status) values(1,1) ");
//            ResultSet resultSet = stm.executeQuery("update t_order set status=1 where order_id=1; ");
            ResultSet resultSet = stm.executeQuery("select  * from t_order where user_id=1 and order_id=1");
//            ResultSet resultSet = stm.executeQuery("select count(DISTINCT  user_id), user_id from t_order group by user_id; ");
            System.out.println("user_id, order_id");
            while (resultSet.next()) {
//                System.out.println(resultSet.getString(1) );
//                System.out.println(resultSet.getString(1) + ',' + resultSet.getString(2));
//                System.out.println(resultSet.getString("count(DISTINCT   user_id)") + ',' + resultSet.getString(1));
                System.out.println(resultSet.getString(1) + ',' + resultSet.getString(2) + ',' +resultSet.getString(3));
            }
//            stm.execute("insert into defray_order(id, order_id,trade_no,request_no,system_id,out_customer_no,amount,account_status,bank_status,trade_status,created_date,modified_date) values (1,'2',1,1,1,1,1,1,1,1,now(),now());");
        } catch (final MyException ex) {
            System.out.println(ex.getClass());
        }
//        ResultSet resultSet = stm.executeQuery("select * from args; ");
       //        ResultSet resultSet = stm.executeQuery("select * from defray_order where order_id in ('20180324023001992815218507495030','20180411022002002815234266046583');");
      
//        stm.executeQuery("SELECT d.* FROM event e, detector_device d where e.status = 'init' and d.is_online not in (2, 3) " +
//                "and e.device_code = d.code group by e.device_code order by e.date desc");

//        if (hasDataSource) {
//            dataSource = YamlShardingDataSourceFactory.createDataSource(yamlFile);
//        } else {
//            dataSource = YamlShardingDataSourceFactory.createDataSource(Maps.asMap(Sets.newHashSet("db0", "db1"), new Function<String, DataSource>() {
//                @Override
//                public DataSource apply(final String key) {
//                    return createDataSource(key);
//                }
//            }), yamlFile);
//        }
//        if (filePath.contains("WithProps.yaml")) {
//            Field field = dataSource.getClass().getDeclaredField("shardingProperties");
//            if (!field.isAccessible()) {
//                field.setAccessible(true);
//            }
//            ShardingProperties shardingProperties = (ShardingProperties) field.get(dataSource);
//            assertTrue((Boolean) shardingProperties.getValue(ShardingPropertiesConstant.SQL_SHOW));
//        }
//        Map<String, Object> configMap = new ConcurrentHashMap<>();
//        configMap.put("key1", "value1");
//        assertThat(ConfigMapContext.getInstance().getShardingConfig(), is(configMap));
//        try (Connection conn = dataSource.getConnection();
//             Statement stm = conn.createStatement()) {
//            stm.execute(String.format("INSERT INTO t_order(user_id,status) values(%d, %s)", 10, "'insert'"));
//            stm.executeQuery("SELECT o.*, i.* FROM T_order o JOIN T_order_item i ON o.order_id = i.order_id");
//            stm.executeQuery("SELECT * FROM config");
//        }
    }
}
