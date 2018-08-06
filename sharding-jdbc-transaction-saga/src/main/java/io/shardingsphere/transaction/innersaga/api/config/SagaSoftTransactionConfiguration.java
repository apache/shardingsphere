package io.shardingsphere.transaction.innersaga.api.config;

import io.shardingsphere.transaction.innersaga.mock.SagaTransactionConfiguration;

import javax.sql.DataSource;

/**
 * Saga soft transaction configuration
 * wrap the configuration of actual saga configuration
 *
 * @author yangyi
 */

public class SagaSoftTransactionConfiguration extends SagaTransactionConfiguration {

    public SagaSoftTransactionConfiguration(DataSource targetDataSource) {
        super(targetDataSource);
    }
}
