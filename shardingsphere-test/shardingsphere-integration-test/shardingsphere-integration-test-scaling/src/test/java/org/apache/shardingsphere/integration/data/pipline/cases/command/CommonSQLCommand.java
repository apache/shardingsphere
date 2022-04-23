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

package org.apache.shardingsphere.integration.data.pipline.cases.command;

import lombok.Data;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@Data
@XmlRootElement(name = "command")
@XmlAccessorType(XmlAccessType.FIELD)
public final class CommonSQLCommand {
    
    @XmlElement(name = "add-resource")
    private String addResource;
    
    @XmlElement(name = "create-database")
    private String createDatabase;
    
    @XmlElement(name = "use-database")
    private String useDatabase;
    
    @XmlElement(name = "create-sharding-algorithm")
    private String createShardingAlgorithm;
    
    @XmlElement(name = "create-sharding-table")
    private String createShardingTable;
    
    @XmlElement(name = "create-sharding-binding")
    private String createShardingBinding;
    
    @XmlElement(name = "create-sharding-scaling-rule")
    private String createShardingScalingRule;
    
    @XmlElement(name = "alter-sharding-algorithm")
    private String alterShardingAlgorithm;
    
    @XmlElement(name = "alter-sharding-table-rule")
    private String alterShardingTableRule;
    
    @XmlElement(name = "preview-select-order")
    private String previewSelectOrder;
    
    @XmlElement(name = "show-scaling-status")
    private String showScalingStatus;
    
    @XmlElement(name = "show-scaling-list")
    private String showScalingList;
    
    @XmlElement(name = "stop-scaling-source-writing")
    private String stopScalingSourceWriting;
    
    @XmlElement(name = "check-scaling-data-match")
    private String checkScalingDataMatch;
    
    @XmlElement(name = "apply-scaling")
    private String applyScaling;
}
