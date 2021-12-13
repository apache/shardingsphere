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

package org.apache.shardingsphere.example.${feature?replace('-', '.')}.${framework?replace('-', '.')}.entity;

<#if framework?contains("jpa")>
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
</#if>
import java.io.Serializable;

<#if framework?contains("jpa")>
@Entity
@Table(name = "t_user")
</#if>
public class User implements Serializable {
    
    private static final long serialVersionUID = 263434701950670170L;
    
    <#if framework?contains("jpa")>
    @Id
    @Column(name = "user_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    </#if>
    private int userId;
    
    <#if framework?contains("jpa")>
    @Column(name = "user_name")
    </#if>
    private String userName;
    
    <#if framework?contains("jpa")>
    @Column(name = "pwd")
    </#if>
    private String pwd;
    
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
    
    public String getPwd() {
        return pwd;
    }
    
    public void setPwd(final String pwd) {
        this.pwd = pwd;
    }
    
    @Override
    public String toString() {
        return String.format("user_id: %d, user_name: %s, pwd: %s", userId, userName, pwd);
    }
}
