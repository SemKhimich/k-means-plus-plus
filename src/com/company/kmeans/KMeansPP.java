package com.company.kmeans;

import java.util.*;
import java.util.List;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class KMeansPP {
    private static final DistanceMetric euclideanMetric = (pointFirst, pointSecond) -> {
        if (pointFirst.dimension() != pointSecond.dimension()) {
            throw new RuntimeException("You can't calculate the distance between points " +
                    "with different dimensions");
        }
        return Math.sqrt(IntStream.range(0, pointFirst.dimension()).parallel()
                .mapToDouble(index ->
                        Math.pow(pointFirst.getCoordinates()[index] -
                                pointSecond.getCoordinates()[index], 2)).sum());
    };

    private static final ClusterCenterCalculator meanCenterCalculator = points -> {
        if (points.isEmpty()) {
            throw new RuntimeException("Must be at least one point in the cluster");
        }
        return new Point(
                Arrays.stream(
                        points.stream().reduce(
                                new Point(new double[points.get(0).dimension()]),
                                Point::addPoint).getCoordinates()).parallel()
                        .map(coordinate -> coordinate / points.size()).toArray());
    };

    private static boolean isPointsOfDifferentDimensions(List<Point> points) {
        if (points.isEmpty()) {
            return false;
        }
        return points.parallelStream()
                .anyMatch(point -> point.dimension() != points.get(0).dimension());
    }

    public static List<Integer> kMeansPP(List<Point> points, int clustersNum) {
        return kMeansPP(points, clustersNum, new Random());
    }

    public static List<Integer> kMeansPP(List<Point> points, int clustersNum, long seed) {
        return kMeansPP(points, clustersNum, new Random(seed));
    }

    public static List<Integer> kMeansPP(List<Point> points, int clustersNum, Random random) {
        if (clustersNum < 2) {
            throw new RuntimeException("Number of clusters must be at least 2");
        }
        if (clustersNum > points.size()) {
            throw new RuntimeException("Number of clusters mustn't be exceed the number of points");
        }
        if (isPointsOfDifferentDimensions(points)) {
            throw new RuntimeException("Points must be of the same dimension");
        }
        List<Point> centers = calculateClustersCenters(points, clustersNum, random);
        return kMeans(points, centers);
    }

    private static List<Point> calculateClustersCenters(List<Point> points, int clustersNum,
                                                        Random random) {
        if (clustersNum < 2) {
            throw new RuntimeException("Number of clusters must be at least 2");
        }
        if (clustersNum > points.size()) {
            throw new RuntimeException("Number of clusters mustn't be exceed the number of points");
        }
        if (isPointsOfDifferentDimensions(points)) {
            throw new RuntimeException("Points must be of the same dimension");
        }
        List<Point> centers = new ArrayList<>();
        centers.add(points.get(random.nextInt(points.size())));
        IntStream.range(0, clustersNum - 1).forEach(index -> {
            List<Double> cumulativeSumOfSquaredDistances = points.parallelStream()
                    .map(point -> Math.pow(euclideanMetric.distance(
                            centers.get(nearestCenterIndex(centers, point)),
                            point), 2))
                    .collect(Collector.of(
                            ArrayList<Double>::new,
                            (a, b) -> a.add(a.isEmpty() ? b : b + a.get(a.size() - 1)),
                            (a, b) -> {
                                a.addAll(b.parallelStream().map(e -> e + a.get(a.size() - 1))
                                        .collect(Collectors.toList()));
                                return a;
                            }
                    ));
            double randomValue = random.nextDouble() *
                    cumulativeSumOfSquaredDistances.get(cumulativeSumOfSquaredDistances.size() - 1);
            int newCenterIndex = -Collections.binarySearch(
                    cumulativeSumOfSquaredDistances, randomValue) - 1;
            centers.add(points.get(newCenterIndex));
        });
        return centers;
    }

    private static List<Integer> kMeans(List<Point> points, List<Point> centers) {
        boolean isStopIterations;
        do {
            Map<Integer, List<Point>> clusterNumberPointsMap = points.parallelStream()
                    .collect(Collectors.groupingBy(point -> nearestCenterIndex(centers, point)));
            List<Point> newCenters = IntStream.range(0, centers.size()).parallel()
                    .mapToObj(numCluster -> meanCenterCalculator.calculateCenter(clusterNumberPointsMap.get(numCluster)))
                    .collect(Collectors.toList());
            isStopIterations = IntStream.range(0, centers.size()).parallel()
                    .allMatch(index -> newCenters.get(index).equals(centers.get(index)));
            IntStream.range(0, centers.size()).parallel()
                    .forEach(index -> centers.set(index, newCenters.get(index)));
        } while (!isStopIterations);
        return points.parallelStream().map(point -> nearestCenterIndex(centers, point))
                .collect(Collectors.toList());
    }

    private static int nearestCenterIndex(List<Point> centers, Point point) {
        if (centers.isEmpty()) {
            throw new RuntimeException("List of centers mustn't be empty");
        }
        if (isPointsOfDifferentDimensions(centers)
                || centers.get(0).dimension() != point.dimension()) {
            throw new RuntimeException("Points must be of the same dimension");
        }
        return IntStream.range(0, centers.size()).parallel()
                .mapToObj(index -> new Pair<>(euclideanMetric.distance(centers.get(index), point), index))
                .min(Comparator.comparingDouble(Pair::getFirst)).get().getSecond();
    }
}
