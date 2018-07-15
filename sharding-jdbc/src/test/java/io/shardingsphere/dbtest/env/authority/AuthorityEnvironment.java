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

package io.shardingsphere.dbtest.env.authority;

import io.shardingsphere.core.constant.DatabaseType;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Collection;
import java.util.LinkedList;

/**
 * Authority root xml entry.
 * 
 * @author panjuan
 */
@XmlRootElement(name = "authority")
public final class AuthorityEnvironment {
    
    @XmlElement(name = "sqlset")
    private Collection<AuthoritySQLSet> sqlSets = new LinkedList<>();
    
    /**
     * Get init sqls of this database type.
     *
     * @param databaseType database type
     * @return init sqls of this data base type
     */
    public Collection<String> getInitSQLs(final DatabaseType databaseType) {
        Collection<String> result = new LinkedList<>();
        for (AuthoritySQLSet each : sqlSets) {
            result.addAll(each.getCreateUserSQLs(databaseType));
        }
        return result;
    }
    
    /**
     * Get clean sqls of this database type.
     *
     * @param databaseType database type
     * @return clean sqls of this database type
     */
    public Collection<String> getCleanSQLs(final DatabaseType databaseType) {
        Collection<String> result = new LinkedList<>();
        for (AuthoritySQLSet each : sqlSets) {
            result.addAll(each.getDropUserSQLs(databaseType));
        }
        return result;
    }
}
