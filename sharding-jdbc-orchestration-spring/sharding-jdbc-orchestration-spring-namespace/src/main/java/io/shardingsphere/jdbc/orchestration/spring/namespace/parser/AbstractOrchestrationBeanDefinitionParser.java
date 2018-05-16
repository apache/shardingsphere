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

package io.shardingsphere.jdbc.orchestration.spring.namespace.parser;

import io.shardingsphere.jdbc.orchestration.api.config.OrchestrationConfiguration;
import io.shardingsphere.jdbc.orchestration.api.config.OrchestrationType;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractBeanDefinitionParser;
import org.w3c.dom.Element;

/**
 * Abstract orchestration parser for spring namespace.
 * 
 * @author caohao
 */
public abstract class AbstractOrchestrationBeanDefinitionParser extends AbstractBeanDefinitionParser {
    
    protected String parseRegistryCenterRef(final Element element) {
        return element.getAttribute("registry-center-ref");
    }
    
    protected BeanDefinition parseOrchestrationConfiguration(final Element element, final OrchestrationType type) {
        BeanDefinitionBuilder factory = BeanDefinitionBuilder.rootBeanDefinition(OrchestrationConfiguration.class);
        factory.addConstructorArgValue(element.getAttribute("id"));
        factory.addConstructorArgReference(element.getAttribute("registry-center-ref"));
        factory.addConstructorArgValue(Boolean.parseBoolean(element.getAttribute("overwrite")));
        factory.addConstructorArgValue(type);
        return factory.getBeanDefinition();
    }
}
