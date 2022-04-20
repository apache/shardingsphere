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

package org.apache.shardingsphere.integration.scaling.test.mysql.container.storage;

import com.google.common.collect.Lists;
import org.apache.shardingsphere.infra.database.type.DatabaseTypeRegistry;
import org.apache.shardingsphere.integration.scaling.test.mysql.engine.base.DockerDatabaseContainer;
import org.apache.shardingsphere.test.integration.framework.container.atomic.storage.StorageContainer;

public class MySQLContainer extends DockerDatabaseContainer implements StorageContainer {
    
    public MySQLContainer(final String dockerImageName) {
        super(DatabaseTypeRegistry.getActualDatabaseType("MySQL"), dockerImageName);
    }
    
    @Override
    protected void configure() {
        super.configure();
        withExposedPorts(3306);
        setEnv(Lists.newArrayList("LANG=C.UTF-8", "MYSQL_ROOT_PASSWORD=123456", "MYSQL_ROOT_HOST=%", "MYSQL_DATABASE=test"));
        withCommand("--sql_mode=", "--default-authentication-plugin=mysql_native_password", "--character-set-server=utf8mb4", "--collation-server=utf8mb4_unicode_ci");
    }
    
    @Override
    protected int getPort() {
        return 3306;
    }
}
