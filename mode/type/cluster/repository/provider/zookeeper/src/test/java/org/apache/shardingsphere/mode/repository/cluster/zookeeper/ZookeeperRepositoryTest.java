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

package org.apache.shardingsphere.mode.repository.cluster.zookeeper;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.api.BackgroundVersionable;
import org.apache.curator.framework.api.CreateBuilder;
import org.apache.curator.framework.api.DeleteBuilder;
import org.apache.curator.framework.api.ExistsBuilder;
import org.apache.curator.framework.api.GetChildrenBuilder;
import org.apache.curator.framework.api.GetDataBuilder;
import org.apache.curator.framework.api.ProtectACLCreateModeStatPathAndBytesable;
import org.apache.curator.framework.api.SetDataBuilder;
import org.apache.shardingsphere.mode.repository.cluster.exception.ClusterRepositoryPersistException;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException.ConnectionLossException;
import org.apache.zookeeper.KeeperException.NoNodeException;
import org.apache.zookeeper.data.Stat;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.internal.configuration.plugins.Plugins;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ZookeeperRepositoryTest {
    
    private ZookeeperRepository repository;
    
    private RepositoryClientFixture fixture;
    
    @BeforeEach
    void setUp() throws Exception {
        repository = new ZookeeperRepository();
        fixture = new RepositoryClientFixture();
        Plugins.getMemberAccessor().set(ZookeeperRepository.class.getDeclaredField("client"), repository, fixture.createClient());
    }
    
    @AfterEach
    void tearDown() {
        repository.close();
    }
    
    @Test
    void assertPersist() {
        repository.persist("/test", "value1");
        assertThat(repository.query("/test"), is("value1"));
    }
    
    @Test
    void assertUpdate() {
        repository.persist("/test", "value1");
        repository.persist("/test", "value2");
        assertThat(repository.query("/test"), is("value2"));
    }
    
    @Test
    void assertPersistThrowsExceptionWhenConnectionLost() {
        fixture.throwConnectionLossOnCheckExists = true;
        assertThrows(ClusterRepositoryPersistException.class, () -> repository.persist("/test", "value"));
    }
    
    @Test
    void assertGetChildrenKeysThrowsExceptionWhenConnectionLost() {
        fixture.throwConnectionLossOnGetChildren = true;
        assertThrows(ClusterRepositoryPersistException.class, () -> repository.getChildrenKeys("/test/children/keys"));
    }
    
    @Test
    void assertIsExistedThrowsExceptionWhenConnectionLost() {
        fixture.throwConnectionLossOnCheckExists = true;
        assertThrows(ClusterRepositoryPersistException.class, () -> repository.isExisted("/test"));
    }
    
    @Test
    void assertPersistEphemeralNotExist() {
        repository.persistEphemeral("/test/ephemeral", "value3");
        assertThat(repository.query("/test/ephemeral"), is("value3"));
    }
    
    @Test
    void assertPersistEphemeralThrowsExceptionWhenConnectionLost() {
        fixture.throwConnectionLossOnCheckExists = true;
        assertThrows(ClusterRepositoryPersistException.class, () -> repository.persistEphemeral("/test/ephemeral", "value3"));
    }
    
    @Test
    void assertPersistEphemeralExist() {
        repository.persistEphemeral("/test/ephemeral", "value3");
        repository.persistEphemeral("/test/ephemeral", "value4");
        assertThat(repository.query("/test/ephemeral"), is("value4"));
    }
    
    @Test
    void assertGetChildrenKeys() {
        repository.persist("/test/children/keys/1", "value1");
        repository.persist("/test/children/keys/2", "value2");
        List<String> actual = repository.getChildrenKeys("/test/children/keys");
        assertThat(actual, is(Arrays.asList("2", "1")));
    }
    
    @Test
    void assertDeleteNotExistKey() {
        repository.delete("/test/children/1");
        assertNull(repository.query("/test/children/1"));
    }
    
    @Test
    void assertDeleteExistKey() {
        repository.persist("/test/children/1", "value1");
        repository.delete("/test/children/1");
        assertNull(repository.query("/test/children/1"));
    }
    
    private static final class RepositoryClientFixture {
        
        private final Map<String, byte[]> persistentData = new LinkedHashMap<>();
        
        private final Map<String, byte[]> ephemeralData = new LinkedHashMap<>();
        
        private boolean throwConnectionLossOnCheckExists;
        
        private boolean throwConnectionLossOnGetChildren;
        
        private CuratorFramework createClient() {
            return (CuratorFramework) Proxy.newProxyInstance(CuratorFramework.class.getClassLoader(), new Class[]{CuratorFramework.class}, this::handleClientInvocation);
        }
        
        private static Object getDefaultValue(final Class<?> returnType) {
            if (!returnType.isPrimitive()) {
                return null;
            }
            if (boolean.class == returnType) {
                return false;
            }
            if (byte.class == returnType) {
                return (byte) 0;
            }
            if (short.class == returnType) {
                return (short) 0;
            }
            if (int.class == returnType) {
                return 0;
            }
            if (long.class == returnType) {
                return 0L;
            }
            if (float.class == returnType) {
                return 0F;
            }
            if (double.class == returnType) {
                return 0D;
            }
            if (char.class == returnType) {
                return '\0';
            }
            return null;
        }
        
        private Object handleClientInvocation(final Object proxy, final Method method, final Object[] args) {
            switch (method.getName()) {
                case "checkExists":
                    return createExistsBuilder();
                case "create":
                    return createCreateBuilder();
                case "setData":
                    return createSetDataBuilder();
                case "delete":
                    return createDeleteBuilder();
                case "getChildren":
                    return createGetChildrenBuilder();
                case "getData":
                    return createGetDataBuilder();
                case "close":
                    return null;
                default:
                    return getDefaultValue(method.getReturnType());
            }
        }
        
        private ExistsBuilder createExistsBuilder() {
            return (ExistsBuilder) Proxy.newProxyInstance(ExistsBuilder.class.getClassLoader(), new Class[]{ExistsBuilder.class}, (proxy, method, args) -> {
                if ("forPath".equals(method.getName())) {
                    if (throwConnectionLossOnCheckExists) {
                        throw new ConnectionLossException();
                    }
                    String key = (String) args[0];
                    return persistentData.containsKey(key) || ephemeralData.containsKey(key) ? new Stat() : null;
                }
                return getDefaultValue(method.getReturnType());
            });
        }
        
        private CreateBuilder createCreateBuilder() {
            ProtectACLCreateModeStatPathAndBytesable<String> protect = (ProtectACLCreateModeStatPathAndBytesable<String>) Proxy.newProxyInstance(
                    ProtectACLCreateModeStatPathAndBytesable.class.getClassLoader(), new Class[]{ProtectACLCreateModeStatPathAndBytesable.class}, new CreateInvocationHandler());
            return (CreateBuilder) Proxy.newProxyInstance(CreateBuilder.class.getClassLoader(), new Class[]{CreateBuilder.class}, (proxy, method, args) -> {
                if ("creatingParentsIfNeeded".equals(method.getName())) {
                    return protect;
                }
                return getDefaultValue(method.getReturnType());
            });
        }
        
        private SetDataBuilder createSetDataBuilder() {
            return (SetDataBuilder) Proxy.newProxyInstance(SetDataBuilder.class.getClassLoader(), new Class[]{SetDataBuilder.class},
                    (proxy, method, args) -> {
                        if ("forPath".equals(method.getName())) {
                            return handleSetDataForPath(args);
                        }
                        return getDefaultValue(method.getReturnType());
                    });
        }
        
        private DeleteBuilder createDeleteBuilder() {
            BackgroundVersionable backgroundVersionable = (BackgroundVersionable) Proxy.newProxyInstance(
                    BackgroundVersionable.class.getClassLoader(), new Class[]{BackgroundVersionable.class}, (proxy, method, args) -> {
                        if ("forPath".equals(method.getName())) {
                            deleteRecursively((String) args[0]);
                            return null;
                        }
                        return getDefaultValue(method.getReturnType());
                    });
            return (DeleteBuilder) Proxy.newProxyInstance(DeleteBuilder.class.getClassLoader(), new Class[]{DeleteBuilder.class}, (proxy, method, args) -> {
                if ("deletingChildrenIfNeeded".equals(method.getName())) {
                    return backgroundVersionable;
                }
                return getDefaultValue(method.getReturnType());
            });
        }
        
        private GetChildrenBuilder createGetChildrenBuilder() {
            return (GetChildrenBuilder) Proxy.newProxyInstance(GetChildrenBuilder.class.getClassLoader(), new Class[]{GetChildrenBuilder.class}, (proxy, method, args) -> {
                if ("forPath".equals(method.getName())) {
                    if (throwConnectionLossOnGetChildren) {
                        throw new ConnectionLossException();
                    }
                    return getChildren((String) args[0]);
                }
                return getDefaultValue(method.getReturnType());
            });
        }
        
        private GetDataBuilder createGetDataBuilder() {
            return (GetDataBuilder) Proxy.newProxyInstance(GetDataBuilder.class.getClassLoader(), new Class[]{GetDataBuilder.class},
                    (proxy, method, args) -> {
                        if ("forPath".equals(method.getName())) {
                            return handleGetDataForPath(args);
                        }
                        return getDefaultValue(method.getReturnType());
                    });
        }
        
        private List<String> getChildren(final String key) {
            List<String> result = new ArrayList<>();
            collectChildren(result, persistentData, key);
            collectChildren(result, ephemeralData, key);
            return result;
        }
        
        private void collectChildren(final List<String> result, final Map<String, byte[]> data, final String key) {
            String prefix = key + "/";
            for (String each : data.keySet()) {
                if (each.startsWith(prefix)) {
                    String child = each.substring(prefix.length());
                    if (!child.contains("/")) {
                        result.add(child);
                    }
                }
            }
        }
        
        private void deleteRecursively(final String key) {
            deleteEntries(persistentData, key);
            deleteEntries(ephemeralData, key);
        }
        
        private Stat handleSetDataForPath(final Object[] args) {
            String key = (String) args[0];
            byte[] value = (byte[]) args[1];
            if (persistentData.containsKey(key)) {
                persistentData.put(key, value);
            } else if (ephemeralData.containsKey(key)) {
                ephemeralData.put(key, value);
            }
            return new Stat();
        }
        
        private byte[] handleGetDataForPath(final Object[] args) throws NoNodeException {
            byte[] result = persistentData.get(args[0]);
            if (null == result) {
                result = ephemeralData.get(args[0]);
            }
            if (null == result) {
                throw new NoNodeException();
            }
            return result;
        }
        
        private void deleteEntries(final Map<String, byte[]> data, final String key) {
            List<String> keys = new ArrayList<>(data.keySet());
            for (String each : keys) {
                if (key.equals(each) || each.startsWith(key + "/")) {
                    data.remove(each);
                }
            }
        }
        
        private final class CreateInvocationHandler implements java.lang.reflect.InvocationHandler {
            
            private CreateMode createMode = CreateMode.PERSISTENT;
            
            @Override
            public Object invoke(final Object proxy, final Method method, final Object[] args) {
                if ("withMode".equals(method.getName())) {
                    createMode = (CreateMode) args[0];
                    return proxy;
                }
                if ("forPath".equals(method.getName())) {
                    Map<String, byte[]> target = CreateMode.EPHEMERAL == createMode ? ephemeralData : persistentData;
                    target.put((String) args[0], (byte[]) args[1]);
                    return args[0];
                }
                return getDefaultValue(method.getReturnType());
            }
        }
    }
}
