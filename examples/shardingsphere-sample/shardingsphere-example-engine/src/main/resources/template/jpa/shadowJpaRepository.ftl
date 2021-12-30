<#--
  ~ Licensed to the Apache Software Foundation (ASF) under one or more
  ~ contributor license agreements.  See the NOTICE file distributed with
  ~ this work for additional information regarding copyright ownership.
  ~ The ASF licenses this file to You under the Apache License, Version 2.0
  ~ (the "License"); you may not use this file except in compliance with
  ~ the License.  You may obtain a copy of the License at
  ~
  ~     http://www.apache.org/licenses/LICENSE-2.0
  ~
  ~ Unless required by applicable law or agreed to in writing, software
  ~ distributed under the License is distributed on an "AS IS" BASIS,
  ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  ~ See the License for the specific language governing permissions and
  ~ limitations under the License.
  -->
    
    public int insertUser(final User user) {
        entityManager.persist(user);
        return user.getUserId();
    }
    
    public List<User> selectUsers(final int userType) {
        Query query = entityManager.createQuery("SELECT o FROM User o WHERE o.userType = ?1");
        query.setParameter(1, userType);
        return (List<User>) query.getResultList();
    }

    public void deleteUser(final int userId, final int userType) {
        Query query = entityManager.createQuery("DELETE FROM User o WHERE o.userId = ?1 AND o.userType = ?2");
        query.setParameter(1, userId);
        query.setParameter(2, userType);
        query.executeUpdate();
    }
