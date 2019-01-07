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

package io.shardingsphere.transaction.core.loader;

import com.google.common.base.Optional;
import io.shardingsphere.spi.NewInstanceServiceLoader;
import io.shardingsphere.transaction.api.TransactionType;
import io.shardingsphere.transaction.spi.TransactionalDataSourceConverter;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

/**
 * Transactional data source converter SPI loader.
 *
 * @author zhaojun
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class TransactionalDataSourceConverterSPILoader {
    
    private static final Map<TransactionType, TransactionalDataSourceConverter> CONVERTERS = new HashMap<>();
    
    static {
        for (TransactionalDataSourceConverter each : NewInstanceServiceLoader.load(TransactionalDataSourceConverter.class)) {
            CONVERTERS.put(each.getType(), each);
        }
    }
    
    /**
     * Find transactional data source converter.
     * 
     * @param type transaction type
     * @return data source converter
     */
    public static Optional<TransactionalDataSourceConverter> findConverter(final TransactionType type) {
        return Optional.fromNullable(CONVERTERS.get(type));
    }
}
