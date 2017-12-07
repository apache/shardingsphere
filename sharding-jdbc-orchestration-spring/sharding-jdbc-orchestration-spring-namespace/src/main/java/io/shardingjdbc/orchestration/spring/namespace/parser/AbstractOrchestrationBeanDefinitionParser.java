/*
 * Copyright 1999-2015 dangdang.com.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingjdbc.orchestration.spring.namespace.parser;

import io.shardingjdbc.orchestration.api.config.OrchestrationConfiguration;
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
    
    protected BeanDefinition parseOrchestrationConfiguration(final Element element) {
        BeanDefinitionBuilder factory = BeanDefinitionBuilder.rootBeanDefinition(OrchestrationConfiguration.class);
        factory.addConstructorArgValue(element.getAttribute("id"));
        factory.addConstructorArgReference(element.getAttribute("registry-center-ref"));
        factory.addConstructorArgValue(Boolean.parseBoolean(element.getAttribute("overwrite")));
        return factory.getBeanDefinition();
    }
}
