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

package org.apache.shardingsphere.shadow.rewrite.token.generator;

import lombok.Getter;
import lombok.Setter;
import org.apache.shardingsphere.shadow.rewrite.aware.ShadowRuleAware;
import org.apache.shardingsphere.shadow.rule.ShadowRule;
import org.apache.shardingsphere.infra.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.rewrite.sql.token.generator.SQLTokenGenerator;

/**
 * Base SQL token generator for shadow.
 */
@Getter
@Setter
public abstract class BaseShadowSQLTokenGenerator implements SQLTokenGenerator, ShadowRuleAware {
    
    private ShadowRule shadowRule;
    
    @Override
    public final boolean isGenerateSQLToken(final SQLStatementContext sqlStatementContext) {
        return isGenerateSQLTokenForShadow(sqlStatementContext);
    }
    
    protected abstract boolean isGenerateSQLTokenForShadow(SQLStatementContext sqlStatementContext);
}
