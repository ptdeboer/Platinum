package nl.esciencecenter.ptk.util;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class CollectionUtil {

    public static boolean isEmpty(Collection<?> collection) {
        return ((collection == null) || (collection.size() <= 0));
    }

    public static boolean isNotEmpty(Collection<?> collection) {
        return ((collection != null) && (collection.size() > 0));
    }

    public static <T> T getFirstOrNull(List<T> list) {
        if ((list == null) || (list.size() <= 0)) {
            return null;
        }
        return list.get(0);
    }

    public static <T> List<T> singleton(T element) {
        return Arrays.asList(element);
    }

}
