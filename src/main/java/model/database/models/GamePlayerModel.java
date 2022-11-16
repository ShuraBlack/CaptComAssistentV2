package model.database.models;

public class GamePlayerModel {

    private final String userid;

    private final boolean daily;
    private final int dailyBonus;
    private final String booster;

    private final long money;

    private final String selectDeck;
    private final String avaiableDecks;

    public GamePlayerModel(
            String userid,
            boolean daily,
            String booster,
            int dailyBonus,
            long money,
            String selectDeck,
            String avaiableDecks) {

        this.userid = userid;
        this.daily = daily;
        this.dailyBonus = dailyBonus;
        this.booster = booster;
        this.money = money;
        this.selectDeck = selectDeck;
        this.avaiableDecks = avaiableDecks;
    }

    public int getDailyBonus() {
        return dailyBonus;
    }

    public String getUserid() {
        return userid;
    }

    public boolean getDaily() {
        return daily;
    }

    public long getMoney() {
        return money;
    }

    public String getBooster() {
        return booster;
    }

    public String getSelectDeck() {
        return selectDeck;
    }

    public String getAvaiableDecks() {
        return avaiableDecks;
    }
}
