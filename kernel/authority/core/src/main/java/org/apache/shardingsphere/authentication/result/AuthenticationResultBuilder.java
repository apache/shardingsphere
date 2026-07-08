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

package org.apache.shardingsphere.authentication.result;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Collections;
import java.util.Map;

/**
 * Authentication result builder.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class AuthenticationResultBuilder {
    
    /**
     * Create finished authentication result.
     *
     * @param username username
     * @param hostname hostname
     * @param database database
     * @param attributes attributes
     * @return finished authentication result
     */
    public static AuthenticationResult finished(final String username, final String hostname, final String database, final Map<String, String> attributes) {
        return new AuthenticationResult(username, hostname, database, true, attributes);
    }
    
    /**
     * Create continued authentication result.
     *
     * @return continued authentication result
     */
    public static AuthenticationResult continued() {
        return new AuthenticationResult(null, null, null, false, Collections.emptyMap());
    }
    
    /**
     * Create continued authentication result.
     *
     * @param username username
     * @param hostname hostname
     * @param database database
     * @param attributes attributes
     * @return continued authentication result
     */
    public static AuthenticationResult continued(final String username, final String hostname, final String database, final Map<String, String> attributes) {
        return new AuthenticationResult(username, hostname, database, false, attributes);
    }
}
