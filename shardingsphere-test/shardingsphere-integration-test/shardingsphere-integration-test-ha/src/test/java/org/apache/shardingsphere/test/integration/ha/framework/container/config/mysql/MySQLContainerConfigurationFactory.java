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

package org.apache.shardingsphere.test.integration.ha.framework.container.config.mysql;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.test.integration.env.container.atomic.constants.StorageContainerConstants;
import org.apache.shardingsphere.test.integration.env.container.atomic.storage.config.StorageContainerConfiguration;
import org.apache.shardingsphere.test.integration.env.container.atomic.util.MySQLContainerUtil;
import org.apache.shardingsphere.test.integration.ha.util.HAContainerUtil;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * MySQL container configuration factory.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class MySQLContainerConfigurationFactory {
    
    /**
     * Create new instance of MySQL container configuration.
     * 
     * @param scenario scenario
     * @param databaseType database type
     * @return created instance
     */
    public static List<StorageContainerConfiguration> newInstance(final String scenario, final DatabaseType databaseType) {
        Integer containerQuantity = HAContainerUtil.loadContainerRawNamesAndQuantity(scenario).get(databaseType.getType().toLowerCase());
        if (containerQuantity == null) {
            return getDefaultConfiguration(databaseType);
        }
        if (1 == containerQuantity) {
            return Collections.singletonList(new StorageContainerConfiguration(getCommand(), getContainerEnvironments(), getMountedResources(scenario, databaseType, 0)));
        }
        List<StorageContainerConfiguration> result = new LinkedList<>();
        for (int i = 1; i <= containerQuantity; i++) {
            result.add(new StorageContainerConfiguration(getCommand(), getContainerEnvironments(), getMountedResources(scenario, databaseType, i)));
        }
        return result;
    }
    
    private static String getCommand() {
        return "--server-id=" + MySQLContainerUtil.generateServerId();
    }
    
    private static Map<String, String> getContainerEnvironments() {
        Map<String, String> result = new HashMap<>(2, 1);
        result.put("LANG", "C.UTF-8");
        result.put("MYSQL_RANDOM_ROOT_PASSWORD", "yes");
        return result;
    }
    
    private static Map<String, String> getMountedResources(final String scenario, final DatabaseType databaseType, final int order) {
        return 0 == order ? Collections.singletonMap(String.format("/env/scenario/%s/my.cnf", scenario), StorageContainerConstants.MYSQL_CONF_IN_CONTAINER)
                : Collections.singletonMap(String.format("/env/scenario/%s/%s/my.cnf", scenario, databaseType.getType().toLowerCase() + "_" + order),
                        StorageContainerConstants.MYSQL_CONF_IN_CONTAINER);
    }
    
    private static List<StorageContainerConfiguration> getDefaultConfiguration(final DatabaseType databaseType) {
        return Collections.singletonList(new StorageContainerConfiguration(getCommand(), getContainerEnvironments(),
                Collections.singletonMap(String.format("/env/%s/my.cnf", databaseType.getType().toLowerCase()), StorageContainerConstants.MYSQL_CONF_IN_CONTAINER)));
    }
}
