package com.company.kmeans;

import java.util.List;

public interface ClusterCenterCalculator {
    Point calculateCenter(List<Point> points);
}
