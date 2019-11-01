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

package org.apache.shardingsphere.orchestration.config.nacos;

import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.config.listener.Listener;
import com.alibaba.nacos.api.exception.NacosException;
import com.google.common.base.Strings;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.orchestration.config.api.ConfigCenter;
import org.apache.shardingsphere.orchestration.config.api.ConfigCenterConfiguration;
import org.apache.shardingsphere.orchestration.config.listener.DataChangedEvent;
import org.apache.shardingsphere.orchestration.config.listener.DataChangedEventListener;

import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.Executor;

@Slf4j
public final class NacosConfigCenter implements ConfigCenter {

    private ConfigService configService;

    @Getter
    @Setter
    private Properties properties = new Properties();

    @Override
    public void init(final ConfigCenterConfiguration config) {
        try {
            Properties properties = new Properties();
            properties.put("serverAddr", config.getServerLists());
            properties.put("namespace", null == config.getNamespace() ? "" : config.getNamespace());
            configService = NacosFactory.createConfigService(properties);
        } catch (final NacosException ex) {
            log.debug("exception for: {}", ex.toString());
        }
    }

    @Override
    public String get(final String key) {
        return getDirectly(key);
    }

    @Override
    public String getDirectly(final String key) {
        try {
            String dataId = key.replace("/", ".");
            String group = properties.getProperty("group", "SHARDING_SPHERE_DEFAULT_GROUP");
            long timeoutMs = Long.parseLong(properties.getProperty("timeout", "3000"));
            return configService.getConfig(dataId, group, timeoutMs);
        } catch (final NacosException ex) {
            log.debug("exception for: {}", ex.toString());
            return null;
        }
    }

    @Override
    public boolean isExisted(final String key) {
        return !Strings.isNullOrEmpty(getDirectly(key));
    }

    @Override
    public List<String> getChildrenKeys(final String key) {
        return Collections.singletonList(getDirectly(key));
    }

    @Override
    public void persist(final String key, final String value) {
        update(key, value);
    }

    @Override
    public void update(final String key, final String value) {
        try {
            String dataId = key.replace("/", ".");
            String group = properties.getProperty("group", "SHARDING_SPHERE_DEFAULT_GROUP");
            configService.publishConfig(dataId, group, value);
        } catch (final NacosException ex) {
            log.debug("exception for: {}", ex.toString());
        }
    }

    @Override
    public void persistEphemeral(final String key, final String value) {
        update(key, value);
    }

    @Override
    public void watch(final String key, final DataChangedEventListener dataChangedEventListener) {
        try {
            String dataId = key.replace("/", ".");
            String group = properties.getProperty("group", "SHARDING_SPHERE_DEFAULT_GROUP");
            configService.addListener(dataId, group, new Listener() {

                @Override
                public Executor getExecutor() {
                    return null;
                }

                @Override
                public void receiveConfigInfo(final String configInfo) {
                    dataChangedEventListener.onChange(new DataChangedEvent(key, configInfo, DataChangedEvent.ChangedType.UPDATED));
                }
            });
        } catch (final NacosException ex) {
            log.debug("exception for: {}", ex.toString());
        }
    }

    @Override
    public void close() {

    }

    @Override
    public String getType() {
        return "nacos";
    }
}
