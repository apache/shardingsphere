/*
 * Copyright 2016-2018 shardingsphere.io.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingsphere.dbtest.cases.authority;

import io.shardingsphere.core.constant.DatabaseType;
import lombok.Getter;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import java.util.Collection;
import java.util.LinkedList;

/**
 * Authority InitSQL xml entry.
 *
 * @author panjuan
 */
@Getter
@XmlAccessorType(XmlAccessType.FIELD)
public final class InitSQL {
    
    @XmlAttribute(name = "db-types")
    private String dbTypes;
    
    @XmlElement(name = "sql")
    private Collection<SQL> SQLs= new LinkedList<>();
    
    /**
     * Get database type list.
     *
     * @return database type list
     */
    public Collection<DatabaseType> getDatabaseTypeList() {
        Collection<DatabaseType> result = new LinkedList<>();
        for (String each : dbTypes.split(",")) {
            result.add(DatabaseType.valueOf(each));
        }
        return result;
    }
    
    /**
     * Get all sqls content.
     *
     * @return sqls content
     */
    public Collection<String> getAllSQLContent() {
        Collection<String> result = new LinkedList<>();
        for (SQL each : SQLs) {
            result.add(each.getContent());
        }
        return result;
    }
}
