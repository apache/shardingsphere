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

package org.apache.shardingsphere.sharding.route.time.spi;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.sharding.route.time.exception.NoDatabaseSQLEntrySupportException;
import org.apache.shardingsphere.infra.spi.ShardingSphereServiceLoader;

import java.util.Collection;

/**
 * SPI for DatabaseSQLEntry.
 */
@RequiredArgsConstructor
public final class SPIDataBaseSQLEntry implements DatabaseSQLEntry {
    
    static {
        ShardingSphereServiceLoader.register(DatabaseSQLEntry.class);
    }
    
    private final Collection<DatabaseSQLEntry> sqlEntries = ShardingSphereServiceLoader.newServiceInstances(DatabaseSQLEntry.class);
    
    private final String driverClassName;
    
    @Override
    public String getSQL() {
        for (DatabaseSQLEntry each : sqlEntries) {
            if (each.isSupport(driverClassName)) {
                return each.getSQL();
            }
        }
        throw new NoDatabaseSQLEntrySupportException();
    }
    
    @Override
    public boolean isSupport(final String driverClassName) {
        return true;
    }
}
