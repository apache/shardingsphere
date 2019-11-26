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

package org.apache.shardingsphere.orchestration.center.instance;

import com.ctrip.framework.apollo.Config;
import com.ctrip.framework.apollo.core.ConfigConsts;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import lombok.SneakyThrows;
import org.apache.shardingsphere.orchestration.center.api.ConfigCenter;
import org.apache.shardingsphere.orchestration.center.configuration.InstanceConfiguration;
import org.apache.shardingsphere.orchestration.center.instance.node.ConfigTreeNode;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.internal.util.reflection.FieldSetter;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class ApolloInstanceTest {
    
    private static ConfigCenter configCenter = new ApolloInstance();
    
    @Before
    @SneakyThrows
    public void setUp() {
        Properties properties = initProperties();
        InstanceConfiguration configuration = initConfigCenter(properties);
        initApolloConfig();
        FieldSetter.setField(configCenter, ApolloInstance.class.getDeclaredField("namespace"), configuration.getNamespace());
        FieldSetter.setField(configCenter, ApolloInstance.class.getDeclaredField("appId"), properties.getProperty("appId"));
        FieldSetter.setField(configCenter, ApolloInstance.class.getDeclaredField("env"), properties.getProperty("env"));
        FieldSetter.setField(configCenter, ApolloInstance.class.getDeclaredField("clusterName"), properties.getProperty("clusterName"));
        FieldSetter.setField(configCenter, ApolloInstance.class.getDeclaredField("administrator"), properties.getProperty("administrator"));
        ConfigTreeNode node = mock(ConfigTreeNode.class);
        when(node.getChildrenKeys("/test/children")).thenReturn(Sets.newHashSet("/test/children/1"));
        FieldSetter.setField(configCenter, ApolloInstance.class.getDeclaredField("tree"), node);
    }
    
    @SneakyThrows
    private void initApolloConfig() {
        Config apolloConfig = mock(Config.class);
        when(apolloConfig.getProperty("test.children.1", "")).thenReturn("value1");
        FieldSetter.setField(configCenter, ApolloInstance.class.getDeclaredField("apolloConfig"), apolloConfig);
    }
    
    private static Properties initProperties() {
        Properties result = new Properties();
        result.setProperty("portalUrl", "http://apollo-portal-url");
        result.setProperty("token", "xxxxxxxx");
        result.setProperty("administrator", "apollo");
        result.setProperty("connectTimeout", "1000");
        result.setProperty("readTimeout", "1500");
        result.setProperty("appId", "APOLLO_SHARDING_SPHERE");
        result.setProperty("env", "DEV");
        result.setProperty("clusterName", ConfigConsts.CLUSTER_NAME_DEFAULT);
        return result;
    }
    
    private static InstanceConfiguration initConfigCenter(final Properties properties) {
        InstanceConfiguration result = new InstanceConfiguration(configCenter.getType(), properties);
        result.setServerLists("http://config-service-url");
        result.setNamespace("orchestration");
        return result;
    }
    
    @Test
    public void assertGet() {
        assertThat(configCenter.get("/test/children/1"), is("value1"));
    }
    
    @Test
    public void assertGetChildrenKeys() {
        assertEquals(configCenter.getChildrenKeys("/test/children"), Lists.newArrayList("/test/children/1"));
    }
}
