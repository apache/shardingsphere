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

package org.apache.shardingsphere.orchestration.internal.keygen;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.apache.shardingsphere.orchestration.reg.api.RegistryCenterConfiguration;
import org.apache.shardingsphere.orchestration.reg.zookeeper.curator.CuratorZookeeperRegistryCenter;
import org.apache.shardingsphere.spi.keygen.ShardingKeyGenerator;

import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.SynchronousQueue;

/**
 * Key generator implemented by leaf segment algorithms.
 *
 * @author wangguangyuan
 */
public final class LeafSegmentKeyGenerator implements ShardingKeyGenerator {

    private static final String TYPE = "LEAFSEGMENT";

    private static final String NAMESPACE = "leaf_segment";

    private static final String SLANTING_BAR = "/";

    private static final String REGULAR_PATTERN = "^((?!/).)*$";

    private static final String STEP = "10000";

    private static final String INITIAL_VALUE = "1";

    private static final float THRESHOLD = 0.5F;

    private boolean isInitialized = Boolean.FALSE;

    private CuratorZookeeperRegistryCenter leafRegistryCenter;

    private long id;

    private ExecutorService incrementCacheIdExecutor;

    private SynchronousQueue<Long> cacheIdQueue;

    private long step;

    @Getter
    @Setter
    private Properties properties = new Properties();

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public synchronized Comparable<?> generateKey() {
        String leafKey = getLeafKey();
        if (isInitialized == Boolean.FALSE) {
            initLeafSegmentKeyGenerator(leafKey);
            isInitialized = Boolean.TRUE;
            return id;
        }
        id = generateKeyWhenLeafKeyStoredInCenter(leafKey);
        return id;
    }

    private void initLeafSegmentKeyGenerator(final String leafKey) {
        leafRegistryCenter = new CuratorZookeeperRegistryCenter();
        RegistryCenterConfiguration leafConfiguration = getRegistryCenterConfiguration();
        leafRegistryCenter.init(leafConfiguration);
        if (leafRegistryCenter.isExisted(leafKey)) {
            id = incrementCacheId(leafKey, getStep());
        } else {
            id = getInitialValue();
            leafRegistryCenter.persist(leafKey, String.valueOf(id));
        }
        incrementCacheIdExecutor = Executors.newSingleThreadExecutor();
        cacheIdQueue = new SynchronousQueue<>();
        step = getStep();
    }

    private long generateKeyWhenLeafKeyStoredInCenter(final String leafKey) {
        ++id;
        if (((id % step) >= (step * THRESHOLD - 1)) && cacheIdQueue.isEmpty()) {
            incrementCacheIdAsynchronous(leafKey, step);
        }
        if ((id % step) == (step - 1)) {
            id = tryTakeCacheId();
        }
        return id;
    }

    private RegistryCenterConfiguration getRegistryCenterConfiguration() {
        RegistryCenterConfiguration result = new RegistryCenterConfiguration(TYPE, properties);
        result.setNamespace(NAMESPACE);
        result.setServerLists(getServerList());
        result.setDigest(getDigest());
        return result;
    }

    private void incrementCacheIdAsynchronous(final String leafKey, final long step) {
        incrementCacheIdExecutor.execute(new Runnable() {
            @Override
            public void run() {
                long id = incrementCacheId(leafKey, step);
                tryPutCacheId(id);
            }
        });
    }

    @SneakyThrows
    private long incrementCacheId(final String leafKey, final long step) {
        InterProcessMutex lock = leafRegistryCenter.initLock(leafKey);
        long result = Long.MIN_VALUE;
        boolean lockIsAcquired = leafRegistryCenter.tryLock(lock);
        if (lockIsAcquired) {
            result = updateCacheIdInCenter(leafKey, step);
            leafRegistryCenter.tryRelease(lock);
        }
        return result;
    }

    @SneakyThrows
    private void tryPutCacheId(final long id) {
        cacheIdQueue.put(id);
    }

    @SneakyThrows
    private long tryTakeCacheId() {
        return cacheIdQueue.take();
    }

    private long updateCacheIdInCenter(final String leafKey, final long step) {
        String cacheIdInString = leafRegistryCenter.getDirectly(leafKey);
        if (Strings.isNullOrEmpty(cacheIdInString)) {
            return Long.MIN_VALUE;
        }
        long cacheId = Long.parseLong(cacheIdInString);
        long result = cacheId + step;
        leafRegistryCenter.update(leafKey, String.valueOf(result));
        return result;
    }

    private long getStep() {
        long result = Long.parseLong(properties.getProperty("step", STEP));
        Preconditions.checkArgument(result > 0L && result < Long.MAX_VALUE);
        return result;
    }

    private long getInitialValue() {
        long result = Long.parseLong(properties.getProperty("initialValue", INITIAL_VALUE));
        Preconditions.checkArgument(result >= 0L && result < Long.MAX_VALUE);
        return result;
    }

    private String getLeafKey() {
        String leafKey = properties.getProperty("leaf.key");
        Preconditions.checkArgument(!Strings.isNullOrEmpty(leafKey));
        Preconditions.checkArgument(leafKey.matches(REGULAR_PATTERN));
        return SLANTING_BAR + leafKey;
    }

    private String getServerList() {
        String result = properties.getProperty("serverList");
        Preconditions.checkArgument(!Strings.isNullOrEmpty(result));
        return result;
    }

    private String getDigest() {
        return properties.getProperty("digest");
    }

}
