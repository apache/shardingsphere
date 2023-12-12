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

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import lombok.Getter;

/**
 * JDBC repository SQL.
 */
@JacksonXmlRootElement(localName = "sql")
@Getter
public final class JDBCRepositorySQL {
    
    @JacksonXmlProperty(isAttribute = true)
    private String type;
    
    @JacksonXmlProperty(localName = "driver-class-name", isAttribute = true)
    private String driverClassName;
    
    @JacksonXmlProperty(localName = "default", isAttribute = true)
    private boolean isDefault;
    
    @JacksonXmlProperty(localName = "create-table")
    private String createTableSQL;
    
    @JacksonXmlProperty(localName = "select-by-key")
    private String selectByKeySQL;
    
    @JacksonXmlProperty(localName = "select-by-parent")
    private String selectByParentKeySQL;
    
    @JacksonXmlProperty(localName = "insert")
    private String insertSQL;
    
    @JacksonXmlProperty(localName = "update")
    private String updateSQL;
    
    @JacksonXmlProperty(localName = "delete")
    private String deleteSQL;
}
