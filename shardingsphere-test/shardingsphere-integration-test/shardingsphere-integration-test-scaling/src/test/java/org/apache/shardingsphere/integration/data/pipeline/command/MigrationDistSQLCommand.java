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

package org.apache.shardingsphere.integration.data.pipeline.command;

import lombok.Getter;
import lombok.Setter;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@Setter
@XmlRootElement(name = "command")
@XmlAccessorType(XmlAccessType.FIELD)
public final class MigrationDistSQLCommand {
    
    @XmlElement(name = "add-migration-process-config")
    @Getter
    private String addMigrationProcessConfig;
    
    @XmlElement(name = "create-target-order-table-encrypt-rule")
    @Getter
    private String createTargetOrderTableEncryptRule;
    
    @XmlElement(name = "create-target-order-table-rule")
    @Getter
    private String createTargetOrderTableRule;
    
    @XmlElement(name = "create-target-order-item-table-rule")
    @Getter
    private String createTargetOrderItemTableRule;
    
    @XmlElement(name = "add-migration-source-resource-template")
    @Getter
    private String addMigrationSourceResourceTemplate;
    
    @XmlElement(name = "add-migration-target-resource-template")
    @Getter
    private String addMigrationTargetResourceTemplate;
    
    @XmlElement(name = "migration-single-table")
    private String migrationSingleTable;
    
    @XmlElement(name = "migration-single-table-with-schema")
    private String migrationSingleTableWithSchema;
    
    /**
     * Get migration single table DistSQL.
     *
     * @param sourceTableName source table name
     * @param targetTableName target table name
     * @return migration single table DistSQL
     */
    public String getMigrationSingleTable(final String sourceTableName, final String targetTableName) {
        return String.format(migrationSingleTable, sourceTableName, targetTableName);
    }
    
    /**
     * Get migration single table DistSQL.
     *
     * @param sourceTableName source table name
     * @param targetTableName target table name
     * @return migration single table DistSQL
     */
    public String getMigrationSingleTableWithSchema(final String sourceTableName, final String targetTableName) {
        return String.format(migrationSingleTableWithSchema, sourceTableName, targetTableName);
    }
}
