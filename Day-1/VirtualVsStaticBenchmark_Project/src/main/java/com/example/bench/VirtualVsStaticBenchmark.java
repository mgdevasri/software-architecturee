package com.example.bench;

import java.lang.reflect.Method;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import org.openjdk.jmh.annotations.*;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Thread)
public class OptimizerDefeatBenchmark {

    // OPTIMIZED STATIC PATH (INLINEABLE)
    static class StaticCalculator {
        static int compute(int x) {
            return x + 10;
        }
    }

    @Benchmark
    public int staticCall() {
        // JVM can inline this completely
        return StaticCalculator.compute(5);
    }

    // OPTIMIZER-DEFEATED: TRUE VIRTUAL DISPATCH
    interface Calculator {
        int compute(int x);
    }

    static class AddCalculator implements Calculator {
        public int compute(int x) {
            return x + 10;
        }
    }

    static class MulCalculator implements Calculator {
        public int compute(int x) {
            return x * 10;
        }
    }

    // Multiple implementations → polymorphic
    private final Calculator[] calculators = {
            new AddCalculator(),
            new MulCalculator()
    };

    private final Random random = new Random();

    @Benchmark
    public int virtualDispatch() {
        // Runtime selection defeats devirtualization
        Calculator calc = calculators[random.nextInt(calculators.length)];
        return calc.compute(5);
    }

    // OPTIMIZER KILLER: REFLECTION
    private Method reflectMethod;
    private final Calculator reflectTarget = new AddCalculator();

    @Setup
    public void setup() throws Exception {
        reflectMethod = reflectTarget
                .getClass()
                .getMethod("compute", int.class);
    }

    @Benchmark
    public int reflectionCall() throws Exception {
        // JVM cannot inline or optimize this
        return (int) reflectMethod.invoke(reflectTarget, 5);
    }
}
