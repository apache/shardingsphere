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

import org.apache.shardingsphere.example.core.api.entity.OrderItem;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "t_order_item")
public final class OrderItemEntity extends OrderItem {
    
    private static final long serialVersionUID = 5685474394188443341L;
    
    @Id
    @Column(name = "order_item_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Override
    public long getOrderItemId() {
        return super.getOrderItemId();
    }
    
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
    
    @Column(name = "status")
    @Override
    public String getStatus() {
        return super.getStatus();
    }
}
