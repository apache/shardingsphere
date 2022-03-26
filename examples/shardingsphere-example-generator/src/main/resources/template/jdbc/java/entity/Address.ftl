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
@Table(name = "t_address")
</#if>
public class Address implements Serializable {
    
    private static final long serialVersionUID = 4743102234543827855L;
    
    <#if framework?contains("jpa")>
    @Id
    @Column(name = "address_id")
    </#if>
    private Long addressId;
    
    <#if framework?contains("jpa")>
    @Column(name = "address_name")
    </#if>
    private String addressName;
    
    public Long getAddressId() {
        return addressId;
    }
    
    public void setAddressId(Long addressId) {
        this.addressId = addressId;
    }
    
    public String getAddressName() {
        return addressName;
    }
    
    public void setAddressName(String addressName) {
        this.addressName = addressName;
    }
    
    @Override
    public String toString() {
        return String.format("address_id: %s, address_name: %s", addressId, addressName);
    }
}
