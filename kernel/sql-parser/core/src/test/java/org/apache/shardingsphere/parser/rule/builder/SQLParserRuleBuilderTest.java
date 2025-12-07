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

package org.apache.shardingsphere.parser.rule.builder;

import org.apache.shardingsphere.infra.rule.builder.global.GlobalRuleBuilder;
import org.apache.shardingsphere.infra.spi.type.ordered.OrderedSPILoader;
import org.apache.shardingsphere.parser.config.SQLParserRuleConfiguration;
import org.apache.shardingsphere.parser.rule.SQLParserRule;
import org.apache.shardingsphere.sql.parser.engine.api.CacheOption;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.isA;

class SQLParserRuleBuilderTest {
    
    @Test
    void assertBuild() {
        SQLParserRuleConfiguration ruleConfig = new SQLParserRuleConfiguration(new CacheOption(4, 64L), new CacheOption(8, 128L));
        SQLParserRuleBuilder builder = (SQLParserRuleBuilder) OrderedSPILoader.getServices(GlobalRuleBuilder.class, Collections.singleton(ruleConfig)).get(ruleConfig);
        assertThat(builder.build(ruleConfig, Collections.emptyList(), null), isA(SQLParserRule.class));
    }
}
