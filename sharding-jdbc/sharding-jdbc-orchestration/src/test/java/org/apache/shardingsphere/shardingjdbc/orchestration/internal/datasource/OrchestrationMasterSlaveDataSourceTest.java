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

package org.apache.shardingsphere.shardingjdbc.orchestration.internal.datasource;

import lombok.SneakyThrows;
import org.apache.shardingsphere.orchestration.config.OrchestrationConfiguration;
import org.apache.shardingsphere.orchestration.reg.api.RegistryCenterConfiguration;
import org.apache.shardingsphere.shardingjdbc.api.yaml.YamlMasterSlaveDataSourceFactory;
import org.apache.shardingsphere.shardingjdbc.jdbc.core.datasource.MasterSlaveDataSource;
import org.apache.shardingsphere.shardingjdbc.orchestration.user.YamlUserTest;
import org.junit.Before;
import org.junit.Test;

import java.io.File;

public class OrchestrationMasterSlaveDataSourceTest {
    
    private OrchestrationMasterSlaveDataSource masterSlaveDataSource;
    
    @Before
    @SneakyThrows
    public void setUp() {
        masterSlaveDataSource = new OrchestrationMasterSlaveDataSource(getMasterSlaveDataSource(), getOrchestrationConfiguration());
        
    }
    
    @SneakyThrows
    private MasterSlaveDataSource getMasterSlaveDataSource() {
        File yamlFile = new File(YamlUserTest.class.getResource("/yaml/unit/masterSlave.yaml").toURI());
        return (MasterSlaveDataSource) YamlMasterSlaveDataSourceFactory.createDataSource(yamlFile);
    }
    
    private OrchestrationConfiguration getOrchestrationConfiguration() {
        return new OrchestrationConfiguration("test", new RegistryCenterConfiguration(), true);
    }
    
    @Test
    public void assertRenewRule() {
    }
    
    @Test
    public void assertRenewDataSource() {
    }
    
    @Test
    public void assertRenewProperties() {
    }
    
    @Test
    public void assertRenewConfigMap() {
    }
    
    @Test
    public void assertRenewDisabledState() {
    }
    
    @Test
    public void assertRenewCircuitState() {
    }
    
    @Test
    public void assertGetDataSource() {
    }
}
