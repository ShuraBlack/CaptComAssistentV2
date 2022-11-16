package model.database.models;

public class RulerModel {

    private final String userid;
    private final boolean request;
    private final String role;

    public RulerModel(String userid, boolean request, String role) {
        this.userid = userid;
        this.request = request;
        this.role = role;
    }

    public String getUserid() {
        return userid;
    }

    public boolean isRequest() {
        return request;
    }

    public String getRole() {
        return role;
    }
}
