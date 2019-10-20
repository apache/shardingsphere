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

package org.apache.shardingsphere.shardingjdbc.jdbc.core.context;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;

/**
 * Sharding runtime context wrapper holder.
 *
 * @author chenchuangliu
 */
public final class RuntimeContextHolder {

    private static final RuntimeContextHolder INSTANCE = new RuntimeContextHolder();

    private final Collection<RuntimeContext> runtimeContext = new LinkedList<>();

    private RuntimeContextHolder() {

    }

    /**
     * Get RuntimeContextHolder instance.
     *
     * @return RuntimeContextHolder
     */
    public static RuntimeContextHolder getInstance() {
        return INSTANCE;
    }

    /**
     * Save {@link RuntimeContext}.
     *
     * @param context {@link RuntimeContext}
     */
    public void addRuntimeContext(final RuntimeContext context) {
        synchronized (runtimeContext) {
            runtimeContext.add(context);
        }
    }

    /**
     * get all {@link ShardingRuntimeContext} wrapper.
     *
     * @return collection {@link RuntimeContext}
     */
    public Collection<ShardingRuntimeContext> getShardingRuntimeContexts() {
        return getRuntimeContext(ShardingRuntimeContext.class);
    }

    /**
     * get all {@link MasterSlaveRuntimeContext} wrapper.
     *
     * @return collection {@link RuntimeContext}
     */
    public Collection<MasterSlaveRuntimeContext> getMasterSlaveContexts() {
        return getRuntimeContext(MasterSlaveRuntimeContext.class);
    }

    /**
     * get all {@link EncryptRuntimeContext} wrapper.
     *
     * @return collection {@link RuntimeContext}
     */
    public Collection<EncryptRuntimeContext> getEncryptRuntimeContexts() {
        return getRuntimeContext(EncryptRuntimeContext.class);
    }

    @SuppressWarnings("unchecked")
    private <T extends RuntimeContext> Collection<T> getRuntimeContext(final Class<T> clazz) {
        Collection<T> result = new LinkedList<>();
        for (RuntimeContext context : runtimeContext) {
            if (clazz.isInstance(context)) {
                result.add((T) context);
            }
        }
        return result;
    }

    /**
     * Remove RuntimeContext.
     *
     * @param context {@link RuntimeContext}
     * @param <T> T extends RuntimeContext
     * @return removed value
     */
    public <T extends RuntimeContext> RuntimeContext removeRuntimeContext(final T context) {
        Iterator<RuntimeContext> iterator = runtimeContext.iterator();
        synchronized (runtimeContext) {
            while (iterator.hasNext()) {
                RuntimeContext oldContext = iterator.next();
                if (oldContext == context) {
                    iterator.remove();
                    return oldContext;
                }
            }
        }
        return null;
    }
}

