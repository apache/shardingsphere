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

package org.apache.shardingsphere.shadow.distsql.handler.converter;

import org.apache.shardingsphere.distsql.segment.AlgorithmSegment;
import org.apache.shardingsphere.infra.util.props.PropertiesBuilder;
import org.apache.shardingsphere.infra.util.props.PropertiesBuilder.Property;
import org.apache.shardingsphere.shadow.config.ShadowRuleConfiguration;
import org.apache.shardingsphere.shadow.config.datasource.ShadowDataSourceConfiguration;
import org.apache.shardingsphere.shadow.distsql.segment.ShadowAlgorithmSegment;
import org.apache.shardingsphere.shadow.distsql.segment.ShadowRuleSegment;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

class ShadowRuleStatementConverterTest {
    
    @Test
    void assertConvert() {
        ShadowRuleConfiguration config = ShadowRuleStatementConverter.convert(Collections.singleton(createTableRuleSegment()));
        ShadowDataSourceConfiguration shadowDataSourceConfig = config.getDataSources().iterator().next();
        assertThat(shadowDataSourceConfig.getProductionDataSourceName(), is("source"));
        assertThat(shadowDataSourceConfig.getShadowDataSourceName(), is("shadow"));
        assertThat(config.getTables().size(), is(1));
        assertThat(config.getShadowAlgorithms().size(), is(1));
        assertThat(config.getShadowAlgorithms().get("algorithmsName").getProps().getProperty("foo"), is("bar"));
    }
    
    private ShadowRuleSegment createTableRuleSegment() {
        return new ShadowRuleSegment("ruleName", "source", "shadow", Collections.singletonMap("t_order",
                Collections.singleton(new ShadowAlgorithmSegment("algorithmsName", new AlgorithmSegment("type", PropertiesBuilder.build(new Property("foo", "bar")))))));
    }
}
