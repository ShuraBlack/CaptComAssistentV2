package model.database.models;

public class GamePlayerFullModel extends GamePlayerModel {

    private final double rating_blackjack;
    private final double rating_chest;

    public GamePlayerFullModel(String userid,
                               boolean daily,
                               String booster,
                               int dailyBonus,
                               long money,
                               double rating_blackjack,
                               double rating_chest,
                               String selectDeck,
                               String avaiableDecks) {
        super(userid, daily, booster, dailyBonus, money, selectDeck, avaiableDecks);
        this.rating_blackjack = rating_blackjack;
        this.rating_chest = rating_chest;
    }

    public double getRating_blackjack() {
        return rating_blackjack;
    }

    public double getRating_chest() {
        return rating_chest;
    }
}
