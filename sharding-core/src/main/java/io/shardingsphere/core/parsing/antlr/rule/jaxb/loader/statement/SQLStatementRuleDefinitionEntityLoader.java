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

package io.shardingsphere.core.parsing.antlr.rule.jaxb.loader.statement;

import com.google.common.base.Preconditions;
import io.shardingsphere.core.parsing.antlr.rule.jaxb.entity.statement.SQLStatementRuleDefinitionEntity;
import io.shardingsphere.core.parsing.antlr.rule.jaxb.loader.RuleDefinitionEntityLoader;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import java.io.File;
import java.net.URL;

/**
 * SQL statement rule definition entity loader for JAXB.
 *
 * @author zhangliang
 */
public final class SQLStatementRuleDefinitionEntityLoader implements RuleDefinitionEntityLoader {
    
    @Override
    public SQLStatementRuleDefinitionEntity load(final String sqlStatementRuleDefinitionFile) throws JAXBException {
        URL url = SQLStatementRuleDefinitionEntityLoader.class.getClassLoader().getResource(sqlStatementRuleDefinitionFile);
        Preconditions.checkNotNull(url, "Cannot load SQL statement rule definition file.");
        return (SQLStatementRuleDefinitionEntity) JAXBContext.newInstance(SQLStatementRuleDefinitionEntity.class).createUnmarshaller().unmarshal(new File(url.getPath()));
    }
}
