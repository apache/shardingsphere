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
    
    private static final long serialVersionUID = 263434701950670170L;
    
    private int userId;
    
    private String userName;
    
    private String userNamePlain;
    
    private String pwd;
    
    private String assistedQueryPwd;
    
    private boolean shadow;
    
    public int getUserId() {
        return userId;
    }
    
    public void setUserId(final int userId) {
        this.userId = userId;
    }
    
    public String getUserName() {
        return userName;
    }
    
    public void setUserName(final String userName) {
        this.userName = userName;
    }
    
    public String getUserNamePlain() {
        return userNamePlain;
    }
    
    public void setUserNamePlain(final String userNamePlain) {
        this.userNamePlain = userNamePlain;
    }
    
    public String getPwd() {
        return pwd;
    }
    
    public void setPwd(final String pwd) {
        this.pwd = pwd;
    }
    
    public String getAssistedQueryPwd() {
        return assistedQueryPwd;
    }
    
    public void setAssistedQueryPwd(final String assistedQueryPwd) {
        this.assistedQueryPwd = assistedQueryPwd;
    }
    
    public boolean isShadow() {
        return shadow;
    }
    
    public void setShadow(final boolean shadow) {
        this.shadow = shadow;
    }
    
    @Override
    public String toString() {
        return String.format("user_id: %d, user_name: %s, user_name_plain: %s, pwd: %s, assisted_query_pwd: %s, shadow: %s", userId, userName, userNamePlain, pwd, assistedQueryPwd, shadow);
    }
}
