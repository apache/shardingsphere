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

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import lombok.Getter;

/**
 * JDBC repository SQL.
 * `required` in {@link com.fasterxml.jackson.annotation.JsonProperty} only provides Metadata without detecting Null values, which is actually consistent with the design of the JAXB API.
 * See <a href="https://github.com/FasterXML/jackson-dataformat-xml/issues/625">FasterXML/jackson-dataformat-xml#625</a>
 *
 * @see JsonProperty
 */
@JacksonXmlRootElement(localName = "sql")
@Getter
public final class JDBCRepositorySQL {
    
    @JsonProperty(required = true)
    @JacksonXmlProperty(isAttribute = true)
    private String type;
    
    @JsonProperty(required = true)
    @JacksonXmlProperty(localName = "driver-class-name", isAttribute = true)
    private String driverClassName;
    
    @JacksonXmlProperty(localName = "default", isAttribute = true)
    private boolean isDefault;
    
    @JsonProperty(required = true)
    @JacksonXmlProperty(localName = "create-table")
    private String createTableSQL;
    
    @JsonProperty(required = true)
    @JacksonXmlProperty(localName = "select-by-key")
    private String selectByKeySQL;
    
    @JsonProperty(required = true)
    @JacksonXmlProperty(localName = "select-by-parent")
    private String selectByParentKeySQL;
    
    @JsonProperty(required = true)
    @JacksonXmlProperty(localName = "insert")
    private String insertSQL;
    
    @JsonProperty(required = true)
    @JacksonXmlProperty(localName = "update")
    private String updateSQL;
    
    @JsonProperty(required = true)
    @JacksonXmlProperty(localName = "delete")
    private String deleteSQL;
}
