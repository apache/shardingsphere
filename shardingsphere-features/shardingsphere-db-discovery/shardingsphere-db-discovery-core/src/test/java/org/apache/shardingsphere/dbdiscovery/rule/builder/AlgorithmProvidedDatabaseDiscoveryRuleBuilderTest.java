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

package org.apache.shardingsphere.dbdiscovery.rule.builder;

import org.apache.shardingsphere.dbdiscovery.algorithm.config.AlgorithmProvidedDatabaseDiscoveryRuleConfiguration;
import org.apache.shardingsphere.dbdiscovery.api.config.rule.DatabaseDiscoveryDataSourceRuleConfiguration;
import org.apache.shardingsphere.dbdiscovery.api.config.rule.DatabaseDiscoveryHeartBeatConfiguration;
import org.apache.shardingsphere.dbdiscovery.fixture.TestDatabaseDiscoveryType;
import org.apache.shardingsphere.dbdiscovery.rule.DatabaseDiscoveryRule;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.rule.builder.schema.SchemaRuleBuilder;
import org.apache.shardingsphere.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.spi.type.ordered.OrderedSPIRegistry;
import org.junit.Test;

import javax.sql.DataSource;
import java.util.Collections;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

public final class AlgorithmProvidedDatabaseDiscoveryRuleBuilderTest {
    
    static {
        ShardingSphereServiceLoader.register(SchemaRuleBuilder.class);
    }
    
    @SuppressWarnings({"rawtypes", "unchecked"})
    @Test
    public void assertBuild() {
        AlgorithmProvidedDatabaseDiscoveryRuleConfiguration algorithmProvidedRuleConfig = new AlgorithmProvidedDatabaseDiscoveryRuleConfiguration(
                Collections.singletonList(new DatabaseDiscoveryDataSourceRuleConfiguration("name", Collections.singletonList("name"), "", "discoveryTypeName")),
                Collections.singletonMap("ha_heartbeat", 
                        new DatabaseDiscoveryHeartBeatConfiguration(new Properties())), Collections.singletonMap("discoveryTypeName", new TestDatabaseDiscoveryType()));
        SchemaRuleBuilder builder = OrderedSPIRegistry.getRegisteredServices(SchemaRuleBuilder.class, Collections.singletonList(algorithmProvidedRuleConfig)).get(algorithmProvidedRuleConfig);
        assertThat(builder.build(algorithmProvidedRuleConfig, "", Collections.singletonMap("name", mock(DataSource.class)), Collections.emptyList(), new ConfigurationProperties(new Properties())), 
                instanceOf(DatabaseDiscoveryRule.class));
    }
}
