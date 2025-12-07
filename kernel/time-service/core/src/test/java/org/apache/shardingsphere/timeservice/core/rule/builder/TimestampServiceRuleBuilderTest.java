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

package org.apache.shardingsphere.timeservice.core.rule.builder;

import org.apache.shardingsphere.infra.config.rule.scope.GlobalRuleConfiguration;
import org.apache.shardingsphere.infra.rule.builder.global.GlobalRuleBuilder;
import org.apache.shardingsphere.infra.spi.type.ordered.OrderedSPILoader;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.timeservice.config.TimestampServiceRuleConfiguration;
import org.apache.shardingsphere.timeservice.core.rule.TimestampServiceRule;
import org.apache.shardingsphere.timeservice.spi.TimestampService;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.util.Collections;
import java.util.Map;
import java.util.Properties;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;

class TimestampServiceRuleBuilderTest {
    
    @SuppressWarnings({"rawtypes", "unchecked"})
    @Test
    void assertBuild() {
        TimestampServiceRuleConfiguration ruleConfig = new TimestampServiceRuleConfiguration("System", new Properties());
        Map<GlobalRuleConfiguration, GlobalRuleBuilder> builders = OrderedSPILoader.getServices(GlobalRuleBuilder.class, Collections.singleton(ruleConfig));
        TimestampService timestampService = mock(TimestampService.class);
        try (MockedStatic<TypedSPILoader> typedSpiLoader = mockStatic(TypedSPILoader.class)) {
            typedSpiLoader.when(() -> TypedSPILoader.getService(TimestampService.class, "System", new Properties())).thenReturn(timestampService);
            assertThat(builders.get(ruleConfig).build(ruleConfig, Collections.emptyList(), null), isA(TimestampServiceRule.class));
        }
    }
}
