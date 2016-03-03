/**
 * Copyright 1999-2015 dangdang.com.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package com.dangdang.ddframe.rdb.sharding.metrics;

import java.util.HashMap;
import java.util.Map;

/**
 * ThreadLocal对象容器.
 * 
 * <p>
 * 多个ShardingDataSource使用静态对象会造成数据污染, 故使用该类来将这些对象绑定到ThreadLocal中.
 * </p>
 * 
 * @author gaohongtao
 */
public final class ThreadLocalObjectContainer {
    
    private static final ThreadLocal<Map<String, Object>> THREAD_LOCAL_CONTAINER = new ThreadLocal<>();
    
    private Map<String, Object> data = new HashMap<>();
    
    /**
     * 向容器内添加初始对象.
     * 
     * @param item 受容器管理的对象
     */
    public void initItem(final Object item) {
        data.put(item.getClass().getName(), item);
    }
    
    /**
     * 开始使用容器.
     * 在本线程开始执行前要调用此方法设置线程对象状态.
     * 
     */
    public void build() {
        THREAD_LOCAL_CONTAINER.remove();
        THREAD_LOCAL_CONTAINER.set(data);
    }
    
    /**
     * 清理线程中的数据.
     * 
     */
    public static void clear() {
        THREAD_LOCAL_CONTAINER.remove();
    }
    
    /**
     * 获取线程中对象.
     * 
     * @param clazz 对象类型
     * @return 对象
     */
    public static <T> T getItem(final Class<T> clazz) {
        return (T) (null == THREAD_LOCAL_CONTAINER.get() ? null : THREAD_LOCAL_CONTAINER.get().get(clazz.getName()));
    }
}
