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

package org.apache.shardingsphere.readwritesplitting.datanode;

import org.apache.shardingsphere.infra.datanode.DataNode;
import org.apache.shardingsphere.infra.datanode.DataNodeBuilder;
import org.apache.shardingsphere.infra.rule.attribute.RuleAttributes;
import org.apache.shardingsphere.infra.rule.attribute.datasource.DataSourceMapperRuleAttribute;
import org.apache.shardingsphere.infra.spi.type.ordered.OrderedSPILoader;
import org.apache.shardingsphere.readwritesplitting.rule.ReadwriteSplittingRule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReadwriteSplittingDataNodeBuilderTest {
    
    @Mock
    private ReadwriteSplittingRule rule;
    
    @Mock
    private DataSourceMapperRuleAttribute dataSourceMapperRuleAttribute;
    
    private ReadwriteSplittingDataNodeBuilder builder;
    
    @BeforeEach
    void setUp() {
        when(dataSourceMapperRuleAttribute.getDataSourceMapper()).thenReturn(Collections.singletonMap("foo_db", Arrays.asList("foo_write_db", "foo_read_db")));
        when(rule.getAttributes()).thenReturn(new RuleAttributes(dataSourceMapperRuleAttribute));
        builder = (ReadwriteSplittingDataNodeBuilder) OrderedSPILoader.getServices(DataNodeBuilder.class, Collections.singleton(rule)).get(rule);
    }
    
    @Test
    void assertBuild() {
        Collection<DataNode> actual = builder.build(Collections.singleton(new DataNode("foo_db.foo_tbl")), rule);
        assertThat(actual, is(Arrays.asList(new DataNode("foo_write_db.foo_tbl"), new DataNode("foo_read_db.foo_tbl"))));
    }
}
