package com.company.kmeans;

interface DistanceMetric {
    double distance(Point pointFirst, Point pointSecond);
}
