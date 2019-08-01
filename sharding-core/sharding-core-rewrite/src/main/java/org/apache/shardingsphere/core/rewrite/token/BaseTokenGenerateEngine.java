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

package org.apache.shardingsphere.core.rewrite.token;

import org.apache.shardingsphere.core.rewrite.token.generator.InsertRegularNamesTokenGenerator;
import org.apache.shardingsphere.core.rewrite.token.generator.RemoveTokenGenerator;
import org.apache.shardingsphere.core.rewrite.token.generator.SQLTokenGenerator;
import org.apache.shardingsphere.core.rewrite.token.generator.TableTokenGenerator;
import org.apache.shardingsphere.core.rule.BaseRule;

import java.util.Collection;
import java.util.LinkedList;

/**
 * SQL token generator for common.
 *
 * @author panjuan
 */
public final class BaseTokenGenerateEngine extends SQLTokenGenerateEngine<BaseRule> {
    
    private static final Collection<SQLTokenGenerator> SQL_TOKEN_GENERATORS = new LinkedList<>();
    
    static {
        SQL_TOKEN_GENERATORS.add(new InsertRegularNamesTokenGenerator());
        SQL_TOKEN_GENERATORS.add(new RemoveTokenGenerator());
        SQL_TOKEN_GENERATORS.add(new TableTokenGenerator());
    }
    
    @Override
    protected Collection<SQLTokenGenerator> getSQLTokenGenerators() {
        return SQL_TOKEN_GENERATORS;
    }
}
