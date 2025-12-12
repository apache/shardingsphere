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

package org.apache.shardingsphere.test.e2e.sql.framework.metadata;

import org.apache.shardingsphere.database.connector.core.spi.DatabaseTypedSPI;
import org.apache.shardingsphere.infra.spi.annotation.SingletonSPI;

import java.util.Optional;

/**
 * Dialect query behavior provider for E2E assertions.
 */
@SingletonSPI
public interface DialectQueryBehaviorProvider extends DatabaseTypedSPI {
    
    /**
     * Get fallback ORDER BY clause when primary key is unknown.
     *
     * <p>Return value should not contain the leading "ORDER BY" keyword.
     * For example: "1 ASC" or "some_column ASC". Empty means no fallback.</p>
     *
     * @return optional fallback ORDER BY clause (without leading ORDER BY)
     */
    default Optional<String> getFallbackOrderByWhenNoPrimaryKey() {
        return Optional.empty();
    }
}
