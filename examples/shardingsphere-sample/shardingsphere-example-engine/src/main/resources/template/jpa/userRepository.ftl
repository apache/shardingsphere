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

package org.apache.shardingsphere.example.${feature?replace('-', '.')}.${framework?replace('-', '.')};

import org.apache.shardingsphere.example.${feature?replace('-', '.')}.${framework?replace('-', '.')}.entity.User;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.transaction.Transactional;
import java.util.List;

<#assign frameworkName="">
<#list framework?split("-") as framework1>
    <#assign frameworkName=frameworkName + framework1?cap_first>
</#list>
<#assign featureName="">
<#list feature?split("-") as feature1>
    <#assign featureName=featureName + feature1?cap_first>
</#list>
@Repository
@Transactional
public class ${mode?cap_first}${transaction?cap_first}${featureName}${frameworkName}Repository {
    
    @PersistenceContext
    private EntityManager entityManager;

    public int insertUser(final User user) {
        entityManager.persist(user);
        return user.getUserId();
    }
    
    public List<User> selectAllUsers() {
        return (List<User>) entityManager.createQuery("SELECT o FROM User o").getResultList();
    }

    public void deleteUser(final int userId) {
        Query query = entityManager.createQuery("DELETE FROM User o WHERE o.userId = ?1");
        query.setParameter(1, userId);
        query.executeUpdate();
    }
}
