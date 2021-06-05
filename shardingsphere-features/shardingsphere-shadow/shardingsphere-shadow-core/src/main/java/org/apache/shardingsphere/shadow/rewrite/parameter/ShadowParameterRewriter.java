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

package org.apache.shardingsphere.shadow.rewrite.parameter;

import lombok.Getter;
import lombok.Setter;
import org.apache.shardingsphere.shadow.rewrite.aware.ShadowRuleAware;
import org.apache.shardingsphere.shadow.rule.ShadowRule;
import org.apache.shardingsphere.infra.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.rewrite.parameter.rewriter.ParameterRewriter;

/**
 * Parameter rewriter for shadow.
 * 
 * @param <T> type of SQL statement context
 */
@Getter
@Setter
public abstract class ShadowParameterRewriter<T extends SQLStatementContext> implements ParameterRewriter<T>, ShadowRuleAware {
    
    private ShadowRule shadowRule;
    
    @Override
    public final boolean isNeedRewrite(final SQLStatementContext sqlStatementContext) {
        return isNeedRewriteForShadow(sqlStatementContext);
    }
    
    protected abstract boolean isNeedRewriteForShadow(SQLStatementContext sqlStatementContext);
}
