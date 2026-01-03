package com.example.bench;

import java.util.concurrent.TimeUnit;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Thread)
public class VirtualVsStaticBenchmark {

    // ---------------- GOOD CODE (STATIC / INLINEABLE) ----------------
    static class StaticCalc {
        static int add(int a, int b) {
            return a + b;
        }
    }

    @Benchmark   //devirtualization + inlining
    public int staticCall() {
        return StaticCalc.add(10, 20);
    }

    // ---------------- BAD CODE (VIRTUAL / POLYMORPHIC) ----------------
    interface Calculator {
        int add(int a, int b);
    }

    static class VirtualCalc implements Calculator {  //JVM cannot know target at compile time
        @Override
        public int add(int a, int b) {
            return a + b;
        }
    }

    // Polymorphic reference
    private final Calculator calc = new VirtualCalc();

    @Benchmark 
    public int virtualCall() {
        return calc.add(10, 20);
    }
}
