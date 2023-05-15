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

package org.apache.shardingsphere.single.route.validator.ddl;

import org.apache.shardingsphere.infra.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.single.exception.UnsupportedDropCascadeTableException;
import org.apache.shardingsphere.single.route.validator.SingleMetaDataValidator;
import org.apache.shardingsphere.single.rule.SingleRule;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.DropTableStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.handler.ddl.DropTableStatementHandler;

/**
 * Single drop table validator.
 */
public final class SingleDropTableValidator implements SingleMetaDataValidator {
    
    @Override
    public void validate(final SingleRule rule, final SQLStatementContext sqlStatementContext, final ShardingSphereDatabase database) {
        if (DropTableStatementHandler.containsCascade((DropTableStatement) sqlStatementContext.getSqlStatement())) {
            throw new UnsupportedDropCascadeTableException();
        }
    }
}
