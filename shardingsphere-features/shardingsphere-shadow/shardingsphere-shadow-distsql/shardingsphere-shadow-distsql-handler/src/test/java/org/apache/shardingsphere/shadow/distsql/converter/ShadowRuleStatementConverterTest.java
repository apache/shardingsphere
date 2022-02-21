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

package org.apache.shardingsphere.shadow.distsql.converter;

import org.apache.shardingsphere.distsql.parser.segment.AlgorithmSegment;
import org.apache.shardingsphere.shadow.api.config.ShadowRuleConfiguration;
import org.apache.shardingsphere.shadow.distsql.handler.converter.ShadowRuleStatementConverter;
import org.apache.shardingsphere.shadow.distsql.parser.segment.ShadowAlgorithmSegment;
import org.apache.shardingsphere.shadow.distsql.parser.segment.ShadowRuleSegment;
import org.apache.shardingsphere.shadow.spi.ShadowAlgorithm;
import org.apache.shardingsphere.spi.ShardingSphereServiceLoader;
import org.junit.Test;

import java.util.Collections;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public final class ShadowRuleStatementConverterTest {
    
    @Test
    public void assertConvert() {
        ShadowRuleConfiguration config = ShadowRuleStatementConverter.convert(Collections.singleton(createTableRuleSegment()));
        assertThat(config.getDataSources().get("ruleName").getSourceDataSourceName(), is("source"));
        assertThat(config.getDataSources().get("ruleName").getShadowDataSourceName(), is("shadow"));
        assertThat(config.getTables().size(), is(1));
        assertThat(config.getShadowAlgorithms().size(), is(1));
        assertThat(config.getShadowAlgorithms().get("algorithmsName").getProps().get("foo"), is("bar"));
    }
    
    private ShadowRuleSegment createTableRuleSegment() {
        ShardingSphereServiceLoader.register(ShadowAlgorithm.class);
        Properties props = new Properties();
        props.setProperty("foo", "bar");
        return new ShadowRuleSegment("ruleName", "source", "shadow",
                Collections.singletonMap("t_order", Collections.singleton(new ShadowAlgorithmSegment("algorithmsName", new AlgorithmSegment("type", props)))));
    }
}
