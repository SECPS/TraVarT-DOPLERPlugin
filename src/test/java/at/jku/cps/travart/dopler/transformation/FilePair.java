package at.jku.cps.travart.dopler.transformation;

public class FilePair {

    private final String filename;
    private final String uvlData;
    private final String doplerData;

    public FilePair(String filename, String uvlData, String doplerData) {
        this.filename = filename;
        this.uvlData = uvlData;
        this.doplerData = doplerData;
    }

    public String getUvlData() {
        return uvlData;
    }

    public String getDoplerData() {
        return doplerData;
    }

    public String getFilename() {
        return filename;
    }
}
