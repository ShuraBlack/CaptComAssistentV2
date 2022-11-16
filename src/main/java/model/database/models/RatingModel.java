package model.database.models;

public class RatingModel {

    private final String userid;
    private final double rating_blackjack;
    private final double rating_chest;

    public RatingModel(String userid, double rating_blackjack, double rating_chest) {
        this.userid = userid;
        this.rating_blackjack = rating_blackjack;
        this.rating_chest = rating_chest;
    }

    public String getUserid() {
        return userid;
    }

    public double getRating_blackjack() {
        return rating_blackjack;
    }

    public double getRating_chest() {
        return rating_chest;
    }
}
