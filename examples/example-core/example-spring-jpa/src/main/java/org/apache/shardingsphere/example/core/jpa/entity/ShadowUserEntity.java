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

package org.apache.shardingsphere.example.core.jpa.entity;

import org.apache.shardingsphere.example.core.api.entity.ShadowUser;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "t_shadow_user")
public final class ShadowUserEntity extends ShadowUser {
    
    private static final long serialVersionUID = -3708998745561667721L;
    
    @Id
    @Column(name = "user_id")
    @Override
    public int getUserId() {
        return super.getUserId();
    }
    
    @Column(name = "user_name")
    @Override
    public String getUserName() {
        return super.getUserName();
    }
    
    @Column(name = "pwd")
    @Override
    public String getPwd() {
        return super.getPwd();
    }
    
    
    @Column(name = "shadow")
    @Override
    public boolean isShadow() {
        return super.isShadow();
    }
}
