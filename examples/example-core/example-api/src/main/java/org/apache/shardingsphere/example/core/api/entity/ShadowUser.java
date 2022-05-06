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

package org.apache.shardingsphere.example.core.api.entity;

import java.io.Serializable;

public class ShadowUser implements Serializable {
    
    private static final long serialVersionUID = -6711618386636677067L;
    
    private int userId;
    
    private int userType;
    
    private String username;
    
    private String usernamePlain;
    
    private String pwd;
    
    private String assistedQueryPwd;
    
    public int getUserId() {
        return userId;
    }
    
    public void setUserId(int userId) {
        this.userId = userId;
    }
    
    public int getUserType() {
        return userType;
    }
    
    public void setUserType(int userType) {
        this.userType = userType;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getUsernamePlain() {
        return usernamePlain;
    }
    
    public void setUsernamePlain(String usernamePlain) {
        this.usernamePlain = usernamePlain;
    }
    
    public String getPwd() {
        return pwd;
    }
    
    public void setPwd(String pwd) {
        this.pwd = pwd;
    }
    
    public String getAssistedQueryPwd() {
        return assistedQueryPwd;
    }
    
    public void setAssistedQueryPwd(String assistedQueryPwd) {
        this.assistedQueryPwd = assistedQueryPwd;
    }
    
    @Override
    public String toString() {
        return String.format("user_id: %d, user_type: %d, username: %s, username_plain: %s, pwd: %s, assisted_query_pwd: %s", userId, userType, username, usernamePlain, pwd,
                assistedQueryPwd);
    }
}
