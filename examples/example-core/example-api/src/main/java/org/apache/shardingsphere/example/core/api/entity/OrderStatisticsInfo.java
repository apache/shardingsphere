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
import java.util.Date;

public class OrderStatisticsInfo implements Serializable {
    
    private static final long serialVersionUID = -1770007969944794302L;
    
    private Long id;
    
    private Long userId;
    
    private Date orderDate;
    
    private int orderNum;
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Long getUserId() {
        return userId;
    }
    
    public void setUserId(Long userId) {
        this.userId = userId;
    }
    
    public Date getOrderDate() {
        return orderDate;
    }
    
    public void setOrderDate(Date orderDate) {
        this.orderDate = orderDate;
    }
    
    public int getOrderNum() {
        return orderNum;
    }
    
    public void setOrderNum(int orderNum) {
        this.orderNum = orderNum;
    }
    
    @Override
    public String toString() {
        return String.format("id: %s, user_id: %s, order_date: %s, order_num: %s", id, userId, orderDate, orderNum);
    }
}
