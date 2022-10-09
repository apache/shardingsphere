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
<#assign package = feature?replace('-', '')?replace(',', '.') />

package org.apache.shardingsphere.example.${package}.${framework?replace('-', '.')}.entity;

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
@Table(name = "t_order_item")
</#if>
public class OrderItem implements Serializable {
    
    private static final long serialVersionUID = 1332162822494069342L;
    
    <#if framework?contains("jpa")>
    @Id
    @Column(name = "order_item_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    </#if>
    private long orderItemId;
    
    <#if framework?contains("jpa")>
    @Column(name = "order_id")
    </#if>
    private long orderId;
    
    <#if framework?contains("jpa")>
    @Column(name = "user_id")
    </#if>
    private int userId;
    
    <#if framework?contains("jpa")>
    @Column(name = "phone")
    </#if>
    private String phone;
    
    <#if framework?contains("jpa")>
    @Column(name = "status")
    </#if>
    private String status;
    
    public long getOrderItemId() {
        return orderItemId;
    }
    
    public void setOrderItemId(final long orderItemId) {
        this.orderItemId = orderItemId;
    }
    
    public long getOrderId() {
        return orderId;
    }
    
    public void setOrderId(final long orderId) {
        this.orderId = orderId;
    }
    
    public int getUserId() {
        return userId;
    }
    
    public void setUserId(final int userId) {
        this.userId = userId;
    }
    
    public String getPhone() {
        return phone;
    }
    
    public void setPhone(String phone) {
        this.phone = phone;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(final String status) {
        this.status = status;
    }
    
    @Override
    public String toString() {
        return String.format("order_item_id:%s, order_id: %s, user_id: %s, phone: %s, status: %s", orderItemId, orderId, userId, phone, status);
    }
}
