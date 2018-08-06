package io.shardingsphere.transaction.innersaga.mock;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Saga child transaction for each sql
 * need service comb implement
 *
 * @author yangyi
 */
@AllArgsConstructor
@Getter
public class SagaSubTransaction {
    /**
     * sub transaction id
     */
    private String subId;

    /**
     * datasource name for this part saga transaction
     */
    private String datasource;

    /**
     * the confirm sql of this part saga transaction
     */
    private String confirm;

    /**
     * the cancel sql of this part saga transaction
     */
    private String cancel;
}
