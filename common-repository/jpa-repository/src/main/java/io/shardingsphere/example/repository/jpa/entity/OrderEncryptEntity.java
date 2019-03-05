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

package io.shardingsphere.example.repository.jpa.entity;

import io.shardingsphere.example.repository.api.entity.OrderEncrypt;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "t_order_encrypt")
public final class OrderEncryptEntity extends OrderEncrypt {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_id")
    @Override
    public long getOrderId() {
        return super.getOrderId();
    }
    
    @Column(name = "user_id")
    @Override
    public int getUserId() {
        return super.getUserId();
    }
    
    @Column(name = "aes_id")
    @Override
    public String getAesId() {
        return super.getAesId();
    }
    
    @Column(name = "aes_query_id")
    @Override
    public String getAesQueryId() {
        return super.getAesQueryId();
    }
}
