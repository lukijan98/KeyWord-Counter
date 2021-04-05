package FileScanner;

public class CrawlerObject {
    private boolean poison;
    private String path;

    public CrawlerObject(String path) {
        this.path = path;
        this.poison = false;
    }

    public CrawlerObject() {
        this.poison = true;
    }

    public boolean isPoison() {
        return poison;
    }

    public String getPath() {
        return path;
    }
}
