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

package org.apache.shardingsphere.infra.url.core.arg;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Optional;

/**
 * URL argument line render.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class URLArgumentLineRender {
    
    /**
     * Render argument.
     *
     * @param lines lines to be rendered
     * @param placeholderType configuration content placeholder type
     * @return rendered content
     */
    public static byte[] render(final Collection<String> lines, final URLArgumentPlaceholderType placeholderType) {
        StringBuilder result = new StringBuilder();
        for (String each : lines) {
            Optional<URLArgumentLine> argLine = URLArgumentPlaceholderType.NONE == placeholderType ? Optional.empty() : URLArgumentLine.parse(each);
            result.append(argLine.map(optional -> optional.replaceArgument(placeholderType)).orElse(each)).append(System.lineSeparator());
        }
        return result.toString().getBytes(StandardCharsets.UTF_8);
    }
}
