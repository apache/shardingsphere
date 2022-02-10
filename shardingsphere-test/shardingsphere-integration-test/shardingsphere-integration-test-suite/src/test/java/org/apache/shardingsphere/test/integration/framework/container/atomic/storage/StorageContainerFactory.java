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

package org.apache.shardingsphere.test.integration.framework.container.atomic.storage;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.test.integration.framework.container.atomic.storage.impl.H2Container;
import org.apache.shardingsphere.test.integration.framework.container.atomic.storage.impl.MySQLContainer;
import org.apache.shardingsphere.test.integration.framework.container.atomic.storage.impl.PostgreSQLContainer;
import org.apache.shardingsphere.test.integration.framework.param.model.ParameterizedArray;

/**
 * Storage container factory.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class StorageContainerFactory {
    
    /**
     * Create new instance of storage container.
     * 
     * @param parameterizedArray parameterized array
     * @return new instance of storage container
     */
    public static StorageContainer newInstance(final ParameterizedArray parameterizedArray) {
        switch (parameterizedArray.getDatabaseType().getName()) {
            case "MySQL":
                return new MySQLContainer(parameterizedArray);
            case "PostgreSQL" :
                return new PostgreSQLContainer(parameterizedArray);
            case "H2":
                return new H2Container(parameterizedArray);
            default:
                throw new RuntimeException(String.format("Database [%s] is unknown.", parameterizedArray.getDatabaseType()));
        }
    }
}
