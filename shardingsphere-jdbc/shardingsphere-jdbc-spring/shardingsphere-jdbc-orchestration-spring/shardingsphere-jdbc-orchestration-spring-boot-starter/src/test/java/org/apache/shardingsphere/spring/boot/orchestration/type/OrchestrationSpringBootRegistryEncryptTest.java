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

package org.apache.shardingsphere.spring.boot.orchestration.type;

import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.shardingsphere.driver.jdbc.core.datasource.ShardingSphereDataSource;
import org.apache.shardingsphere.driver.orchestration.internal.datasource.OrchestrationShardingSphereDataSource;
import org.apache.shardingsphere.spring.boot.orchestration.registry.TestCenterRepository;
import org.apache.shardingsphere.spring.boot.orchestration.util.EmbedTestingServer;
import org.apache.shardingsphere.encrypt.api.config.EncryptRuleConfiguration;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;
import javax.sql.DataSource;
import java.lang.reflect.Field;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = OrchestrationSpringBootRegistryEncryptTest.class)
@SpringBootApplication
@ActiveProfiles("registry")
public class OrchestrationSpringBootRegistryEncryptTest {
    
    @Resource
    private DataSource dataSource;
    
    @BeforeClass
    public static void init() {
        EmbedTestingServer.start();
        TestCenterRepository testCenter = new TestCenterRepository();
        testCenter.persist("/demo_spring_boot_ds_center/config/schema/logic_db/datasource", ""
                + "dataSource: !!org.apache.shardingsphere.orchestration.core.configuration.YamlDataSourceConfiguration\n" 
                + "  dataSourceClassName: org.apache.commons.dbcp2.BasicDataSource\n"
                + "  properties:\n"
                + "    url: jdbc:h2:mem:ds;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false;MODE=MYSQL\n"
                + "    maxTotal: 100\n"
                + "    password: ''\n"
                + "    username: sa\n");
        testCenter.persist("/demo_spring_boot_ds_center/config/schema/logic_db/rule", ""
                + "rules:\n"
                + "- !ENCRYPT\n"
                + "  encryptors:\n"
                + "    order_encrypt:\n"
                + "      props:\n"
                + "        aes.key.value: '123456'\n"
                + "      type: aes\n"
                + "  tables:\n" 
                + "    t_order:\n" 
                + "      columns:\n"
                + "         user_id:\n"
                + "           cipherColumn: user_id\n"
                + "           encryptor: order_encrypt\n");
        testCenter.persist("/demo_spring_boot_ds_center/config/props", "sql.show: 'true'\n");
        testCenter.persist("/demo_spring_boot_ds_center/registry/datasources", "");
    }
    
    @Test
    public void assertWithEncryptDataSource() throws NoSuchFieldException, IllegalAccessException {
        assertTrue(dataSource instanceof OrchestrationShardingSphereDataSource);
        Field field = OrchestrationShardingSphereDataSource.class.getDeclaredField("dataSource");
        field.setAccessible(true);
        ShardingSphereDataSource encryptDataSource = (ShardingSphereDataSource) field.get(dataSource);
        BasicDataSource embedDataSource = (BasicDataSource) encryptDataSource.getDataSourceMap().values().iterator().next();
        assertThat(embedDataSource.getMaxTotal(), is(100));
        assertThat(embedDataSource.getUsername(), is("sa"));
        EncryptRuleConfiguration configuration = (EncryptRuleConfiguration) encryptDataSource.getSchemaContexts().getDefaultSchemaContext().getSchema().getConfigurations().iterator().next();
        assertThat(configuration.getEncryptors().size(), is(1));
        assertTrue(configuration.getEncryptors().containsKey("order_encrypt"));
        assertThat(configuration.getEncryptors().get("order_encrypt").getType(), is("aes"));
    }
}
