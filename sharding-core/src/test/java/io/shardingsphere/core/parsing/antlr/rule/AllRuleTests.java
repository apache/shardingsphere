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

package io.shardingsphere.core.parsing.antlr.rule;

import io.shardingsphere.core.parsing.antlr.rule.jaxb.loader.RuleDefinitionFileConstantTest;
import io.shardingsphere.core.parsing.antlr.rule.jaxb.segment.ExtractorRuleDefinitionEntityLoaderTest;
import io.shardingsphere.core.parsing.antlr.rule.jaxb.statement.SQLStatementRuleDefinitionEntityLoaderTest;
import io.shardingsphere.core.parsing.antlr.rule.registry.ParsingRuleRegistryTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({
        RuleDefinitionFileConstantTest.class, 
        SQLStatementRuleDefinitionEntityLoaderTest.class, 
        ExtractorRuleDefinitionEntityLoaderTest.class,
        ParsingRuleRegistryTest.class
})
public final class AllRuleTests {
}
