package org.apache.shardingsphere.traffic.exception.segment;

import org.apache.shardingsphere.infra.util.exception.external.sql.sqlstate.XOpenSQLState;
import org.apache.shardingsphere.traffic.exception.TrafficException;

public class SegmentTrafficAlgorithmInitializationException extends TrafficException {

    private static final long serialVersionUID = 7514112348846701284L;

    public SegmentTrafficAlgorithmInitializationException(final String segmentTrafficType, final String reason) {
        super(XOpenSQLState.GENERAL_ERROR, 98, "Segmentation traffic algorithm `%s` initialization failed, reason is: %s.", segmentTrafficType, reason);
    }
}
