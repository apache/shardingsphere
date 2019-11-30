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
import com.ctrip.framework.apollo.openapi.client.ApolloOpenApiClient;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import lombok.SneakyThrows;
import org.apache.shardingsphere.orchestration.center.api.ConfigCenter;
import org.apache.shardingsphere.orchestration.center.instance.node.ConfigTreeNode;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.internal.util.reflection.FieldSetter;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Set;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class ApolloInstanceTest {
    
    private static ConfigCenter configCenter = new ApolloInstance();
    
    @BeforeClass
    @SneakyThrows
    public static void init() {
        Config apolloConfig = mock(Config.class);
        when(apolloConfig.getProperty("test.children.1", "")).thenReturn("value1");
        when(apolloConfig.getProperty("test2", "")).thenReturn("value2");
        FieldSetter.setField(configCenter, ApolloInstance.class.getDeclaredField("apolloConfig"), apolloConfig);
        Set<String> instanceKeys = Sets.newHashSet();
        instanceKeys.add("test.children.1");
        instanceKeys.add("test.children.2");
        instanceKeys.add("test1.children.3");
        instanceKeys.add("test2");
        ConfigTreeNode root = new ConfigTreeNode(null, "/", Sets.<ConfigTreeNode>newHashSet());
        root.init(instanceKeys, ".");
        FieldSetter.setField(configCenter, ApolloInstance.class.getDeclaredField("tree"), root);
        ApolloOpenApiClient client = mock(ApolloOpenApiClient.class);
        FieldSetter.setField(configCenter, ApolloInstance.class.getDeclaredField("client"), client);
    }
    
    @Test
    public void assertGet() {
        assertThat(configCenter.get("/test2"), is("value2"));
        assertThat(configCenter.get("/test/children/1"), is("value1"));
    }
    
    @Test
    public void assertGetChildrenKeys() {
        assertThat(configCenter.getChildrenKeys("/").size(), is(3));
        assertThat(configCenter.getChildrenKeys("/test/children").size(), is(2));
        assertEquals(configCenter.getChildrenKeys("/test1/children"), Lists.newArrayList("/test1/children/3"));
    }
    
    @Test
    public void assertPersist() {
        configCenter.persist("/test1/children2/4", "value4");
        assertThat(configCenter.getChildrenKeys("/test1/children2").size(), is(1));
        assertEquals(configCenter.getChildrenKeys("/test1/children2"), Lists.newArrayList("/test1/children2/4"));
    }
}
