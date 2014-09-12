package net.ijus.nidi.utils;

import java.util.Collection;
import java.util.Iterator;

/**
 * Utilities for dealing with collections
 */
public class CollectionUtils {

    public static String join(Collection collection, String separator) {
        StringBuilder sb = new StringBuilder();
        Iterator it = collection.iterator();
        while (it.hasNext()) {
            Object thing = it.next();
            sb.append(thing);
            if (it.hasNext()) {
                sb.append(separator);
            }
        }
        return sb.toString();
    }
}
