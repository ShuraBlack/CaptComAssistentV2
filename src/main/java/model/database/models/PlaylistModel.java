package model.database.models;

public class PlaylistModel {

    private final int id;
    private final String userid;
    private final String songlist;
    private final String song;

    public PlaylistModel(int id, String userid, String songlist, String song) {
        this.id = id;
        this.userid = userid;
        this.songlist = songlist;
        this.song = song;
    }

    public int getId() {
        return id;
    }

    public String getUserid() {
        return userid;
    }

    public String getSonglist() {
        return songlist;
    }

    public String getSong() {
        return song;
    }
}
