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

package org.apache.shardingsphere.ui.security;

import com.google.common.base.Strings;
import com.google.gson.Gson;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.codec.binary.Base64;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * User authentication service.
 */
@Component
@ConfigurationProperties(prefix = "user.admin")
public final class UserAuthenticationService {
    
    @Getter
    @Setter
    private String username;
    
    @Getter
    @Setter
    private String password;
    
    private final Base64 base64 = new Base64();
    
    private Gson gson = new Gson();
    
    /**
     * Check user.
     *
     * @param userAccount user account
     * @return check success or failure
     */
    public boolean checkUser(final UserAccount userAccount) {
        if (null == userAccount || Strings.isNullOrEmpty(userAccount.getUsername()) || Strings.isNullOrEmpty(userAccount.getPassword())) {
            return false;
        }
        return username.equals(userAccount.getUsername()) && password.equals(userAccount.getPassword());
    }
    
    /**
     * Get user authentication token.
     *
     * @return authentication token
     */
    public String getToken() {
        return base64.encodeToString(gson.toJson(this).getBytes());
    }
}
