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

package org.apache.shardingsphere.transaction.spi;

import org.apache.shardingsphere.infra.config.schema.SchemaConfiguration;
import org.apache.shardingsphere.infra.instance.InstanceContext;
import org.apache.shardingsphere.spi.typed.TypedSPI;
import org.apache.shardingsphere.transaction.config.TransactionRuleConfiguration;
import org.apache.shardingsphere.transaction.rule.TransactionRule;

import java.util.Properties;

/**
 * Transaction configuration file generator.
 */
public interface TransactionConfigurationFileGenerator extends TypedSPI {
    
    /**
     * Generate transaction configuration file.
     *
     * @param transactionRule transaction rule
     * @param instanceContext instance context
     */
    void generateFile(TransactionRule transactionRule, InstanceContext instanceContext);
    
    /**
     * Get transaction configuration.
     *
     * @param transactionRuleConfiguration transaction rule configuration
     * @param schemaConfiguration schema configuration
     * @return transaction rule props
     */
    Properties getTransactionProps(TransactionRuleConfiguration transactionRuleConfiguration, SchemaConfiguration schemaConfiguration);
}
