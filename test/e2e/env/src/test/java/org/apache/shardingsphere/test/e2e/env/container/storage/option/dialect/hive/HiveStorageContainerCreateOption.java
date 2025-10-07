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

package org.apache.shardingsphere.test.e2e.env.container.storage.option.dialect.hive;

import org.apache.shardingsphere.test.e2e.env.container.storage.option.StorageContainerCreateOption;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Storage container craete option for Hive.
 */
public final class HiveStorageContainerCreateOption implements StorageContainerCreateOption {
    
    @Override
    public int getPort() {
        return 10000;
    }
    
    @Override
    public String getDefaultImageName() {
        return "apache/hive:4.0.1";
    }
    
    @Override
    public String getCommand() {
        return "bash -c 'start-hive.sh && tail -f /dev/null'";
    }
    
    @Override
    public Map<String, String> getEnvironments() {
        Map<String, String> result = new HashMap<>(3, 1F);
        result.put("SERVICE_NAME", "hiveserver2");
        result.put("SERVICE_OPTS", "-Dhive.support.concurrency=true -Dhive.exec.dynamic.partition.mode=nonstrict -Dhive.txn.manager=org.apache.hadoop.hive.ql.lockmgr.DbTxnManager");
        result.put("LANG", "C.UTF-8");
        return result;
    }
    
    @Override
    public Collection<String> getMountedConfigurationResources() {
        return Collections.singleton("/opt/hive/conf/hive-site.xml");
    }
    
    @Override
    public Collection<String> getAdditionalEnvMountedSQLResources(final int majorVersion) {
        return Collections.emptyList();
    }
    
    @Override
    public List<Integer> getSupportedMajorVersions() {
        return Collections.emptyList();
    }
    
    @Override
    public boolean withPrivilegedMode() {
        return false;
    }
    
    @Override
    public Optional<String> getDefaultDatabaseName(final int majorVersion) {
        return Optional.of("default");
    }
    
    @Override
    public long getStartupTimeoutSeconds() {
        return 180L;
    }
    
    @Override
    public boolean isSupportDockerEntrypoint() {
        return false;
    }
    
    @Override
    public Optional<String> getDefaultUserWhenUnsupportedDockerEntrypoint() {
        return Optional.of("");
    }
    
    @Override
    public Optional<String> getDefaultPasswordWhenUnsupportedDockerEntrypoint() {
        return Optional.of("");
    }
}
