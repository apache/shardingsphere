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

package com.dangdang.ddframe.rdb.sharding.parser.jaxb;

import com.google.common.base.Optional;
import com.google.common.base.Strings;

import javax.xml.bind.annotation.adapters.XmlAdapter;

class OptionalAdapter extends XmlAdapter<String, Optional<Integer>> {
    
    @Override
    public Optional<Integer> unmarshal(final String v) throws Exception {
        if (Strings.isNullOrEmpty(v)) {
            return Optional.absent();
        }
        return Optional.of(Integer.valueOf(v));
    }
    
    @Override
    public String marshal(final Optional<Integer> v) throws Exception {
        if (v.isPresent()) {
            return v.get().toString();
        }
        return "";
    }
}
