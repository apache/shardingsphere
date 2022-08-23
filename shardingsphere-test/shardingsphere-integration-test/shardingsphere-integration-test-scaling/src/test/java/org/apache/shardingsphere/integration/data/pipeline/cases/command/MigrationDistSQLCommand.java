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
public final class MigrationDistSQLCommand {
    
    @XmlElement(name = "create-target-order-table-rule")
    private String createTargetOrderTableRule;
    
    @XmlElement(name = "create-target-order-item-table-rule")
    private String createTargetOrderItemTableRule;
    
    @XmlElement(name = "add-migration-source-resource-template")
    private String addMigrationSourceResourceTemplate;
    
    @XmlElement(name = "add-migration-target-resource-template")
    private String addMigrationTargetResourceTemplate;
    
    @XmlElement(name = "migration-order-single-table")
    private String migrationOrderSingleTable;
    
    @XmlElement(name = "migration-order-item-single-table")
    private String migrationOrderItemSingleTable;
    
    @XmlElement(name = "migration-order-single-table-with-schema")
    private String migrationOrderSingleTableWithSchema;
    
    @XmlElement(name = "migration-order-item-single-table-with-schema")
    private String migrationOrderItemSingleTableWithSchema;
}
