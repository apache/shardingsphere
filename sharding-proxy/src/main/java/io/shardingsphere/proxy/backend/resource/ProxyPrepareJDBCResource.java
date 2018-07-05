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

package io.shardingsphere.proxy.backend.resource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;

/**
 * Proxy jdbc resource model for prepare statement.
 *
 * @author zhaojun
 */
public class ProxyPrepareJDBCResource extends BaseJDBCResource {
    
    private List<PreparedStatement> preparedStatements;
    
    public ProxyPrepareJDBCResource(final List<Connection> connections, final List<PreparedStatement> preparedStatements, final List<ResultSet> resultSets) {
        super(connections, resultSets);
        this.preparedStatements = preparedStatements;
    }
    
    /**
     * Add prepare statement to resource manager.
     *
     * @param preparedStatement prepare statement
     */
    public void addPrepareStatement(final PreparedStatement preparedStatement) {
        preparedStatements.add(preparedStatement);
    }
}
