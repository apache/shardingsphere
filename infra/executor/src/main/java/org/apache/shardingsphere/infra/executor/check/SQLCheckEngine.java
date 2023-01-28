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
import org.apache.shardingsphere.infra.executor.check.checker.SQLChecker;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.rule.ShardingSphereRuleMetaData;
import org.apache.shardingsphere.infra.metadata.user.Grantee;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.util.spi.type.ordered.OrderedSPILoader;

import java.util.Collection;
import java.util.List;
import java.util.Map.Entry;
import java.util.function.BiPredicate;

/**
 * SQL check engine.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SQLCheckEngine {
    
    /**
     * Check SQL with database rules.
     *
     * @param sqlStatementContext SQL statement context
     * @param params SQL parameters
     * @param globalRuleMetaData global rule meta data
     * @param database database
     * @param grantee grantee
     */
    public static void checkWithDatabaseRules(final SQLStatementContext<?> sqlStatementContext, final List<Object> params,
                                              final ShardingSphereRuleMetaData globalRuleMetaData, final ShardingSphereDatabase database, final Grantee grantee) {
        checkWithRules(sqlStatementContext, params, globalRuleMetaData, database.getRuleMetaData().getRules(), database, grantee);
    }
    
    /**
     * Check SQL with rules.
     *
     * @param sqlStatementContext SQL statement context
     * @param params SQL parameters
     * @param globalRuleMetaData global rule meta data
     * @param rules rules
     * @param database database
     * @param grantee grantee
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    public static void checkWithRules(final SQLStatementContext<?> sqlStatementContext, final List<Object> params, final ShardingSphereRuleMetaData globalRuleMetaData,
                                      final Collection<ShardingSphereRule> rules, final ShardingSphereDatabase database, final Grantee grantee) {
        for (Entry<ShardingSphereRule, SQLChecker> entry : OrderedSPILoader.getServices(SQLChecker.class, rules).entrySet()) {
            entry.getValue().check(sqlStatementContext, params, grantee, globalRuleMetaData, database, entry.getKey());
        }
    }
    
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
        for (Entry<ShardingSphereRule, SQLChecker> entry : OrderedSPILoader.getServices(SQLChecker.class, rules).entrySet()) {
            boolean checkResult = entry.getValue().check(databaseName, grantee, entry.getKey());
            if (!checkResult) {
                return false;
            }
        }
        return true;
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
        for (Entry<ShardingSphereRule, SQLChecker> entry : OrderedSPILoader.getServices(SQLChecker.class, rules).entrySet()) {
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
        for (Entry<ShardingSphereRule, SQLChecker> entry : OrderedSPILoader.getServices(SQLChecker.class, rules).entrySet()) {
            boolean checkResult = entry.getValue().check(user, validate, cipher, entry.getKey());
            if (!checkResult) {
                return false;
            }
        }
        return true;
    }
}
