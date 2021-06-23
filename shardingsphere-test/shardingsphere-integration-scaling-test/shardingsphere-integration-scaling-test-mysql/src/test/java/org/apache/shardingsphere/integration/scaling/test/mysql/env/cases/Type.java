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

package org.apache.shardingsphere.integration.scaling.test.mysql.env.cases;

import lombok.Getter;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import java.util.LinkedList;
import java.util.List;

/**
 * Type.
 */
@Getter
@XmlAccessorType(XmlAccessType.FIELD)
public final class Type {
    
    private static final String TABLE_NAME_PREFIX = "type_it_";
    
    private static final String COLUMN_NAME = "c1";
    
    @XmlAttribute(required = true)
    private String name;
    
    @XmlElement(name = "value")
    private final List<String> values = new LinkedList<>();
    
    /**
     * Get table name.
     *
     * @return table name
     */
    public String getTableName() {
        return TABLE_NAME_PREFIX + name.replaceAll("[^\\w]", "_");
    }
    
    /**
     * Get column name.
     *
     * @return column name
     */
    public String getColumnName() {
        return COLUMN_NAME;
    }
    
    /**
     * Get column type.
     *
     * @return column type
     */
    public String getColumnType() {
        return name;
    }
}
