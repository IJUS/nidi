package net.ijus.nidi.utils

import com.example.general.WithOptionalClass
import com.example.general.WithOptionalProperty
import com.example.impl.NamespacedLoggingService
import com.example.interfaces.LoggingService
import spock.lang.Specification

import java.lang.annotation.Annotation
import java.lang.reflect.Constructor

import static net.ijus.nidi.utils.ReflectionUtils.*;

/**
 * Created by pfried on 9/14/14.
 */
class ReflectionUtilsSpec extends Specification {

    void "constructor params with the @Bound annotation should be correctly identified"(){
        setup:
        Constructor constructor = NamespacedLoggingService.getConstructor(String)

        when:
        String[] result = getBoundAnnotatedParams(constructor.getParameterAnnotations())

        then:
        result.length == 1
        result[0] == 'stringProperty'

    }

    void "@Optional constructor parameters should not be counted if they dont't have a string value"(){
        setup: "use constructor with a single @Optional param that doesn't have a string value"
        Constructor constructor = WithOptionalClass.getConstructor(LoggingService)

        when:
        String[] result = getBoundAnnotatedParams(constructor.getParameterAnnotations())

        then: "the entry for that constructor parameter should be null"
        result.length == 1
        result[0] == null
    }

    void "optional constructor parameters with string values should be included in getBoundAnnotatedParams"() {
        setup:
        Annotation[][] annos = WithOptionalProperty.getConstructor(String).getParameterAnnotations()

        when:
        String[] result = getBoundAnnotatedParams(annos)

        then:
        result.length == 1
        result[0] == "optionalProperty"
    }

    void "optional constructor parameters should be correctly identified"() {
        setup:
        Annotation[][] anno1 = WithOptionalClass.getConstructor(LoggingService).getParameterAnnotations()
        Annotation[][] anno2 = WithOptionalProperty.getConstructor(String).getParameterAnnotations()

        Annotation[][] required = NamespacedLoggingService.getConstructor(String).getParameterAnnotations()

        expect:
        isParameterOptional(anno1, 0)
        isParameterOptional(anno2, 0)
        !isParameterOptional(required, 0)

    }

}
