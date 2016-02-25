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

package com.dangdang.ddframe.rdb.transaction.soft.bed;

import com.dangdang.ddframe.rdb.transaction.soft.api.config.AbstractBestEffortsDeliveryJobConfiguration;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 最大努力送达型异步作业配置对象.
 * 
 * @author zhangliang
 */
@Getter
@RequiredArgsConstructor
public final class BestEffortsDeliveryJobConfiguration extends AbstractBestEffortsDeliveryJobConfiguration {
    
    /**
     * 注册中心的连接地址.
     */
    private final String zookeeperConnectionString;
}
