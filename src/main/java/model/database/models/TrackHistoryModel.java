package model.database.models;

public class TrackHistoryModel {

    private final String title;
    private final String link;
    private final String date;

    public TrackHistoryModel(String title, String link, String date) {
        this.title = title;
        this.link = link;
        this.date = date;
    }

    public String getTitle() {
        return title;
    }

    public String getLink() {
        return link;
    }

    public String getDate() {
        return date;
    }
}
