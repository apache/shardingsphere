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

package org.apache.shardingsphere.infra.metadata;

import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.database.DefaultDatabase;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.resource.ShardingSphereResource;
import org.apache.shardingsphere.infra.metadata.database.rule.ShardingSphereRuleMetaData;
import org.apache.shardingsphere.infra.rule.identifier.type.ResourceHeldRule;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class ShardingSphereMetaDataTest {
    @Mock
    private ShardingSphereRuleMetaData globalRuleMetaData;
    
    @Mock
    private ResourceHeldRule<?> shardingRuleMetaDataResourceHeldRule;
    
    @Mock
    private ResourceHeldRule<?> globalRuleMedataResourceHeldRule;
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ShardingSphereDatabase shardingSphereDatabase;
    
    @Mock
    private ShardingSphereResource shardingSphereResource;
    
    @Mock
    private DataSource mockedDataSource;
    
    private ShardingSphereMetaData shardingSphereMetaData;
    
    @Before
    public void setUp() throws SQLException {
        when(shardingSphereDatabase.getName()).thenReturn(DefaultDatabase.LOGIC_NAME);
        when(shardingSphereDatabase.getResource()).thenReturn(shardingSphereResource);
        when(shardingSphereDatabase.getResource().getDataSources()).thenReturn(Collections.singletonMap(DefaultDatabase.LOGIC_NAME, mockedDataSource));
        when(shardingSphereDatabase.getRuleMetaData().findRules(ResourceHeldRule.class)).thenReturn(Collections.singletonList(shardingRuleMetaDataResourceHeldRule));
        when(globalRuleMetaData.findRules(ResourceHeldRule.class)).thenReturn(Collections.singletonList(globalRuleMedataResourceHeldRule));
        shardingSphereMetaData = new ShardingSphereMetaData(new LinkedHashMap<>(), globalRuleMetaData, new ConfigurationProperties(new Properties()));
    }
    
    @Test
    public void assertDropDatabase() {
        shardingSphereMetaData.getDatabases().put(DefaultDatabase.LOGIC_NAME, shardingSphereDatabase);
        shardingSphereMetaData.dropDatabase(DefaultDatabase.LOGIC_NAME);
        Assert.assertThat(shardingSphereMetaData.getDatabases(), is(Collections.emptyMap()));
        verify(shardingSphereResource).close(mockedDataSource);
        verify(shardingRuleMetaDataResourceHeldRule).closeStaleResource(DefaultDatabase.LOGIC_NAME);
        verify(globalRuleMedataResourceHeldRule).closeStaleResource(DefaultDatabase.LOGIC_NAME);
    }
}
