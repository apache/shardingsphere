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

public class Account implements Serializable {
    
    private static final long serialVersionUID = -5889545274302226912L;
    
    private long accountId;
    
    private int userId;
    
    private String status;
    
    public long getAccountId() {
        return accountId;
    }
    
    public void setAccountId(final long accountId) {
        this.accountId = accountId;
    }
    
    public int getUserId() {
        return userId;
    }
    
    public void setUserId(final int userId) {
        this.userId = userId;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(final String status) {
        this.status = status;
    }
    
    @Override
    public String toString() {
        return String.format("account_id: %s, user_id: %s, status: %s", accountId, userId, status);
    }
}
