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

package org.apache.shardingsphere.test.e2e.env.runtime.type.scenario.authority;

import org.apache.shardingsphere.database.connector.core.type.DatabaseType;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Collection;
import java.util.LinkedList;
import java.util.stream.Collectors;

/**
 * Authority root XML entry.
 */
@XmlRootElement(name = "authority")
public final class AuthorityEnvironment {
    
    @XmlElement(name = "sqlset")
    private final Collection<AuthorityEnvironmentSQLSet> sqlSets = new LinkedList<>();
    
    /**
     * Get init SQLs.
     *
     * @param databaseType database type
     * @return init SQLs
     */
    public Collection<String> getInitSQLs(final DatabaseType databaseType) {
        return sqlSets.stream().flatMap(each -> each.getInitSQLs(databaseType).stream()).collect(Collectors.toList());
    }
    
    /**
     * Get clean SQLs.
     *
     * @param databaseType database type
     * @return clean SQLs
     */
    public Collection<String> getCleanSQLs(final DatabaseType databaseType) {
        return sqlSets.stream().flatMap(each -> each.getCleanSQLs(databaseType).stream()).collect(Collectors.toList());
    }
}
