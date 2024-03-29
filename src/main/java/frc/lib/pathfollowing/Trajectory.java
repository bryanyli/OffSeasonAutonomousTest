package frc.lib.pathfollowing;

import java.util.ArrayList;
import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.IOException;

/**
 * Designed only to work with PathPlanner paths with the output set as p,v,a,h
 */
public class Trajectory {
    public int currentIndex = 0;
    public ArrayList<PathPoint> points;

    public class PathPoint {
        public double position;
        public double velocity;
        public double acceleration;
        public double heading;

        PathPoint(double pos, double vel, double acc, double hea) {
            position = pos;
            velocity = vel;
            acceleration = acc;
            heading = hea;
        }

        Double[] toArray() {
            return new Double[] {position, velocity, acceleration, heading};
        }
    }

    public Trajectory(String filePath) throws IOException {
        this(new File(filePath));
    }

    public Trajectory(File file) throws IOException {
        BufferedReader csvReader = new BufferedReader(new FileReader(file));
        String row;
        points = new ArrayList<PathPoint>();
        while ((row = csvReader.readLine()) != null) {
            String[] data = row.split(",");
            double pos = Double.parseDouble(data[0]);
            double vel = Double.parseDouble(data[1]);
            double acc = Double.parseDouble(data[2]);
            double hea = Double.parseDouble(data[3]);
            
            points.add(new PathPoint(pos, vel, acc, hea));
        }
        csvReader.close();
        currentIndex = 0;
    }

    public boolean hasNext() {
        return currentIndex >= points.size();
    }

    public PathPoint next() {
<<<<<<< HEAD:src/main/java/frc/lib/pathfollowing/Trajectory.java
=======
        if (!hasNext()) {
            return null;
        }
>>>>>>> a4683c83daa2eda56a4a18c8324d709ecb9aabd2:src/main/java/frc/robot/types/Trajectory.java
        return points.get(currentIndex++);
    }

    public Double[] nextValues() {
        return next().toArray();
    }

    public double getTotalTime() {
        return points.size() * .2;
    }

    public double getStartHeading() {
        return points.get(0).heading;
    }
}
