/*
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

package com.dangdang.ddframe.rdb.transaction.soft.api.config;

import lombok.Getter;
import lombok.Setter;

/**
 * 内嵌的最大努力送达型异步作业配置对象.
 * 
 * @author zhangliang
 */
@Getter
@Setter
public final class NestedBestEffortsDeliveryJobConfiguration extends AbstractBestEffortsDeliveryJobConfiguration {
    
    /**
     * 内嵌的注册中心端口号.
     */
    private int zookeeperPort = 4181;
    
    /**
     * 内嵌的注册中心的数据存放目录.
     */
    private String zookeeperDataDir = String.format("target/test_zk_data/%s/", System.nanoTime());
    
    /**
     * 异步的事务送达的最大尝试次数.
     */
    private int asyncMaxDeliveryTryTimes = 3;
    
    /**
     * 执行异步送达事务的延迟毫秒数.
     *
     * <p>早于此间隔时间的入库事务才会被异步作业执行.</p>
     */
    private long asyncMaxDeliveryTryDelayMillis = 60  * 1000L;
    
}
