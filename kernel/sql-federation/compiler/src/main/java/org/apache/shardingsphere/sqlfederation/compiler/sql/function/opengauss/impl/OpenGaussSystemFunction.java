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

package org.apache.shardingsphere.sqlfederation.compiler.sql.function.opengauss.impl;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.version.ShardingSphereVersion;

/**
 * PostgreSQL system function.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class OpenGaussSystemFunction {
    
    private static final int DEFAULT_PASSWORD_DEADLINE = 90;
    
    private static final int DEFAULT_PASSWORD_NOTIFY_TIME = 7;
    
    /**
     * Get version of ShardingSphere-Proxy.
     *
     * @return version message
     */
    @SuppressWarnings("unused")
    public static String version() {
        return "ShardingSphere-Proxy " + ShardingSphereVersion.VERSION + ("-" + ShardingSphereVersion.BUILD_COMMIT_ID_ABBREV) + (ShardingSphereVersion.BUILD_DIRTY ? "-dirty" : "");
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
