package com.justbelieveinmyself.mocf;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class Gradient {

    public static Function<List<Double>, Double> rosenbrock = (List<Double> v) -> {
        double x = v.get(0);
        double y = v.get(1);
        return Math.pow(1 - x, 2) + 100 * Math.pow(y - x * x, 2);
    };

    public static Function<List<Double>, Double> rastrigin = (List<Double> v) -> {
        double A = 10;
        int n = v.size();
        return A * n + v.stream().mapToDouble(x -> x * x - A * Math.cos(2 * Math.PI * x)).sum();
    };

    public static Function<List<Double>, Double> rosenbrock3D = (List<Double> v) -> {
        double a = 1, b = 100;
        return Math.pow(a - v.get(0), 2) + b * Math.pow(v.get(1) - v.get(0) * v.get(0), 2) +
                Math.pow(a - v.get(1), 2) + b * Math.pow(v.get(2) - v.get(1) * v.get(1), 2);
    };

    public static List<List<Double>> points2D = new ArrayList<>();
    static {
        points2D.add(List.of(0.0, 0.0));
        points2D.add(List.of(1.0, 1.0));
        points2D.add(List.of(-1.0, 1.0));
        points2D.add(List.of(1.0, -1.0));
        points2D.add(List.of(-1.0, -1.0));
        points2D.add(List.of(-2.0, -3.0));
        points2D.add(List.of(3.0, -4.0));
        points2D.add(List.of(-7.0, 5.0));
        points2D.add(List.of(7.0, 3.0));
        points2D.add(List.of(-8.0, -3.0));
        points2D.add(List.of(-1.5, 1.5));
        points2D.add(List.of(1.0, -2.0));
        points2D.add(List.of(-2.0, 2.0));
    }

    public static List<List<Double>> points3D = new ArrayList<>();
    static {
        points3D.add(List.of(0.0, 0.0, 0.0));
        points3D.add(List.of(1.0, 1.0, 1.0));
        points3D.add(List.of(-1.0, 1.0, 1.0));
        points3D.add(List.of(1.0, -1.0, -1.0));
        points3D.add(List.of(-1.0, -1.0, 1.0));
        points3D.add(List.of(-2.0, -3.0, 2.0));
        points3D.add(List.of(3.0, -4.0, -3.0));
        points3D.add(List.of(-7.0, 5.0, 2.0));
        points3D.add(List.of(7.0, 3.0, -4.0));
        points3D.add(List.of(-8.0, -3.0, -5.0));
        points3D.add(List.of(-1.5, 1.5, 4.0));
        points3D.add(List.of(1.0, -2.0, 4.0));
        points3D.add(List.of(-2.0, 2.0, 1.0));
    }

    public static List<List<Double>> minimize(Function<List<Double>, Double> f, List<List<Double>> starts, double learningRate, double tolerance) {
        List<List<Double>> localMinima = new ArrayList<>();

        for (List<Double> start : starts) {
            boolean isUnique = true;
            List<Double> x = new ArrayList<>(start);
            List<Double> previous;
            do {
                previous = new ArrayList<>(x);
                List<Double> grad = calculateGradient(f, x);
                for (int i = 0; i < x.size(); i++)
                    x.set(i, x.get(i) - learningRate * grad.get(i));
            } while (normalizeVector(subtractVector(x, previous)) > tolerance);

            for (List<Double> localMinimum : localMinima) {
                int n = 0;
                for (int j = 0; j < localMinimum.size(); j++) {
                    if (localMinimum.get(j).equals(x.get(j))) {
                        n++;
                    }
                }
                if (n == localMinimum.size() || x.get(0) > 100 || x.get(1) > 100) {
                    isUnique = false;
                }
            }
            if (isUnique) {
                localMinima.add(new ArrayList<>(x));
            }
        }

        return localMinima;
    }

    private static List<Double> calculateGradient(Function<List<Double>, Double> f, List<Double> x) {
        List<Double> grad = new ArrayList<>(new ArrayList<>(x.size()));
        double fx = f.apply(x);
        double h = 1e-5;
        for (int i = 0; i < x.size(); i++) {
            List<Double> xh = new ArrayList<>(x);
            xh.set(i, x.get(i) + h);
            grad.add
                    ((f.apply(xh) - fx) / h);
        }
        return grad;
    }

    private static List<Double> subtractVector(List<Double> a, List<Double> b) {
        List<Double> result = new ArrayList<>();
        for (int i = 0; i < a.size(); i++) {
            result.add(a.get(i) - b.get(i));
        }
        return result;
    }

    private static double normalizeVector(List<Double> a) {
        double sum = 0;
        for (double ai : a) {
            sum += ai * ai;
        }
        return Math.sqrt(sum);
    }
}
