package it.unige.dibris.batchrmperm.domain.comparable;


public class SpeedPoint implements Comparable<SpeedPoint> {
    double seconds;
    double fileSize;

    public SpeedPoint(double seconds, double fileSize) {
        this.seconds = seconds;
        this.fileSize = fileSize;
    }


    @Override
    public int compareTo(SpeedPoint sp) {
        return Double.compare(this.fileSize, sp.getFileSize());
    }


    public double getFileSize() {
        return fileSize;
    }

    @Override
    public String toString() {
        return String.format("%d %f", (long) fileSize, seconds);
    }
}
