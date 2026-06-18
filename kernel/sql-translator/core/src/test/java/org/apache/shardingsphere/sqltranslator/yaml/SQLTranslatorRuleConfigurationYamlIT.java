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

package org.apache.shardingsphere.sqltranslator.yaml;

import org.apache.shardingsphere.infra.util.props.PropertiesBuilder;
import org.apache.shardingsphere.infra.util.props.PropertiesBuilder.Property;
import org.apache.shardingsphere.sqltranslator.config.SQLTranslatorRuleConfiguration;
import org.apache.shardingsphere.test.it.yaml.YamlRuleConfigurationIT;

class SQLTranslatorRuleConfigurationYamlIT extends YamlRuleConfigurationIT {
    
    SQLTranslatorRuleConfigurationYamlIT() {
        super("yaml/sql-translator-rule.yaml", new SQLTranslatorRuleConfiguration("CORE:FIXTURE", PropertiesBuilder.build(new Property("k0", "v0"), new Property("k1", "v1")), true));
    }
}
