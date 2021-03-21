package org.apache.shardingsphere.infra.executor.exec.tool;

import com.google.common.collect.Iterables;
import com.google.common.collect.Ordering;
import org.apache.calcite.rel.RelCollation;
import org.apache.calcite.rel.RelFieldCollation;
import org.apache.calcite.rel.RelFieldCollation.Direction;
import org.apache.shardingsphere.infra.executor.exec.meta.Row;

import java.util.Comparator;
import java.util.List;

public class RowComparatorUtil {
    
    public static final Comparator<Row> EMPTY = (t1, t2) -> {
        throw new UnsupportedOperationException();
    };
    
    /**
     * Convert sort specifications to <code>Comparator</code>.
     * @param collation sort specifications
     * @return <code>Comparator</code>
     */
    public static Comparator<Row> convertCollationToRowComparator(final RelCollation collation) {
        if (collation == null) {
            return EMPTY;
        }
        List<RelFieldCollation> fieldCollations = collation.getFieldCollations();
        Iterable<Comparator<Row>> comparators = Iterables.transform(fieldCollations, fieldCollation -> comparator(fieldCollation));
        return Ordering.compound(comparators);
    }
    
    private static Comparator<Row> comparator(final RelFieldCollation fieldCollation) {
        int nullComparison = fieldCollation.nullDirection.nullComparison;
        int fieldIdx = fieldCollation.getFieldIndex();
        if (fieldCollation.direction == Direction.ASCENDING) {
            return (o1, o2) -> comparator(o1, o2, fieldIdx, nullComparison);
        } else {
            return (o1, o2) -> comparator(o2, o1, fieldIdx, nullComparison);
        }
    }
    
    private static int comparator(final Row r1, final Row r2, final int fieldIdx, final int nullComparison) {
        Comparable<?> c1 = r1.getColumnValue(fieldIdx + 1);
        Comparable<?> c2 = r2.getColumnValue(fieldIdx + 1);
        return RelFieldCollation.compare(c1, c2, nullComparison);
    }
}
