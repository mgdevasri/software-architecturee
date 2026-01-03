package com.example.bench;

import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;
//import all the annotation used in the  JMH 
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Thread)
public class OptimizerDefeatBenchmark {

    //  OPTIMIZED STATIC PATH is some of the method call this method means.. the JVM use the INLINE,Devirtualization
    public static class StaticCalc {
        public static int compute(int x) {
            return x + 10;
        }
    }

    @Benchmark  
    public int staticCall() {
        return StaticCalc.compute(5);
    }

    //TRUE VIRTUAL DISPATCH
    public interface Calculator {
        int compute(int x);
    }

    public static class AddCalc implements Calculator {
        public int compute(int x) {
            return x + 10;
        }
    }

    public static class MulCalc implements Calculator {
        public int compute(int x) {
            return x * 10;
        }
    }

    private final Calculator[] calcs = {
            new AddCalc(),
            new MulCalc()
    };

    private int index = 0;

    @Benchmark
    public int virtualDispatch() {
        Calculator c = calcs[index++ & 1];
        return c.compute(5);
    }

    // REFLECTION 
    private Method reflectMethod;
    private final Calculator reflectTarget = new AddCalc();

    @Setup
    public void setup() throws Exception {
        reflectMethod =
                reflectTarget.getClass().getMethod("compute", int.class);
    }

    @Benchmark //used to measure the cost of the method accurately
    public int reflectionCall() throws Exception {
        return (int) reflectMethod.invoke(reflectTarget, 5);
    }
}
