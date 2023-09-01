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

package org.apache.shardingsphere.infra.database.core.metadata.database.system;

import org.apache.shardingsphere.infra.database.core.spi.DatabaseTypedSPI;
import org.apache.shardingsphere.infra.spi.annotation.SingletonSPI;

import java.util.Collection;
import java.util.Map;

/**
 * Dialect system database.
 */
@SingletonSPI
public interface DialectSystemDatabase extends DatabaseTypedSPI {
    
    /**
     * Get system database schema map.
     *
     * @return system database schema map
     */
    Map<String, Collection<String>> getSystemDatabaseSchemaMap();
    
    /**
     * Get system schemas.
     *
     * @return system schemas
     */
    Collection<String> getSystemSchemas();
}
