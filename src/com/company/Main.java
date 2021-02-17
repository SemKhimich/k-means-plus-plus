package com.company;

import com.company.kmeans.KMeansPP;
import com.company.kmeans.Point;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

public class Main {
    public static List<Point> readPoints(String filename) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(filename));
        List<Point> points = new ArrayList<>();
        String line = reader.readLine();
        while (line != null) {
            StringTokenizer tokenizer = new StringTokenizer(line);
            if (tokenizer.countTokens() != 2) {
                throw new RuntimeException();
            }
            points.add(new Point(
                    new double[]{Integer.parseInt(tokenizer.nextToken()),
                            Integer.parseInt(tokenizer.nextToken())}));
            line = reader.readLine();
        }
        return points;
    }

    public static void main(String[] args) {
        try {
            List<Point> points = readPoints("points.txt");
            List<Integer> res = KMeansPP.kMeansPP(points, 10);
            System.out.println(res.subList(0, 20));
        } catch (IOException exception) {
            System.out.print(exception.getMessage());
        }
    }
}
