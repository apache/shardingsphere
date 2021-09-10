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

import java.util.Collection;
import org.apache.shardingsphere.infra.rule.builder.schema.DefaultSchemaRuleConfigurationBuilder;
import org.apache.shardingsphere.singletable.config.SingleTableRuleConfiguration;
import org.apache.shardingsphere.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.spi.ordered.OrderedSPIRegistry;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertThat;

@RunWith(MockitoJUnitRunner.class)
public final class DefaultSingleTableSchemaRuleConfigurationBuilderTest {

    static {
        ShardingSphereServiceLoader.register(DefaultSchemaRuleConfigurationBuilder.class);
    }

    @Test
    public void assertBuild() {
        Collection<DefaultSchemaRuleConfigurationBuilder> registeredServices = OrderedSPIRegistry.getRegisteredServices(DefaultSchemaRuleConfigurationBuilder.class);
        DefaultSchemaRuleConfigurationBuilder builder = registeredServices.iterator().next();
        assertThat(builder.build(), instanceOf(SingleTableRuleConfiguration.class));
    }
}
