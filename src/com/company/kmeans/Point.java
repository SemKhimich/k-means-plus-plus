package com.company.kmeans;

import java.util.Arrays;
import java.util.stream.IntStream;

public class Point {
    private final double[] coordinates;

    public Point(double[] coordinates) {
        this.coordinates = coordinates;
    }

    public double[] getCoordinates() {
        return coordinates;
    }

    public int dimension() {
        return coordinates.length;
    }

    public Point addPoint(Point point) {
        if (dimension() != point.dimension()) {
            throw new RuntimeException("You can't calculate the sum of points with different dimensions");
        }
        IntStream.range(0, dimension()).parallel()
                .forEach(index -> coordinates[index] += point.getCoordinates()[index]);
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Point point = (Point) o;
        return Arrays.equals(coordinates, point.coordinates);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(coordinates);
    }

    @Override
    public String toString() {
        return Arrays.toString(coordinates);
    }
}
