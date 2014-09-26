package net.ijus.nidi.utils;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import static net.ijus.nidi.utils.CollectionUtils.join;

/**
 * utility methods for dealing with classes
 */
public class ClassUtils {

    public static String classNames(Class[] classes) {
        return classNames(Arrays.asList(classes));
    }

    public static String classNames(List<Class> classes) {
        List<String> names = new LinkedList<String>();
        for (Class c : classes) {
            names.add(c.getName());
        }
        return "[ " + join(names, ", ") + " ]";
    }


}
