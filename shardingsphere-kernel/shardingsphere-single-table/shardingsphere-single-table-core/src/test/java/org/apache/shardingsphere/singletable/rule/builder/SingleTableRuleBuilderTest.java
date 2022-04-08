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

package org.apache.shardingsphere.singletable.rule.builder;

import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.config.props.ConfigurationPropertyKey;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.rule.builder.schema.SchemaRuleBuilder;
import org.apache.shardingsphere.infra.rule.identifier.scope.SchemaRule;
import org.apache.shardingsphere.singletable.config.SingleTableRuleConfiguration;
import org.apache.shardingsphere.singletable.rule.SingleTableRule;
import org.apache.shardingsphere.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.spi.type.ordered.OrderedSPIRegistry;
import org.junit.Test;

import java.util.Collection;
import java.util.Collections;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

public final class SingleTableRuleBuilderTest {
    
    static {
        ShardingSphereServiceLoader.register(SchemaRuleBuilder.class);
    }
    
    @Test
    public void assertBuild() {
        Collection<SchemaRuleBuilder> builders = OrderedSPIRegistry.getRegisteredServices(SchemaRuleBuilder.class);
        SchemaRuleBuilder builder = builders.iterator().next();
        Properties props = new Properties();
        props.setProperty(ConfigurationPropertyKey.CHECK_DUPLICATE_TABLE_ENABLED.getKey(), Boolean.FALSE.toString());
        SingleTableRuleConfiguration config = mock(SingleTableRuleConfiguration.class);
        ShardingSphereRule shardingSphereRule = mock(ShardingSphereRule.class);
        SchemaRule schemaRule = builder.build(config, "", Collections.emptyMap(), Collections.singletonList(shardingSphereRule), new ConfigurationProperties(props));
        assertThat(schemaRule, instanceOf(SingleTableRule.class));
        assertFalse(((SingleTableRule) schemaRule).getDefaultDataSource().isPresent());
    }
    
    @Test
    public void assertBuildWithDefaultDataSource() {
        Properties props = new Properties();
        props.setProperty(ConfigurationPropertyKey.CHECK_DUPLICATE_TABLE_ENABLED.getKey(), Boolean.FALSE.toString());
        ShardingSphereRule shardingSphereRule = mock(ShardingSphereRule.class);
        Collection<SchemaRuleBuilder> builders = OrderedSPIRegistry.getRegisteredServices(SchemaRuleBuilder.class);
        SchemaRuleBuilder builder = builders.iterator().next();
        SingleTableRuleConfiguration config = new SingleTableRuleConfiguration();
        config.setDefaultDataSource("ds_0");
        SchemaRule schemaRule = builder.build(config, "", Collections.emptyMap(), Collections.singletonList(shardingSphereRule), new ConfigurationProperties(props));
        assertThat(schemaRule, instanceOf(SingleTableRule.class));
        assertTrue(((SingleTableRule) schemaRule).getDefaultDataSource().isPresent());
        assertThat(((SingleTableRule) schemaRule).getDefaultDataSource().get(), is("ds_0"));
    }
}
