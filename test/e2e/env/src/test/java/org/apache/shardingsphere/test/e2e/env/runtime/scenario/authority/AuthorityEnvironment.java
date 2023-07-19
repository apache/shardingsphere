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

package org.apache.shardingsphere.test.e2e.env.runtime.scenario.authority;

import org.apache.shardingsphere.infra.database.spi.DatabaseType;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Collection;
import java.util.LinkedList;

/**
 * Authority root xml entry.
 */
@XmlRootElement(name = "authority")
public final class AuthorityEnvironment {
    
    @XmlElement(name = "sqlset")
    private final Collection<AuthoritySQLSet> sqlSets = new LinkedList<>();
    
    /**
     * Get init SQLs of this database type.
     *
     * @param databaseType database type
     * @return init SQLs of this data base type
     */
    public Collection<String> getInitSQLs(final DatabaseType databaseType) {
        Collection<String> result = new LinkedList<>();
        for (AuthoritySQLSet each : sqlSets) {
            result.addAll(each.getCreateUserSQLs(databaseType));
        }
        return result;
    }
    
    /**
     * Get clean SQLs of this database type.
     *
     * @param databaseType database type
     * @return clean SQLs of this database type
     */
    public Collection<String> getCleanSQLs(final DatabaseType databaseType) {
        Collection<String> result = new LinkedList<>();
        for (AuthoritySQLSet each : sqlSets) {
            result.addAll(each.getDropUserSQLs(databaseType));
        }
        return result;
    }
}
