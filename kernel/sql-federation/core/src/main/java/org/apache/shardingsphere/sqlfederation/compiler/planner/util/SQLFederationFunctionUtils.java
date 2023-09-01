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

package org.apache.shardingsphere.sqlfederation.compiler.planner.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.calcite.schema.SchemaPlus;
import org.apache.calcite.schema.impl.ScalarFunctionImpl;
import org.apache.shardingsphere.infra.autogen.version.ShardingSphereVersion;

/**
 * SQL federation function utility class.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SQLFederationFunctionUtils {
    
    private static final int DEFAULT_PASSWORD_DEADLINE = 90;
    
    private static final int DEFAULT_PASSWORD_NOTIFY_TIME = 7;
    
    /**
     * Registry user defined function.
     * 
     * @param schemaName schema name
     * @param schemaPlus schema plus
     */
    public static void registryUserDefinedFunction(final String schemaName, final SchemaPlus schemaPlus) {
        schemaPlus.add("version", ScalarFunctionImpl.create(SQLFederationFunctionUtils.class, "version"));
        schemaPlus.add("opengauss_version", ScalarFunctionImpl.create(SQLFederationFunctionUtils.class, "openGaussVersion"));
        schemaPlus.add("gs_password_deadline", ScalarFunctionImpl.create(SQLFederationFunctionUtils.class, "gsPasswordDeadline"));
        schemaPlus.add("intervaltonum", ScalarFunctionImpl.create(SQLFederationFunctionUtils.class, "intervalToNum"));
        schemaPlus.add("gs_password_notifyTime", ScalarFunctionImpl.create(SQLFederationFunctionUtils.class, "gsPasswordNotifyTime"));
        if ("pg_catalog".equalsIgnoreCase(schemaName)) {
            schemaPlus.add("pg_catalog.pg_table_is_visible", ScalarFunctionImpl.create(SQLFederationFunctionUtils.class, "pgTableIsVisible"));
            schemaPlus.add("pg_catalog.pg_get_userbyid", ScalarFunctionImpl.create(SQLFederationFunctionUtils.class, "pgGetUserById"));
        }
    }
    
    /**
     * Mock pg_table_is_visible function.
     *
     * @param oid oid
     * @return true
     */
    @SuppressWarnings("unused")
    public static boolean pgTableIsVisible(final Long oid) {
        return true;
    }
    
    /**
     * Mock pg_get_userbyid function.
     * 
     * @param oid oid
     * @return user name
     */
    @SuppressWarnings("unused")
    public static String pgGetUserById(final Long oid) {
        return "mock user";
    }
    
    /**
     * Get version of ShardingSphere-Proxy.
     *
     * @return version message
     */
    public static String version() {
        return "ShardingSphere-Proxy " + ShardingSphereVersion.VERSION + ("-" + ShardingSphereVersion.BUILD_GIT_COMMIT_ID_ABBREV) + (ShardingSphereVersion.BUILD_GIT_DIRTY ? "-dirty" : "");
    }
    
    /**
     * Get version of ShardingSphere-Proxy for openGauss.
     *
     * @return version message
     */
    @SuppressWarnings("unused")
    public static String openGaussVersion() {
        return ShardingSphereVersion.VERSION;
    }
    
    /**
     * The type interval is not supported in standard JDBC.
     * Indicates the number of remaining days before the password of the current user expires.
     *
     * @return 90 days
     */
    @SuppressWarnings("unused")
    public static int gsPasswordDeadline() {
        return DEFAULT_PASSWORD_DEADLINE;
    }
    
    /**
     * The type interval is not supported in standard JDBC.
     * Convert interval to num.
     *
     * @param result result
     * @return result
     */
    @SuppressWarnings("unused")
    public static int intervalToNum(final int result) {
        return result;
    }
    
    /**
     * Specifies the number of days prior to password expiration that a user will receive a reminder.
     *
     * @return 7 days
     */
    @SuppressWarnings("unused")
    public static int gsPasswordNotifyTime() {
        return DEFAULT_PASSWORD_NOTIFY_TIME;
    }
}
