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

package org.apache.shardingsphere.shardingproxy.transport.common.codec;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.core.constant.DatabaseType;
import org.apache.shardingsphere.core.spi.NewInstanceServiceLoader;

/**
 * Database packet codec factory.
 *
 * @author zhangliang
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DatabasePacketCodecEngineFactory {
    
    static {
        NewInstanceServiceLoader.register(DatabasePacketCodecEngine.class);
    }
    
    /**
     * Create new instance of database packet codec engine instance.
     * 
     * @param databaseType database type
     * @return packet codec instance
     */
    public static DatabasePacketCodecEngine newInstance(final DatabaseType databaseType) {
        for (DatabasePacketCodecEngine each : NewInstanceServiceLoader.newServiceInstances(DatabasePacketCodecEngine.class)) {
            if (DatabaseType.valueFrom(each.getDatabaseType()) == databaseType) {
                return each;
            }
        }
        throw new UnsupportedOperationException(String.format("Cannot support database type '%s'", databaseType));
    }
}
