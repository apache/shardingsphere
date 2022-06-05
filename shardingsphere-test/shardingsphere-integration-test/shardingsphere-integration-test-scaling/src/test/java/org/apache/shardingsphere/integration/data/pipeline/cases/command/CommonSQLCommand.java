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

package org.apache.shardingsphere.integration.data.pipeline.cases.command;

import lombok.Data;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@Data
@XmlRootElement(name = "command")
@XmlAccessorType(XmlAccessType.FIELD)
public final class CommonSQLCommand {
    
    @XmlElement(name = "create-database-sharding-algorithm")
    private String createDatabaseShardingAlgorithm;
    
    @XmlElement(name = "create-order-sharding-algorithm")
    private String createOrderShardingAlgorithm;
    
    @XmlElement(name = "create-order-item-sharding-algorithm")
    private String createOrderItemShardingAlgorithm;
    
    @XmlElement(name = "create-order-with-item-sharding-table-rule")
    private String createOrderWithItemSharingTableRule;
    
    @XmlElement(name = "create-order-sharding-table-rule")
    private String createOrderShardingTableRule;
    
    @XmlElement(name = "alter-sharding-algorithm")
    private String alterShardingAlgorithm;
    
    @XmlElement(name = "alter-sharding-table-rule")
    private String alterShardingTableRule;
    
    @XmlElement(name = "auto-alter-order-with-item-sharding-table-rule")
    private String autoAlterOrderWithItemShardingTableRule;
    
    @XmlElement(name = "auto-alter-order-sharding-table-rule")
    private String autoAlterOrderShardingTableRule;
    
    @XmlElement(name = "source-add-resource-template")
    private String sourceAddResourceTemplate;
    
    @XmlElement(name = "target-add-resource-template")
    private String targetAddResourceTemplate;
}
