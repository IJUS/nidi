package net.ijus.nidi;

import net.ijus.nidi.instantiation.InstanceGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;

import static java.lang.System.currentTimeMillis;

/**
 * Created by pfried on 6/18/14.
 */
public class ContextTestUtils {
    private static final Logger log = LoggerFactory.getLogger(ContextTestUtils.class);


    public static void clearContextHolder() {
        log.info("Resetting the ContextHolder");
        ContextHolder.setContext(null);
    }

    /**
     * Executes the ContextConfig and returns the execution time in milliseconds. This is provided so that
     * Context configuration can be benchmarked easily.
     * @param config
     * @return
     */
    public static long timeConfiguration(ContextConfig config) {
        return ContextTestUtils.time(config, currentTimeMillis());
    }

    /**
     * Executes the InstanceGenerator and returns the execution time in milliseconds. This is provided so that
     * InstanceGeneration can be easily benchmarked.
     * @param gen any instance generator to run.
     * @return
     */
    public static long timeInstanceGeneration(InstanceGenerator gen){
        long start = currentTimeMillis();
        gen.createNewInstance();
        return currentTimeMillis() - start;
    }

    /**
     * performs a simple benchmark on an instance generator. Returns all of the execution times as a long[].
     * warmup iterations are logged, but not included in the returned times
     *
     * @param gen
     * @param numExecutions the number of iterations to use for the test
     * @param numWarmups the number of warmup iterations. these will be logged, but not returned. can be 0.
     * @return each of the execution times, in order from first to last.
     */
    public static long[] benchmarkGenerator(InstanceGenerator gen, int numExecutions, int numWarmups) {
        if (numWarmups > 0) {
            log.info("Starting Benchmark of InstanceGenerator: Warming up with {} iterations", numWarmups);
            long warmupStart = currentTimeMillis();
            long[] warmups = doBenchmark(gen, numWarmups);
            long warmupEnd = currentTimeMillis();
            long avg = (warmupEnd - warmupStart) / numWarmups;
            log.info("Finished warmup iterations and averaged {}ms", avg);
        }


        log.info("Starting Benchmark at {}", System.currentTimeMillis());
        long start = currentTimeMillis();
        long[] times = doBenchmark(gen, numExecutions);
        long total = currentTimeMillis() - start;
        long avg = total / numExecutions;
        log.info("Finished Benchmark in {}ms, Average time was {}ms", total, avg);
        return times;
    }

    public static long[] doBenchmark(InstanceGenerator gen, int iterations) {
        long[] times = new long[iterations];
        for (int i = 0; i < iterations; i++) {
            long start = currentTimeMillis();
            gen.createNewInstance();
            times[i] = currentTimeMillis() - start;
        }
        return times;
    }

    private static long time(ContextConfig config, long startTime) {
        Configuration.configureNew(config);
        return System.currentTimeMillis() - startTime;
    }


}
