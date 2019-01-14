/*
 * Copyright 2016-2018 shardingsphere.io.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingsphere.shardingui.security;

import com.google.common.base.Strings;
import com.google.gson.Gson;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.codec.binary.Base64;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * User authentication service.
 *
 * @author chenqingyang
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
    
    /**
     * Check user.
     *
     * @param userAccount user account
     * @return check success or failure
     */
    public boolean checkUser(final UserAccount userAccount) {
        if (userAccount == null || Strings.isNullOrEmpty(userAccount.getUsername()) || Strings.isNullOrEmpty(userAccount.getPassword())) {
            return false;
        }
        if (!username.equals(userAccount.getUsername()) || !password.equals(userAccount.getPassword())) {
            return false;
        }
        return true;
    }
    
    /**
     * Get user authentication token.
     *
     * @return authentication token
     */
    public String getToken() {
        return new Base64().encodeToString(new Gson().toJson(this).getBytes());
    }
}
