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

package org.apache.shardingsphere.single.distsql.handler.provider;

import org.apache.shardingsphere.single.api.config.SingleRuleConfiguration;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SingleRuleConfigurationToDistSQLConverterTest {
    
    @Test
    void assertConvert() {
        SingleRuleConfiguration singleRuleConfiguration = new SingleRuleConfiguration(new LinkedList<>(Arrays.asList("t_0", "t_1")), "foo_ds");
        SingleRuleConfigurationToDistSQLConverter singleRuleConfigurationToDistSQLConverter = new SingleRuleConfigurationToDistSQLConverter();
        assertThat(singleRuleConfigurationToDistSQLConverter.convert(singleRuleConfiguration),
                is("LOAD SINGLE TABLE t_0,t_1;" + System.lineSeparator() + System.lineSeparator() + "SET DEFAULT SINGLE TABLE STORAGE UNIT = foo_ds;"));
    }
    
    @Test
    void assertConvertWithoutDefaultDatasourceAndTables() {
        SingleRuleConfiguration singleRuleConfiguration = mock(SingleRuleConfiguration.class);
        when(singleRuleConfiguration.getDefaultDataSource()).thenReturn(Optional.empty());
        when(singleRuleConfiguration.getTables()).thenReturn(Collections.emptyList());
        SingleRuleConfigurationToDistSQLConverter singleRuleConfigurationToDistSQLConverter = new SingleRuleConfigurationToDistSQLConverter();
        assertThat(singleRuleConfigurationToDistSQLConverter.convert(singleRuleConfiguration), is(""));
    }
    
    @Test
    void assertGetType() {
        SingleRuleConfigurationToDistSQLConverter singleRuleConfigurationToDistSQLConverter = new SingleRuleConfigurationToDistSQLConverter();
        assertThat(singleRuleConfigurationToDistSQLConverter.getType().getName(), is("org.apache.shardingsphere.single.api.config.SingleRuleConfiguration"));
    }
}
