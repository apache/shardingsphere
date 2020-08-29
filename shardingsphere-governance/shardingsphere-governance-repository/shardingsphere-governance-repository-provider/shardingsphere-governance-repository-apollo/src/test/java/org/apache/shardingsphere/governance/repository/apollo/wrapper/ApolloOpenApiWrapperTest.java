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

package org.apache.shardingsphere.governance.repository.apollo.wrapper;

import com.ctrip.framework.apollo.openapi.client.ApolloOpenApiClient;
import com.ctrip.framework.apollo.openapi.dto.NamespaceReleaseDTO;
import com.ctrip.framework.apollo.openapi.dto.OpenItemDTO;
import lombok.SneakyThrows;
import org.apache.shardingsphere.governance.repository.apollo.ApolloProperties;
import org.apache.shardingsphere.governance.repository.apollo.ApolloPropertyKey;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.internal.util.reflection.FieldSetter;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Properties;

import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class ApolloOpenApiWrapperTest {
    
    private static final String NAME_SPACE = "governance";
    
    private static final String PORTAL_URL = "http://127.0.0.1";
    
    private static final String TOKEN = "testToken";
    
    @Mock
    private ApolloOpenApiClient client;
    
    @Mock
    private OpenItemDTO openItemDTO;
    
    private ApolloOpenApiWrapper apolloOpenApiWrapper;
    
    @SneakyThrows({NoSuchFieldException.class, SecurityException.class})
    @Before
    public void setUp() {
        Properties props = new Properties();
        props.setProperty(ApolloPropertyKey.PORTAL_URL.getKey(), PORTAL_URL);
        props.setProperty(ApolloPropertyKey.TOKEN.getKey(), TOKEN);
        apolloOpenApiWrapper = new ApolloOpenApiWrapper("apollo_namespace", new ApolloProperties(props));
        FieldSetter.setField(apolloOpenApiWrapper, ApolloOpenApiWrapper.class.getDeclaredField("client"), client);
        FieldSetter.setField(apolloOpenApiWrapper, ApolloOpenApiWrapper.class.getDeclaredField("namespace"), NAME_SPACE);
        FieldSetter.setField(apolloOpenApiWrapper, ApolloOpenApiWrapper.class.getDeclaredField("appId"), ApolloPropertyKey.APP_ID.getDefaultValue());
        FieldSetter.setField(apolloOpenApiWrapper, ApolloOpenApiWrapper.class.getDeclaredField("env"), ApolloPropertyKey.ENV.getDefaultValue());
        FieldSetter.setField(apolloOpenApiWrapper, ApolloOpenApiWrapper.class.getDeclaredField("clusterName"), ApolloPropertyKey.CLUSTER_NAME.getDefaultValue());
        FieldSetter.setField(apolloOpenApiWrapper, ApolloOpenApiWrapper.class.getDeclaredField("administrator"), ApolloPropertyKey.ADMINISTRATOR.getDefaultValue());
    }
    
    @Test
    public void getValue() {
        apolloOpenApiWrapper.getValue("test.children.0");
        verify(client).getItem(ApolloPropertyKey.APP_ID.getDefaultValue(), ApolloPropertyKey.ENV.getDefaultValue(),
                ApolloPropertyKey.CLUSTER_NAME.getDefaultValue(), NAME_SPACE, "test.children.0");
    }
    
    @Test
    public void getValueNotNull() {
        when(client.getItem(ApolloPropertyKey.APP_ID.getDefaultValue(), ApolloPropertyKey.ENV.getDefaultValue(),
                ApolloPropertyKey.CLUSTER_NAME.getDefaultValue(), NAME_SPACE, "test.children.0")).thenReturn(openItemDTO);
        assertNull(apolloOpenApiWrapper.getValue("test.children.0"));
    }
    
    @Test
    public void persist() {
        apolloOpenApiWrapper.persist("test.children.0", "value0");
        verify(client).createOrUpdateItem(anyString(), anyString(), anyString(), anyString(), any(OpenItemDTO.class));
        verify(client).publishNamespace(anyString(), anyString(), anyString(), anyString(), any(NamespaceReleaseDTO.class));
    }
    
    @Test
    public void remove() {
        apolloOpenApiWrapper.remove("test.children.0");
        verify(client).removeItem(anyString(), anyString(), anyString(), anyString(), eq("test.children.0"), anyString());
    }
}
