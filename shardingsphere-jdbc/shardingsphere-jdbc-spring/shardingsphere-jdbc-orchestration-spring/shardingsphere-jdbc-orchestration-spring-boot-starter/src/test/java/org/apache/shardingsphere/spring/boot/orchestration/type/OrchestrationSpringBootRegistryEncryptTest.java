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

import lombok.SneakyThrows;
import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.shardingsphere.driver.jdbc.core.datasource.ShardingSphereDataSource;
import org.apache.shardingsphere.driver.orchestration.internal.datasource.OrchestrationShardingSphereDataSource;
import org.apache.shardingsphere.encrypt.api.config.EncryptRuleConfiguration;
import org.apache.shardingsphere.infra.config.algorithm.ShardingSphereAlgorithmConfiguration;
import org.apache.shardingsphere.spring.boot.orchestration.registry.TestOrchestrationRepository;
import org.apache.shardingsphere.spring.boot.orchestration.util.EmbedTestingServer;
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
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = OrchestrationSpringBootRegistryEncryptTest.class)
@SpringBootApplication
@ActiveProfiles("registry")
public class OrchestrationSpringBootRegistryEncryptTest {
    
    private static final String DATA_SOURCE_FILE = "yaml/data-source.yaml";
    
    private static final String ENCRYPT_RULE_FILE = "yaml/encrypt-rule.yaml";
    
    @Resource
    private DataSource dataSource;
    
    @BeforeClass
    public static void init() {
        EmbedTestingServer.start();
        String dataSource = readYAML(DATA_SOURCE_FILE);
        String encryptRule = readYAML(ENCRYPT_RULE_FILE);
        TestOrchestrationRepository repository = new TestOrchestrationRepository();
        repository.persist("/orchestration-spring-boot-test/config/schema/logic_db/datasource", dataSource);
        repository.persist("/orchestration-spring-boot-test/config/schema/logic_db/rule", encryptRule);
        repository.persist("/orchestration-spring-boot-test/config/props", "sql.show: 'true'\n");
        repository.persist("/orchestration-spring-boot-test/registry/datasources", "");
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
        ShardingSphereAlgorithmConfiguration encryptAlgorithmConfiguration = configuration.getEncryptors().get("order_encrypt");
        assertThat(encryptAlgorithmConfiguration, instanceOf(ShardingSphereAlgorithmConfiguration.class));
        assertThat(encryptAlgorithmConfiguration.getType(), is("AES"));
    }
    
    @SneakyThrows
    private static String readYAML(final String yamlFile) {
        return Files.readAllLines(Paths.get(ClassLoader.getSystemResource(yamlFile).toURI())).stream().map(each -> each + System.lineSeparator()).collect(Collectors.joining());
    }
}
