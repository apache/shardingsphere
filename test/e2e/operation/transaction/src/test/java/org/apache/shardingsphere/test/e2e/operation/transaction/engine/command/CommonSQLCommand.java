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

package org.apache.shardingsphere.test.e2e.operation.transaction.engine.command;

import lombok.Getter;
import lombok.Setter;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "command")
@XmlAccessorType(XmlAccessType.FIELD)
@Getter
@Setter
public final class CommonSQLCommand {
    
    @XmlElement(name = "create-order-table-rule")
    private String createOrderTableRule;
    
    @XmlElement(name = "create-order-item-table-rule")
    private String createOrderItemTableRule;
    
    @XmlElement(name = "create-account-table-rule")
    private String createAccountTableRule;
    
    @XmlElement(name = "create-one-data-source-account-table-rule")
    private String createOneDataSourceAccountTableRule;
    
    @XmlElement(name = "create-three-data-source-account-table-rule")
    private String createThreeDataSourceAccountTableRule;
    
    @XmlElement(name = "create-address-broadcast-table-rule")
    private String createAddressBroadcastTableRule;
    
    @XmlElement(name = "alter-local-transaction-rule")
    private String alterLocalTransactionRule;
    
    @XmlElement(name = "alter-xa-transaction-rule")
    private String alterXATransactionRule;
    
    @XmlElement(name = "source-add-resource-template")
    private String sourceAddResourceTemplate;
    
    @XmlElement(name = "source-add-new-resource-template")
    private String sourceAddNewResourceTemplate;
    
    @XmlElement(name = "create-account-table")
    private String createAccountTable;
    
    @XmlElement(name = "create-order-table")
    private String createOrderTable;
    
    @XmlElement(name = "create-order-item-table")
    private String createOrderItemTable;
    
    @XmlElement(name = "create-address-table")
    private String createAddressTable;
}
