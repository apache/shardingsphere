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

package org.apache.shardingsphere.core.parse.core.rule.jaxb.loader.filler;

import lombok.SneakyThrows;
import org.apache.shardingsphere.core.parse.core.rule.jaxb.entity.filler.FillerRuleDefinitionEntity;
import org.apache.shardingsphere.core.parse.core.rule.jaxb.loader.RuleDefinitionEntityLoader;

import javax.xml.bind.JAXBContext;
import java.io.InputStream;

/**
 * Filler rule definition entity loader for JAXB.
 *
 * @author zhangliang
 */
public final class FillerRuleDefinitionEntityLoader implements RuleDefinitionEntityLoader {
    
    @Override
    @SneakyThrows
    public FillerRuleDefinitionEntity load(final String fillerRuleDefinitionFile) {
        InputStream inputStream = FillerRuleDefinitionEntityLoader.class.getClassLoader().getResourceAsStream(fillerRuleDefinitionFile);
        return null == inputStream
                ? new FillerRuleDefinitionEntity() : (FillerRuleDefinitionEntity) JAXBContext.newInstance(FillerRuleDefinitionEntity.class).createUnmarshaller().unmarshal(inputStream);
    }
}
