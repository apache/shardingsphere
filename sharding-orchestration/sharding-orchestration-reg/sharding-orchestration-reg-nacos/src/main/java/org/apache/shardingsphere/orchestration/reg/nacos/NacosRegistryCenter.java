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

package org.apache.shardingsphere.orchestration.reg.nacos;

import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.config.listener.Listener;
import com.alibaba.nacos.api.exception.NacosException;
import com.google.common.base.Strings;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.orchestration.reg.api.RegistryCenter;
import org.apache.shardingsphere.orchestration.reg.api.RegistryCenterConfiguration;
import org.apache.shardingsphere.orchestration.reg.listener.DataChangedEvent;
import org.apache.shardingsphere.orchestration.reg.listener.DataChangedEventListener;

import java.util.List;
import java.util.Properties;
import java.util.concurrent.Executor;

@Slf4j
public class NacosRegistryCenter implements RegistryCenter {
    private ConfigService configService;

    @Getter
    @Setter
    private Properties properties = new Properties();

    @Override
    public void init(RegistryCenterConfiguration config) {
        try {
            configService = NacosFactory.createConfigService(config.getServerLists());
        } catch (NacosException e) {
            log.debug("exception for: {}", e.toString());
        }
    }

    @Override
    public String get(String key) {
        return getDirectly(key);
    }

    @Override
    public String getDirectly(String key) {
        /**
         * getConfig(String dataId, String group, long timeoutMs)
         * dataId:
         * 配置 ID，采用类似 package.class（如org.apache.shardingsphere）的命名规则保证全局唯一性，
         * class 部分建议是配置的业务含义。 全部字符小写。只允许英文字符和 4 种特殊字符（"."、":"、"-"、"_"）。不超过 256 字节
         * group:
         * 配置分组，建议填写产品名：模块名（如 Nacos:Test）保证唯一性。 只允许英文字符和4种特殊字符（"."、":"、"-"、"_"），不超过128字节
         * timeoutMs:
         * 读取配置超时时间，单位 ms，推荐值 3000
         */
        try {
            String dataId = key.replace("/",".");
            String group = properties.getProperty("group","SHARDING_SPHERE_DEFAULT_GROUP");
            long timeoutMs = Long.parseLong(properties.getProperty("timeout","3000"));
            return configService.getConfig(dataId,group,timeoutMs);
        } catch (NacosException e) {
            log.debug("exception for: {}", e.toString());
            return null;
        }
    }

    @Override
    public boolean isExisted(String key) {
        return !Strings.isNullOrEmpty(getDirectly(key));
    }

    @Override
    public List<String> getChildrenKeys(String key) {
        return null;
    }

    @Override
    public void persist(String key, String value) {
        update(key,value);
    }

    @Override
    public void update(String key, String value) {
        /**
         * publishConfig(String dataId, String group, String content)
         * dataId:
         * 配置 ID，采用类似 package.class（如org.apache.shardingsphere）的命名规则保证全局唯一性，
         * class 部分建议是配置的业务含义。 全部字符小写。只允许英文字符和 4 种特殊字符（"."、":"、"-"、"_"）。不超过 256 字节
         * group:
         * 配置分组，建议填写产品名：模块名（如 Nacos:Test）保证唯一性。 只允许英文字符和4种特殊字符（"."、":"、"-"、"_"），不超过128字节
         * content:
         * 配置内容，不超过 100K 字节
         */
        try {
            String dataId = key.replace("/",".");
            String group = properties.getProperty("group","SHARDING_SPHERE_DEFAULT_GROUP");
            configService.publishConfig(dataId,group,value);
        } catch (NacosException e) {
            log.debug("exception for: {}", e.toString());
        }
    }

    @Override
    public void persistEphemeral(String key, String value) {

    }

    @Override
    public void watch(final String key, final DataChangedEventListener dataChangedEventListener) {
        /**
         * addListener(String dataId, String group, Listener listener)
         * dataId:
         * 配置 ID，采用类似 package.class（如org.apache.shardingsphere）的命名规则保证全局唯一性，
         * class 部分建议是配置的业务含义。 全部字符小写。只允许英文字符和 4 种特殊字符（"."、":"、"-"、"_"）。不超过 256 字节
         * group:
         * 配置分组，建议填写产品名：模块名（如 Nacos:Test）保证唯一性。 只允许英文字符和4种特殊字符（"."、":"、"-"、"_"），不超过128字节
         */
        try {
            String dataId = key.replace("/",".");
            String group = properties.getProperty("group","SHARDING_SPHERE_DEFAULT_GROUP");
            configService.addListener(dataId,group,new Listener(){

                @Override
                public Executor getExecutor() {
                    return null;
                }

                @Override
                public void receiveConfigInfo(String configInfo) {
                    /**
                     *Nacos只有UPDATED状态
                     */
                    dataChangedEventListener.onChange(new DataChangedEvent(key,configInfo, DataChangedEvent.ChangedType.UPDATED));
                }
            });
        } catch (NacosException e) {
            log.debug("exception for: {}", e.toString());
        }
    }

    @Override
    public void close() {
    }

    @Override
    public void initLock(String key) {

    }

    @Override
    public boolean tryLock() {
        return false;
    }

    @Override
    public void tryRelease() {

    }

    @Override
    public String getType() {
        return "nacos";
    }
}
