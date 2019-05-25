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

package org.apache.shardingsphere.core.strategy.keygen;

import com.google.common.base.Preconditions;
import lombok.Getter;
import lombok.Setter;
import org.apache.shardingsphere.spi.keygen.ShardingKeyGenerator;

import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.SynchronousQueue;

/**
 * Created by Jason on 2019/4/28.
 */
public class LeafSegmentKeyGenerator implements ShardingKeyGenerator {

    private LeafCuratorZookeeper leafCuratorZookeeper;

    @Getter
    @Setter
    private Properties properties = new Properties();

    private long id;

    private static final String TYPE = "LEAFSEGMENT";

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
        return null;
    }

    public synchronized Comparable<?> generateKey(final String tableName){
        if (isInitialized == Boolean.FALSE) {
            initLeafSegmentKeyGenerator(tableName);
            isInitialized = Boolean.TRUE;
            return id;
        }
        id = generateKeyWhenTableStoredInCenter(tableName);
        return id;
    }

    private void initLeafSegmentKeyGenerator(final String tableName){
        LeafConfiguration leafConfiguration = new LeafConfiguration(TYPE,getServerList());
        leafCuratorZookeeper = new LeafCuratorZookeeper();
        leafCuratorZookeeper.init(leafConfiguration);
        if(leafCuratorZookeeper.isExisted(tableName)){
            id = leafCuratorZookeeper.incrementCacheId(tableName,getStep());
        }else{
            id = getInitialValue();
            leafCuratorZookeeper.persist(tableName,String.valueOf(id));
        }
        incrementCacheIdExecutor = Executors.newSingleThreadExecutor();
        cacheIdQueue = new SynchronousQueue<Long>();
        step = getStep();
    }

    private long generateKeyWhenTableStoredInCenter(final String tableName){
        ++id;
        if(((id%step) >= (step*THRESHOLD-1)) && cacheIdQueue.isEmpty()){
            incrementCacheIdAsynchronous(tableName,step);
        }
        if((id%step) == (step-1)){
            id = tryTakeCacheId();
        }
        return id;
    }

    private void incrementCacheIdAsynchronous(final String tableName,final long step){
        incrementCacheIdExecutor.execute(new Runnable() {
            @Override
            public void run() {
                long id = leafCuratorZookeeper.incrementCacheId(tableName,step);
                tryPutCacheId(id);
            }
        });
    }

    private long tryTakeCacheId(){
        long id = Long.MIN_VALUE;
        try{
            id = cacheIdQueue.take();
        }catch (Exception e){
            LeafExceptionHandler.handleException(e);
        }
        return id;
    }

    private void tryPutCacheId(long id){
        try{
            cacheIdQueue.put(id);
        }catch (Exception e){
            LeafExceptionHandler.handleException(e);
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
}