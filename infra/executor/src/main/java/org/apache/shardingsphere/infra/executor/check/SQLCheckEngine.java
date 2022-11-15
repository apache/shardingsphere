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

package org.apache.shardingsphere.infra.executor.check;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.check.SQLCheckException;
import org.apache.shardingsphere.infra.check.SQLCheckResult;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.user.Grantee;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.BiPredicate;

/**
 * SQL check engine.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SQLCheckEngine {
    
    /**
     * Check database.
     *
     * @param databaseName database name
     * @param rules rules
     * @param grantee grantee
     * @return check result
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    public static boolean check(final String databaseName, final Collection<ShardingSphereRule> rules, final Grantee grantee) {
        for (Entry<ShardingSphereRule, SQLChecker> entry : SQLCheckerFactory.getInstance(rules).entrySet()) {
            boolean checkResult = entry.getValue().check(databaseName, grantee, entry.getKey());
            if (!checkResult) {
                return false;
            }
        }
        return true;
    }
    
    /**
     * Check SQL.
     *
     * @param sqlStatementContext SQL statement context
     * @param params SQL parameters
     * @param rules rules
     * @param currentDatabase current database
     * @param databases databases
     * @param grantee grantee
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    public static void check(final SQLStatementContext<?> sqlStatementContext, final List<Object> params, final Collection<ShardingSphereRule> rules,
                             final String currentDatabase, final Map<String, ShardingSphereDatabase> databases, final Grantee grantee) {
        for (Entry<ShardingSphereRule, SQLChecker> entry : SQLCheckerFactory.getInstance(rules).entrySet()) {
            SQLCheckResult checkResult = entry.getValue().check(sqlStatementContext, params, grantee, currentDatabase, databases, entry.getKey());
            if (!checkResult.isPassed()) {
                throw new SQLCheckException(checkResult.getErrorMessage());
            }
        }
    }
    
    /**
     * Check user exists.
     * 
     * @param user user
     * @param rules rules
     * @return check result
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    public static boolean check(final Grantee user, final Collection<ShardingSphereRule> rules) {
        if (rules.isEmpty()) {
            return false;
        }
        for (Entry<ShardingSphereRule, SQLChecker> entry : SQLCheckerFactory.getInstance(rules).entrySet()) {
            boolean checkResult = entry.getValue().check(user, entry.getKey());
            if (!checkResult) {
                return false;
            }
        }
        return true;
    }
    
    /**
     * Check authentication.
     * @param user user
     * @param validate validate
     * @param cipher cipher
     * @param rules rules
     * @return check result
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    public static boolean check(final Grantee user, final BiPredicate<Object, Object> validate, final Object cipher, final Collection<ShardingSphereRule> rules) {
        if (rules.isEmpty()) {
            return false;
        }
        for (Entry<ShardingSphereRule, SQLChecker> entry : SQLCheckerFactory.getInstance(rules).entrySet()) {
            boolean checkResult = entry.getValue().check(user, validate, cipher, entry.getKey());
            if (!checkResult) {
                return false;
            }
        }
        return true;
    }
}
