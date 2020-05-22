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

package org.apache.shardingsphere.dbtest.env.authority;

import com.google.common.base.Splitter;
import lombok.Setter;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.database.type.DatabaseTypes;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Authority SQL set xml entry.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@Setter
public final class AuthoritySQLSet {
    
    @XmlAttribute(name = "db-types", required = true)
    private String databaseTypes;
    
    @XmlElementWrapper(name = "user-create")
    @XmlElement(name = "sql")
    private List<String> useCreateSQLs = new LinkedList<>();
    
    @XmlElementWrapper(name = "user-drop")
    @XmlElement(name = "sql")
    private List<String> useDropSQLs = new LinkedList<>();
    
    /**
     * Get all create user SQLs.
     *
     * @param databaseType database type
     * @return create user SQLs
     */
    public Collection<String> getCreateUserSQLs(final DatabaseType databaseType) {
        return getDatabaseTypes().contains(databaseType) ? useCreateSQLs : Collections.emptyList();
    }
    
    /**
     * Get all drop user SQLs.
     *
     * @param databaseType database type
     * @return create user SQLs
     */
    public Collection<String> getDropUserSQLs(final DatabaseType databaseType) {
        return getDatabaseTypes().contains(databaseType) ? useDropSQLs : Collections.emptyList();
    }
    
    private Collection<DatabaseType> getDatabaseTypes() {
        return Splitter.on(",").trimResults().splitToList(databaseTypes).stream().map(DatabaseTypes::getActualDatabaseType).collect(Collectors.toList());
    }
}
