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

package org.apache.shardingsphere.shardingjdbc.orchestration.spring.boot.type;

import java.lang.reflect.Field;
import javax.annotation.Resource;
import javax.sql.DataSource;
import lombok.SneakyThrows;
import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.shardingsphere.shardingjdbc.jdbc.core.datasource.MasterSlaveDataSource;
import org.apache.shardingsphere.shardingjdbc.orchestration.spring.boot.registry.TestCenterRepository;
import org.apache.shardingsphere.shardingjdbc.orchestration.spring.boot.util.EmbedTestingServer;
import org.apache.shardingsphere.shardingjdbc.orchestration.internal.datasource.OrchestrationMasterSlaveDataSource;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = OrchestrationSpringBootRegistryMasterSlaveTest.class)
@SpringBootApplication
@ActiveProfiles("registry")
public class OrchestrationSpringBootRegistryMasterSlaveTest {
    
    @Resource
    private DataSource dataSource;
    
    @BeforeClass
    public static void init() {
        EmbedTestingServer.start();
        TestCenterRepository testCenter = new TestCenterRepository();
        testCenter.persist("/demo_spring_boot_ds_center/config/schema/logic_db/datasource",
            "ds_master: !!org.apache.shardingsphere.orchestration.core.configuration.YamlDataSourceConfiguration\n"
            + "  dataSourceClassName: org.apache.commons.dbcp2.BasicDataSource\n"
            + "  properties:\n"
            + "    url: jdbc:h2:mem:ds_master;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false;MODE=MYSQL\n"
            + "    maxTotal: 16\n"
            + "    password: ''\n"
            + "    username: root\n"
            + "ds_slave_0: !!org.apache.shardingsphere.orchestration.core.configuration.YamlDataSourceConfiguration\n"
            + "  dataSourceClassName: org.apache.commons.dbcp2.BasicDataSource\n"
            + "  properties:\n"
            + "    url: jdbc:h2:mem:demo_ds_slave_0;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false;MODE=MYSQL\n"
            + "    maxTotal: 16\n"
            + "    password: ''\n"
            + "    username: root\n"
            + "ds_slave_1: !!org.apache.shardingsphere.orchestration.core.configuration.YamlDataSourceConfiguration\n"
            + "  dataSourceClassName: org.apache.commons.dbcp2.BasicDataSource\n"
            + "  properties:\n"
            + "    url: jdbc:h2:mem:demo_ds_slave_1;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false;MODE=MYSQL\n"
            + "    maxTotal: 16\n"
            + "    password: ''\n"
            + "    username: root\n");
        testCenter.persist("/demo_spring_boot_ds_center/config/schema/logic_db/rule", "loadBalanceAlgorithmType: ROUND_ROBIN\n"
            + "masterDataSourceName: ds_master\n"
            + "name: ds_ms\n"
            + "slaveDataSourceNames: \n"
            + "  - ds_slave_0\n"
            + "  - ds_slave_1\n");
        testCenter.persist("/demo_spring_boot_ds_center/config/props", "{}\n");
        testCenter.persist("/demo_spring_boot_ds_center/registry/datasources", "");
    }
    
    @Test
    @SneakyThrows
    public void assertWithMasterSlaveDataSource() {
        assertTrue(dataSource instanceof OrchestrationMasterSlaveDataSource);
        Field field = OrchestrationMasterSlaveDataSource.class.getDeclaredField("dataSource");
        field.setAccessible(true);
        MasterSlaveDataSource masterSlaveDataSource = (MasterSlaveDataSource) field.get(dataSource);
        for (DataSource each : masterSlaveDataSource.getDataSourceMap().values()) {
            assertThat(((BasicDataSource) each).getMaxTotal(), is(16));
            assertThat(((BasicDataSource) each).getUsername(), is("root"));
        }
    }
}
