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

package org.apache.shardingsphere.mode.repository.standalone.jdbc.sql;

import lombok.Getter;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * JDBC repository SQL.
 */
@XmlRootElement(name = "sql")
@Getter
public final class JDBCRepositorySQL {
    
    @XmlAttribute(required = true)
    private String type;
    
    @XmlAttribute(name = "driver-class-name", required = true)
    private String driverClassName;
    
    @XmlAttribute(name = "default")
    private boolean isDefault;
    
    @XmlElement(name = "create-table", required = true)
    private String createTableSQL;
    
    @XmlElement(name = "select-by-key", required = true)
    private String selectByKeySQL;
    
    @XmlElement(name = "select-by-parent", required = true)
    private String selectByParentKeySQL;
    
    @XmlElement(name = "insert", required = true)
    private String insertSQL;
    
    @XmlElement(name = "update", required = true)
    private String updateSQL;
    
    @XmlElement(name = "delete", required = true)
    private String deleteSQL;
}
