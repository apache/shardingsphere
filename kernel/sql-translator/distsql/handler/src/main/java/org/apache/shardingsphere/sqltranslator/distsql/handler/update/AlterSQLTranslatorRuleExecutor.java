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

package org.apache.shardingsphere.sqltranslator.distsql.handler.update;

import lombok.Setter;
import org.apache.shardingsphere.distsql.handler.engine.update.rdl.rule.spi.global.GlobalRuleDefinitionExecutor;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.sqltranslator.api.config.SQLTranslatorRuleConfiguration;
import org.apache.shardingsphere.sqltranslator.distsql.statement.updateable.AlterSQLTranslatorRuleStatement;
import org.apache.shardingsphere.sqltranslator.rule.SQLTranslatorRule;
import org.apache.shardingsphere.sqltranslator.spi.SQLTranslator;

/**
 * Alter SQL translator rule executor.
 */
@Setter
public final class AlterSQLTranslatorRuleExecutor implements GlobalRuleDefinitionExecutor<AlterSQLTranslatorRuleStatement, SQLTranslatorRule> {
    
    private SQLTranslatorRule rule;
    
    @Override
    public void checkBeforeUpdate(final AlterSQLTranslatorRuleStatement sqlStatement) {
        TypedSPILoader.checkService(SQLTranslator.class, sqlStatement.getProvider().getName(), sqlStatement.getProvider().getProps());
    }
    
    @Override
    public SQLTranslatorRuleConfiguration buildToBeAlteredRuleConfiguration(final AlterSQLTranslatorRuleStatement sqlStatement) {
        boolean useOriginalSQLWhenTranslatingFailed = null == sqlStatement.getUseOriginalSQLWhenTranslatingFailed()
                ? rule.getConfiguration().isUseOriginalSQLWhenTranslatingFailed()
                : sqlStatement.getUseOriginalSQLWhenTranslatingFailed();
        return new SQLTranslatorRuleConfiguration(sqlStatement.getProvider().getName(), sqlStatement.getProvider().getProps(), useOriginalSQLWhenTranslatingFailed);
    }
    
    @Override
    public Class<SQLTranslatorRule> getRuleClass() {
        return SQLTranslatorRule.class;
    }
    
    @Override
    public Class<AlterSQLTranslatorRuleStatement> getType() {
        return AlterSQLTranslatorRuleStatement.class;
    }
}
