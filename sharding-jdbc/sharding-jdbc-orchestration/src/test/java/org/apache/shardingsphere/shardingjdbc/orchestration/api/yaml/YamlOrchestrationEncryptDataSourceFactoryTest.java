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

package org.apache.shardingsphere.shardingjdbc.orchestration.api.yaml;

import lombok.SneakyThrows;
import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.shardingsphere.shardingjdbc.orchestration.internal.datasource.OrchestrationEncryptDataSource;
import org.junit.Test;

import javax.sql.DataSource;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.sql.SQLException;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertThat;

public final class YamlOrchestrationEncryptDataSourceFactoryTest {
    
    @Test
    public void assertCreateDataSourceByYamlFile() throws URISyntaxException, IOException, SQLException {
        File yamlFile = new File(OrchestrationEncryptDataSource.class.getResource("/yaml/unit/encryptWithRegistryCenter.yaml").toURI());
        DataSource dataSource = YamlOrchestrationEncryptDataSourceFactory.createDataSource(yamlFile);
        assertThat(dataSource, instanceOf(OrchestrationEncryptDataSource.class));
    }
    
    @Test
    public void assertCreateDataSourceByYamlFileWithDataSource() throws URISyntaxException, IOException, SQLException {
        File yamlFile = new File(OrchestrationEncryptDataSource.class.getResource("/yaml/unit/encryptWithRegistryCenter.yaml").toURI());
        DataSource dataSource = YamlOrchestrationEncryptDataSourceFactory.createDataSource(getDataSource(), yamlFile);
        assertThat(dataSource, instanceOf(OrchestrationEncryptDataSource.class));
    }
    
    @Test
    public void assertCreateDataSourceByYamlFileWithoutRule() throws URISyntaxException, IOException, SQLException {
        File yamlFile = new File(OrchestrationEncryptDataSource.class.getResource("/yaml/unit/noRule.yaml").toURI());
        DataSource dataSource = YamlOrchestrationEncryptDataSourceFactory.createDataSource(getDataSource(), yamlFile);
        assertThat(dataSource, instanceOf(OrchestrationEncryptDataSource.class));
    }
    
    @Test
    public void assertCreateDataSourceByYamlBytes() throws IOException, SQLException {
        DataSource dataSource = YamlOrchestrationEncryptDataSourceFactory.createDataSource(readBytesFromYamlFile());
        assertThat(dataSource, instanceOf(OrchestrationEncryptDataSource.class));
    }
    
    @Test
    public void assertCreateDataSourceByYamlBytesWithDataSource() throws IOException, SQLException {
        DataSource dataSource = YamlOrchestrationEncryptDataSourceFactory.createDataSource(getDataSource(), readBytesFromYamlFile());
        assertThat(dataSource, instanceOf(OrchestrationEncryptDataSource.class));
    }
    
    @SneakyThrows
    private byte[] readBytesFromYamlFile() {
        File yamlFile = new File(OrchestrationEncryptDataSource.class.getResource("/yaml/unit/encryptWithRegistryCenter.yaml").toURI());
        byte[] result = new byte[(int) yamlFile.length()];
        try (InputStream inputStream = new FileInputStream(yamlFile)) {
            inputStream.read(result);
        }
        return result;
    }
    
    private DataSource getDataSource() {
        BasicDataSource result = new BasicDataSource();
        result.setDriverClassName("org.h2.Driver");
        result.setUrl("jdbc:h2:mem:ds_encrypt;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false;MODE=MYSQL");
        result.setUsername("sa");
        result.setPassword("");
        return result;
    }
}
