package net.ijus.nidi.utils;

import net.ijus.nidi.Optional;
import net.ijus.nidi.Require;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;

/**
 * Created by pfried on 9/14/14.
 */
public class ReflectionUtils {

    /**
     * Determines if the constructor parameter at the given index bears the @Optional annotation
     * @param paramAnnotations the 2d array returned by Constructor.getParameterAnnotations()
     * @param paramIdx the index of the constructor parameter
     * @return true if there's an @Optional annotation, otherwise false
     */
    public static boolean isParameterOptional(Annotation[][] paramAnnotations, int paramIdx) {
        Annotation[] annos = paramAnnotations[paramIdx];

        for (int i = 0; i < annos.length; i++) {
            if (annos[i] instanceof Optional) {
                return true;
            }
        }
        return false;
    }


    /**
     * returns an array of string values for all the constructor params annotated with
     * <code>@Require(String)</code> or <code>@Optional(String)</code>
     * The array will always be the same length as the number of constructor params.
     * For the example constructor:
     * <code>MyClass(LoggingService logSvc, @Require("stringProperty") String someString){...</code>
     * the Array returned would look like: [null, 'stringProperty'] since the first param doesn't
     * have the @Require annotation.
     *
     * This method will only return the annotation *values* in the array. So, if there is an @Optional
     * annotation present that doesn't have a value, the array will contain a null.
     *
     * @param allAnnotations two dimensional array of Constructor Parameter Annotations returned by Constructor.getParameterAnnotations()
     * @return String[] of length equal to the number of constructor params, or a String[0] for a
     * zero-arg constructor
     */
    public static String[] getBoundAnnotatedParams(Annotation[][] allAnnotations) {

        String[] boundParams = new String[allAnnotations.length];

        for (int outer = 0; outer < allAnnotations.length; outer++) {
            Annotation[] paramAnnotations = allAnnotations[outer];
            for (int inner = 0; inner < paramAnnotations.length; inner++) {
                Annotation a = paramAnnotations[inner];
                if (a instanceof Require) {
                    boundParams[outer] = ((Require) a).value();
                } else if (a instanceof Optional && ((Optional) a).value().length() > 0) {
                    boundParams[outer] = ((Optional) a).value();
                }

            }

        }


        return boundParams;
    }

}
