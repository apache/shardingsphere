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

package org.apache.shardingsphere.proxy.backend.config.checker;

import org.apache.shardingsphere.infra.exception.kernel.metadata.resource.storageunit.DuplicateStorageUnitException;
import org.apache.shardingsphere.proxy.backend.config.yaml.YamlProxyDataSourceConfiguration;
import org.apache.shardingsphere.proxy.backend.config.yaml.YamlProxyDatabaseConfiguration;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

final class YamlProxyConfigurationCheckerTest {
    
    @Test
    void assertCheckDataSourcesWithoutDuplicates() {
        Map<String, YamlProxyDataSourceConfiguration> globalDataSources = Collections.singletonMap("global_ds", new YamlProxyDataSourceConfiguration());
        YamlProxyDatabaseConfiguration databaseConfig = new YamlProxyDatabaseConfiguration();
        databaseConfig.setDatabaseName("foo_db");
        databaseConfig.setDataSources(Collections.singletonMap("db_ds", new YamlProxyDataSourceConfiguration()));
        assertDoesNotThrow(() -> YamlProxyConfigurationChecker.checkDataSources(globalDataSources, Collections.singleton(databaseConfig)));
    }
    
    @Test
    void assertCheckDataSourcesWithDuplicates() {
        Map<String, YamlProxyDataSourceConfiguration> globalDataSources = Collections.singletonMap("ds_0", new YamlProxyDataSourceConfiguration());
        YamlProxyDatabaseConfiguration databaseConfig = new YamlProxyDatabaseConfiguration();
        databaseConfig.setDatabaseName("foo_db");
        databaseConfig.setDataSources(Collections.singletonMap("ds_0", new YamlProxyDataSourceConfiguration()));
        DuplicateStorageUnitException ex = assertThrows(DuplicateStorageUnitException.class,
                () -> YamlProxyConfigurationChecker.checkDataSources(globalDataSources, Collections.singleton(databaseConfig)));
        assertThat(ex.getMessage(), is("Duplicate storage unit names 'ds_0' on database 'foo_db'."));
    }
}
