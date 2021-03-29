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

package org.apache.shardingsphere.infra.check;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.auth.Authentication;
import org.apache.shardingsphere.infra.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.infra.spi.ordered.OrderedSPIRegistry;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;

import java.util.Collection;
import java.util.List;

/**
 * SQL check engine.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SQLCheckEngine {
    
    private static final Collection<SQLChecker> CHECKERS = OrderedSPIRegistry.getRegisteredServices(SQLChecker.class);
    
    static {
        ShardingSphereServiceLoader.register(SQLChecker.class);
    }
    
    /**
     * Check SQL.
     * 
     * @param sqlStatement SQL statement
     * @param parameters SQL parameters
     * @param metaData meta data
     * @param auth auth
     */
    public static void check(final SQLStatement sqlStatement, final List<Object> parameters, final ShardingSphereMetaData metaData, final Authentication auth) {
        for (SQLChecker each : CHECKERS) {
            SQLCheckResult checkResult = each.check(sqlStatement, parameters, metaData, auth);
            if (!checkResult.isPassed()) {
                throw new SQLCheckException(each.getSQLCheckType(), checkResult.getErrorMessage());
            }
        }
    }
}
