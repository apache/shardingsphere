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

import java.util.Arrays;
import java.util.Collection;
import java.util.Properties;
import org.apache.shardingsphere.infra.config.properties.ConfigurationProperties;
import org.apache.shardingsphere.infra.config.properties.ConfigurationPropertyKey;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.rule.builder.schema.SchemaRuleBuilder;
import org.apache.shardingsphere.infra.rule.builder.schema.SchemaRulesBuilderMaterials;
import org.apache.shardingsphere.infra.rule.identifier.scope.SchemaRule;
import org.apache.shardingsphere.singletable.config.SingleTableRuleConfiguration;
import org.apache.shardingsphere.singletable.rule.SingleTableRule;
import org.apache.shardingsphere.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.spi.ordered.OrderedSPIRegistry;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class SingleTableRuleBuilderTest {

    static {
        ShardingSphereServiceLoader.register(SchemaRuleBuilder.class);
    }

    @Test
    public void assertBuild() {
        Collection<SchemaRuleBuilder> registeredServiceBuilders = OrderedSPIRegistry.getRegisteredServices(SchemaRuleBuilder.class);
        SchemaRuleBuilder builder = registeredServiceBuilders.iterator().next();
        SchemaRulesBuilderMaterials materials = mock(SchemaRulesBuilderMaterials.class);
        Properties properties = new Properties();
        properties.setProperty(ConfigurationPropertyKey.CHECK_DUPLICATE_TABLE_ENABLED.getKey(), "false");
        when(materials.getProps()).thenReturn(new ConfigurationProperties(properties));
        SingleTableRuleConfiguration configuration = mock(SingleTableRuleConfiguration.class);
        ShardingSphereRule shardingSphereRule = mock(ShardingSphereRule.class);
        SchemaRule schemaRule = builder.build(materials, configuration, Arrays.asList(shardingSphereRule));
        assertThat(schemaRule, instanceOf(SingleTableRule.class));
    }
}
