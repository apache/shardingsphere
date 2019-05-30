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
import lombok.Getter;
import lombok.Setter;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.apache.shardingsphere.orchestration.reg.api.RegistryCenterConfiguration;
import org.apache.shardingsphere.orchestration.reg.zookeeper.curator.CuratorZookeeperRegistryCenter;
import org.apache.shardingsphere.spi.keygen.ShardingKeyGenerator;

import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.SynchronousQueue;

/**
 * Created by Jason on 2019/4/28.
 */
public class LeafSegmentKeyGenerator implements ShardingKeyGenerator {

    private CuratorZookeeperRegistryCenter leafCuratorZookeeper;

    @Getter
    @Setter
    private Properties properties = new Properties();

    private long id;

    private static final String TYPE = "LEAFSEGMENT";

    private static final String NAMESPACE = "leaf_segment";

    private ExecutorService incrementCacheIdExecutor;

    private boolean isInitialized = Boolean.FALSE;

    private SynchronousQueue<Long> cacheIdQueue;

    private static final float THRESHOLD=0.5F;

    private long step;

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public synchronized Comparable<?> generateKey() {
        String leafKey = (String)properties.get("leaf.key");
        if (isInitialized == Boolean.FALSE) {
            initLeafSegmentKeyGenerator(leafKey);
            isInitialized = Boolean.TRUE;
            return id;
        }
        id = generateKeyWhenLeafKeyStoredInCenter(leafKey);
        return id;
    }

    private void initLeafSegmentKeyGenerator(final String leafKey){
        RegistryCenterConfiguration leafConfiguration = new RegistryCenterConfiguration(TYPE,properties);
        leafConfiguration.setNamespace(NAMESPACE);
        leafConfiguration.setServerLists(getServerList());
        leafConfiguration.setDigest(getDigest());
        leafCuratorZookeeper = new CuratorZookeeperRegistryCenter ();
        leafCuratorZookeeper.init(leafConfiguration);
        if(leafCuratorZookeeper.isExisted(leafKey)){
            id = incrementCacheId(leafKey,getStep());
        }else{
            id = getInitialValue();
            leafCuratorZookeeper.persist(leafKey,String.valueOf(id));
        }
        incrementCacheIdExecutor = Executors.newSingleThreadExecutor();
        cacheIdQueue = new SynchronousQueue<>();
        step = getStep();
    }

    private long generateKeyWhenLeafKeyStoredInCenter(final String leafKey){
        ++id;
        if(((id%step) >= (step*THRESHOLD-1)) && cacheIdQueue.isEmpty()){
            incrementCacheIdAsynchronous(leafKey,step);
        }
        if((id%step) == (step-1)){
            id = tryTakeCacheId();
        }
        return id;
    }

    private void incrementCacheIdAsynchronous(final String leafKey,final long step){
        incrementCacheIdExecutor.execute(new Runnable() {
            @Override
            public void run() {
                long id = incrementCacheId(leafKey,step);
                tryPutCacheId(id);
            }
        });
    }

    private long tryTakeCacheId(){
        long id = Long.MIN_VALUE;
        try{
            id = cacheIdQueue.take();
        }catch (Exception ex){
            Thread.currentThread().interrupt();
        }
        return id;
    }

    private void tryPutCacheId(long id){
        try{
            cacheIdQueue.put(id);
        }catch (Exception ex){
            Thread.currentThread().interrupt();
        }
    }

    private long getStep(){
        long result = Long.parseLong(properties.getProperty("step"));
        Preconditions.checkArgument(result >= 0L && result < Long.MAX_VALUE);
        return result;
    }

    private long getInitialValue(){
        long result = Long.parseLong(properties.getProperty("initialValue"));
        Preconditions.checkArgument(result >= 0L && result < Long.MAX_VALUE);
        return result;
    }

    private String getServerList(){
        String result = (String)properties.get("serverList");
        String pattern = "(\\d|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])\\." +
                "(\\d|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])\\.(\\d|[1-9]\\d" +
                "|1\\d{2}|2[0-4]\\d|25[0-5])\\.(\\d|[1-9]\\d|1\\d{2}|2[0-4]" +
                "\\d|25[0-5]):(6[0-4]\\d{4}|65[0-4]\\d{2}|655[0-2]\\d|6553[0-5]" +
                "|[1-5]\\d{4}|[1-9]\\d{3}|[1-9]\\d{2}|[1-9]\\d|[0-9])((,(\\d|[1-9]\\d" +
                "|1\\d{2}|2[0-4]\\d|25[0-5])\\.(\\d|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])" +
                "\\.(\\d|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])\\.(\\d|[1-9]\\d|1\\d{2}|2[0-4]" +
                "\\d|25[0-5]):(6[0-4]\\d{4}|65[0-4]\\d{2}|655[0-2]\\d|6553[0-5]|[1-5]\\d{4}" +
                "|[1-9]\\d{3}|[1-9]\\d{2}|[1-9]\\d|[0-9]))*)";
        Preconditions.checkArgument(result.matches(pattern));
        return result;
    }

    private String getDigest(){
        String result = (String)properties.get("digest");
        String pattern = "\\w+:\\w+|";
        Preconditions.checkArgument(result.matches(pattern));
        return result;
    }

    private long incrementCacheId(final String leafKey,final long step){
        InterProcessMutex lock = leafCuratorZookeeper.initLock(leafKey);
        long result=Long.MIN_VALUE;
        boolean lockIsAcquired = leafCuratorZookeeper.tryLock(lock);
        if ( lockIsAcquired ) {
            result = updateCacheIdInCenter(leafKey, step);
            leafCuratorZookeeper.tryRelease(lock);
        }
        return  result;
    }

    private long updateCacheIdInCenter(final String leafKey,final long step){
        long cacheId = Long.parseLong(leafCuratorZookeeper.getDirectly(leafKey));
        long result = cacheId+step;
        leafCuratorZookeeper.update(leafKey, String.valueOf(result));
        return result;
    }

}