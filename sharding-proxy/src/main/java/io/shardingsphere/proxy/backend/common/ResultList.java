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

package io.shardingsphere.proxy.backend.common;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@NoArgsConstructor
@Getter
@Setter
public class ResultList {
    
    private final List<Object> resultList = new CopyOnWriteArrayList<>();
    
    private Iterator<Object> iterator;
    
    /**
     * Add object.
     *
     * @param object object to add
     */
    public void add(final Object object) {
        resultList.add(object);
    }
    
    /**
     * Has next.
     *
     * @return has next
     */
    public boolean hasNext() {
        return iterator.hasNext();
    }
    
    /**
     * Next object.
     *
     * @return object
     */
    public Object next() {
        return iterator.next();
    }
}
