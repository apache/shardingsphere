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

package org.apache.shardingsphere.orchestration.config.apollo;

import com.ctrip.framework.apollo.Config;
import com.ctrip.framework.apollo.ConfigChangeListener;
import com.ctrip.framework.apollo.ConfigService;
import com.ctrip.framework.apollo.core.ConfigConsts;
import com.ctrip.framework.apollo.enums.PropertyChangeType;
import com.ctrip.framework.apollo.model.ConfigChange;
import com.ctrip.framework.apollo.model.ConfigChangeEvent;
import com.google.common.base.Strings;
import com.google.common.collect.Sets;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.orchestration.config.api.ConfigCenter;
import org.apache.shardingsphere.orchestration.config.api.ConfigCenterConfiguration;
import org.apache.shardingsphere.orchestration.config.listener.DataChangedEvent;
import org.apache.shardingsphere.orchestration.config.listener.DataChangedEventListener;

import java.util.List;
import java.util.Properties;

/**
 * Registry center for Apollo.
 *
 * @author dongzonglei
 */
@Slf4j
public final class ApolloConfigCenter implements ConfigCenter {

    private Config apolloConfig;

    @Getter
    @Setter
    private Properties properties = new Properties();

    @Override
    public void init(final ConfigCenterConfiguration config) {
        System.getProperties().setProperty("app.id", properties.getProperty("appId", "APOLLO_SHARDING_SPHERE"));
        System.getProperties().setProperty("env", properties.getProperty("env", "DEV"));
        System.getProperties().setProperty(ConfigConsts.APOLLO_CLUSTER_KEY, properties.getProperty("clusterName", ConfigConsts.CLUSTER_NAME_DEFAULT));
        System.getProperties().setProperty(ConfigConsts.APOLLO_META_KEY, config.getServerLists());
        apolloConfig = ConfigService.getConfig(config.getNamespace());
    }

    @Override
    public String get(final String key) {
        return apolloConfig.getProperty(key.replace("/", "."), "");
    }

    @Override
    public String getDirectly(final String key) {
        return get(key);
    }

    @Override
    public boolean isExisted(final String key) {
        return !Strings.isNullOrEmpty(get(key));
    }

    @Override
    public List<String> getChildrenKeys(final String key) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void persist(final String key, final String value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void update(final String key, final String value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void persistEphemeral(final String key, final String value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void watch(final String key, final DataChangedEventListener dataChangedEventListener) {
        apolloConfig.addChangeListener(new ConfigChangeListener() {
            @Override
            public void onChange(final ConfigChangeEvent changeEvent) {
                for (String key : changeEvent.changedKeys()) {
                    ConfigChange change = changeEvent.getChange(key);
                    DataChangedEvent.ChangedType changedType = getChangedType(change.getChangeType());
                    if (DataChangedEvent.ChangedType.IGNORED != changedType) {
                        dataChangedEventListener.onChange(new DataChangedEvent(key, change.getNewValue(), changedType));
                    }
                }
            }
        }, Sets.newHashSet(key));
    }

    private DataChangedEvent.ChangedType getChangedType(final PropertyChangeType changeType) {
        switch (changeType) {
            case MODIFIED:
                return DataChangedEvent.ChangedType.UPDATED;
            case DELETED:
                return DataChangedEvent.ChangedType.DELETED;
            default:
                return DataChangedEvent.ChangedType.IGNORED;
        }
    }

    @Override
    public void close() {

    }

    @Override
    public String getType() {
        return "apollo";
    }
}
