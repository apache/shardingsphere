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

package org.apache.shardingsphere.infra.metadata.auth.refresher.type;

import org.apache.shardingsphere.infra.eventbus.ShardingSphereEventBus;
import org.apache.shardingsphere.infra.metadata.auth.Authentication;
import org.apache.shardingsphere.infra.metadata.auth.model.user.ShardingSphereUser;
import org.apache.shardingsphere.infra.metadata.auth.refresher.AuthenticationRefresher;
import org.apache.shardingsphere.infra.metadata.auth.refresher.event.CreateUserEvent;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dcl.CreateUserStatement;

import java.util.Collection;
import java.util.stream.Collectors;

/**
 * Create user statement auth refresher.
 */
public final class CreateUserStatementAuthRefresher implements AuthenticationRefresher {
    
    @Override
    public void refresh(final Authentication authentication, final SQLStatement sqlStatement) {
        Collection<ShardingSphereUser> users = getUsers((CreateUserStatement) sqlStatement);
        users.addAll(authentication.getAllUsers());
        ShardingSphereEventBus.getInstance().post(new CreateUserEvent(users));
    }
    
    private Collection<ShardingSphereUser> getUsers(final CreateUserStatement sqlStatement) {
        return sqlStatement.getUsers().stream().map(each -> new ShardingSphereUser(each.getUser(), each.getAuth(), null != each.getHost() ? each.getHost() : "%")).collect(Collectors.toList());
    }
}
