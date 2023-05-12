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

package org.apache.shardingsphere.mode.repository.standalone.jdbc.provider;

import lombok.Getter;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * JDBC repository provider.
 */
@XmlRootElement(name = "sql")
@Getter
public class JDBCRepositoryProvider {
    
    /**
     * JDBCRepositoryProvider type.
     */
    @XmlAttribute(name = "type", required = true)
    private String type;
    
    /**
     * JDBCRepositoryProvider driverClassName.
     */
    @XmlAttribute(name = "driver-class-name", required = true)
    private String driverClassName;
    
    /**
     * Whether it is the default provider.
     */
    @XmlAttribute(name = "is-default")
    private Boolean isDefault = false;
    
    /**
     * Create table SQL.
     */
    @XmlElement(name = "create-table", required = true)
    private String createTableSQL;
    
    /**
     * Select by key SQL.
     */
    @XmlElement(name = "select-by-key", required = true)
    private String selectByKeySQL;
    
    /**
     * Select by parent key SQL.
     */
    @XmlElement(name = "select-by-parent", required = true)
    private String selectByParentKeySQL;
    
    /**
     * Insert SQL.
     */
    @XmlElement(name = "insert", required = true)
    private String insertSQL;
    
    /**
     * Update SQL.
     */
    @XmlElement(name = "update", required = true)
    private String updateSQL;
    
    /**
     * Delete SQL.
     */
    @XmlElement(name = "delete", required = true)
    private String deleteSQL;
    
}
