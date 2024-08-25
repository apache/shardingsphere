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

package org.apache.shardingsphere.driver.executor.engine.facade.standard;

import org.apache.shardingsphere.driver.executor.engine.facade.DriverExecutorFacade;
import org.apache.shardingsphere.driver.executor.engine.facade.DriverExecutorFacadeFactory;
import org.apache.shardingsphere.driver.jdbc.core.connection.ShardingSphereConnection;
import org.apache.shardingsphere.driver.jdbc.core.statement.StatementManager;
import org.apache.shardingsphere.infra.executor.sql.prepare.driver.jdbc.StatementOption;

/**
 * Standard driver executor facade factory.
 */
public final class StandardDriverExecutorFacadeFactory implements DriverExecutorFacadeFactory {
    
    @Override
    public DriverExecutorFacade newInstance(final ShardingSphereConnection connection,
                                            final StatementOption statementOption, final StatementManager statementManager, final String jdbcDriverType) {
        return new StandardDriverExecutorFacade(connection, statementOption, statementManager, jdbcDriverType);
    }
    
    @Override
    public Object getType() {
        return "Standard";
    }
    
    @Override
    public boolean isDefault() {
        return true;
    }
}
