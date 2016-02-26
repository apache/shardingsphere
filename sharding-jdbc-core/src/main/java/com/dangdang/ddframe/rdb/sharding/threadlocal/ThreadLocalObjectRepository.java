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

package com.dangdang.ddframe.rdb.sharding.threadlocal;

import java.util.HashMap;
import java.util.Map;

import lombok.NoArgsConstructor;

/**
 * ThreadLocal对象仓库.
 * 多个ShardingDataSource使用static对象会造成数据污染,故使用该类来将这些对象绑定到ThreadLocal中.
 * 
 * @author gaohongtao
 */
@NoArgsConstructor
public final class ThreadLocalObjectRepository {
    
    private static final ThreadLocal<Map<String, Object>> THREAD_LOCAL_REPOSITORY = new ThreadLocal<>();
    
    private Map<String, Object> repository = new HashMap<>();
    
    /**
     * 向仓库内添加对象.
     * 
     * @param item 受仓库管理的对象
     */
    public void addItem(final Object item) {
        repository.put(item.getClass().getName(), item);
    }
    
    /**
     * 开始使用仓库.
     * 在本线程开始执行前要调用此方法设置线程对象状态.
     * 
     */
    public void open() {
        Map<String, Object> repositoryInThisThread = THREAD_LOCAL_REPOSITORY.get();
        if (null != repositoryInThisThread && repositoryInThisThread.equals(repository)) {
            return;
        }
        THREAD_LOCAL_REPOSITORY.remove();
        THREAD_LOCAL_REPOSITORY.set(repository);
    }
    
    /**
     * 获取线程中对象.
     * 
     * @param tClass 对象类型
     * @return 对象
     */
    public static <T> T getItem(final Class<T> tClass) {
        return (T) THREAD_LOCAL_REPOSITORY.get().get(tClass.getName());
    }
    
}
