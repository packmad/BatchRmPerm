package it.unige.dibris.batchrmperm.domain.result;


public class SpeedPoint implements Comparable<SpeedPoint> {
    double nanosec;
    double fileSize;

    public SpeedPoint(double nanosec, double fileSize) {
        this.nanosec = nanosec;
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
        return String.format("%f %f", fileSize / 1048576.0, nanosec / 1000000000.0);
    }
}
