package org.apache.shardingsphere.traffic.exception.segment;

import org.apache.shardingsphere.infra.util.exception.external.sql.sqlstate.XOpenSQLState;
import org.apache.shardingsphere.traffic.exception.TrafficException;

public class SegmentTrafficAlgorithmInitializationException extends TrafficException {

    public SegmentTrafficAlgorithmInitializationException(final String methodName, final String reason) {
        super(XOpenSQLState.INVALID_PARAMETER_VALUE, 1, "Segmentation traffic algorithm `%s` initialization failed, reason is: %s.", methodName, reason);
    }
}
