package model.database.models;

public class SongIDModel {

    private final String list;
    private final String song;
    private final int id;

    public SongIDModel(String list, String song, int id) {
        this.list = list;
        this.song = song;
        this.id = id;
    }

    public String getList() {
        return list;
    }

    public String getSong() {
        return song;
    }

    public int getId() {
        return id;
    }
}
