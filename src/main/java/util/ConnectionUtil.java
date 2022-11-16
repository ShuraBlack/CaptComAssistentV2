package util;

import model.database.MySQLConnectionPool;
import model.database.models.*;
import model.game.ChestRequest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * @author ShuraBlack
 * @since 03-20-2022
 */
public class ConnectionUtil {

    public static MySQLConnectionPool CONNECTIONPOOL;

    private static final Logger LOGGER = LogManager.getLogger(ConnectionUtil.class);

    public static <T> Optional<T> executeSingleSQL(String sql, Class<T> cls) {
        Optional<T> rtn = Optional.empty();
        final String type = cls.getSimpleName();

        final Connection conn = CONNECTIONPOOL.getConnection();
        try {
            Statement statement = conn.createStatement();
            ResultSet rs = statement.executeQuery(sql);

            switch (type) {
                case "GamePlayerModel":
                    if (rs.next()) {
                        rtn = Optional.of(cls.cast(new GamePlayerModel(
                                rs.getString(1),
                                rs.getBoolean(2),
                                rs.getString(3),
                                rs.getInt(4),
                                rs.getLong(5),
                                rs.getString(6),
                                rs.getString(7)
                        )));
                    }
                    break;
                case "GamePlayerFullModel":
                    if (rs.next()) {
                        rtn = Optional.of(cls.cast(new GamePlayerFullModel(
                                rs.getString(1),
                                rs.getBoolean(2),
                                rs.getString(3),
                                rs.getInt(4),
                                rs.getLong(5),
                                rs.getDouble(8),
                                rs.getDouble(9),
                                rs.getString(6),
                                rs.getString(7)
                        )));
                    }
                    break;
                case "RatingModel":
                    if (rs.next()) {
                        rtn = Optional.of(cls.cast(new RatingModel(
                            rs.getString(1),
                            rs.getDouble(2),
                            rs.getDouble(3)
                        )));
                    }
                case "Integer":
                    if (rs.next()) {
                        rtn = Optional.of(cls.cast(rs.getInt(1)));
                    }
                    break;
                case "String":
                    if (rs.next()) {
                        rtn = Optional.of(cls.cast(rs.getString(1)));
                    }
                    break;
            }

        } catch (SQLException e) {
            LOGGER.error("Couldt create connection statement or read ResultSet",e);
        } finally {
            CONNECTIONPOOL.returnConnection(conn);
        }

        return rtn;
    }

    /**
     * Process SELECT SQL requests
     * @param sql SQL command in string format
     * @param cls Class type which will be returned
     * @param <T> Intern generic type for return
     * @return {@link List} with {@link ResultSet} as {@link T} model
     */
    public static <T> List<T> executeSQL(String sql, Class<T> cls) {
        final List<T> list = new ArrayList<>();
        final String type = cls.getSimpleName();

        final Connection conn = CONNECTIONPOOL.getConnection();
        try {
            Statement statement = conn.createStatement();
            ResultSet rs = statement.executeQuery(sql);

            // TODO: Put different model proccess in here
            switch (type) {
                case "RulerModel":
                    while (rs.next()) {
                        list.add(cls.cast(new RulerModel(
                                rs.getString(1),
                                rs.getBoolean(2),
                                rs.getString(3)
                        )));
                    }
                    break;
                case "PlaylistModel":
                    while (rs.next()) {
                        list.add(cls.cast(new PlaylistModel(
                                rs.getInt(1),
                                rs.getString(2),
                                rs.getString(3),
                                rs.getString(4)
                        )));
                    }
                    break;
                case "SongIDModel":
                    while (rs.next()) {
                        list.add(cls.cast(new SongIDModel(
                                rs.getString(1),
                                rs.getString(2),
                                rs.getInt(3)
                        )));
                    }
                    break;
                case "TrackHistoryModel":
                    while (rs.next()) {
                        list.add(cls.cast(new TrackHistoryModel(
                                rs.getString(1),
                                rs.getString(2),
                                rs.getString(3)
                        )));
                    }
                    break;
                case "GamePlayerModel":
                    while (rs.next()) {
                        list.add(cls.cast(new GamePlayerModel(
                                rs.getString(1),
                                rs.getBoolean(2),
                                rs.getString(3),
                                rs.getInt(4),
                                rs.getLong(5),
                                rs.getString(6),
                                rs.getString(7)
                        )));
                    }
                    break;
                case "GamePlayerFullModel":
                    while (rs.next()) {
                        list.add(cls.cast(new GamePlayerFullModel(
                                rs.getString(1),
                                rs.getBoolean(2),
                                rs.getString(3),
                                rs.getInt(4),
                                rs.getLong(5),
                                rs.getDouble(8),
                                rs.getDouble(9),
                                rs.getString(6),
                                rs.getString(7)
                        )));
                    }
                    break;
                case "RatingModel":
                    while (rs.next()) {
                        list.add(cls.cast(new RatingModel(
                                rs.getString(1),
                                rs.getDouble(2),
                                rs.getDouble(3)
                        )));
                    }
                    break;
                case "Integer":
                    while (rs.next()) {
                        list.add(cls.cast(rs.getInt(1)));
                    }
                    break;
                case "String":
                    while (rs.next()) {
                        list.add(cls.cast(rs.getString(1)));
                    }
                    break;
                case "ResultSet":
                    list.add(cls.cast(rs));
                    break;
            }

        } catch (SQLException e) {
            LOGGER.error("Couldt create connection statement or read ResultSet",e);
        } finally {
            CONNECTIONPOOL.returnConnection(conn);
        }

        return list;
    }

    /**
     * Proccess INSERT, UPDATE & DELETE SQL requests
     * @param sql SQL command in string format
     */
    public static void executeSQL(String sql) {
        final Connection conn = CONNECTIONPOOL.getConnection();
        try {
            Statement statement = conn.createStatement();
            statement.executeUpdate(sql);
        } catch (SQLException e) {
            LOGGER.error("Couldt create connection statement",e);
        } finally {
            CONNECTIONPOOL.returnConnection(conn);
        }
    }

    /*-- Prepared Statments PLAYLISTS --------------------------------------------------------------------------------*/

    public static String INSERT_SONG(String userid, String playlist, String song) {
        return String.format("INSERT INTO playlist VALUE (null,'%s','%s','%s');", userid, playlist, song);
    }

    public static String SELECT_COUNT_PLAYLIST(String userid) {
        return String.format("SELECT count(DISTINCT songlist) FROM playlist WHERE userid = '%s';", userid);
    }

    public static String SELECT_ALL_SONGS(String userid) {
        return String.format("SELECT * FROM playlist p WHERE p.userid = '%s';", userid);
    }

    public static String SELECT_SONGS_OF_PLAYLIST(String userid, String playlist) {
        return String.format("SELECT song FROM playlist WHERE userid = '%s' AND songlist = '%s';", userid, playlist);
    }

    public static String SELECT_PLAYLISTS(String userid) {
        return String.format("SELECT DISTINCT songlist FROM playlist WHERE userid = '%s';", userid);
    }

    public static String SELECT_SONG_PLAYLIST(String userid, String playlist) {
        return String.format("SELECT p.songlist,p.song,p.id FROM playlist p WHERE p.userid = '%s' AND p.songlist = '%s';", userid, playlist);
    }

    public static String DELETE_SONG(String userid, String id) {
        return String.format("DELETE FROM playlist WHERE userid = '%s' AND id = '%s';", userid, id);
    }

    public static String DELETE_ALL_SONGS(String userid) {
        return String.format("DELETE FROM playlist WHERE userid = '%s';", userid);
    }

    /*----------------------------------------------------------------------------------------------------------------*/

    /*-- Prepared Statments PLAYER -----------------------------------------------------------------------------------*/

    public static String INSERT_TITLE(String title, String link) {
        return String.format("INSERT INTO trackhistory VALUE('%s','%s',null);", title.replace("'",""), link);
    }

    public static String SELECT_TITLES(int amount) {
        return String.format("SELECT * FROM trackhistory ORDER BY date LIMIT %d;", amount);
    }

    public static String SELECT_ALL_TITLES() {
        return "SELECT * FROM trackhistory ORDER BY date;";
    }

    public static String DELETE_OLD_TRACKS() {
        return "DELETE FROM trackhistory WHERE DATEDIFF(CURRENT_DATE, date) > 14";
    }

    /*----------------------------------------------------------------------------------------------------------------*/

    /*-- Prepared Statments Ruler ------------------------------------------------------------------------------------*/

    public static String INSERT_RULER(String userid) {
        return String.format("INSERT INTO roles VALUE('%s',false,null);", userid);
    }

    public static String SELECT_RULER(String userid) {
        return String.format("SELECT * FROM roles WHERE userid = '%s';", userid);
    }

    public static String UPDATE_RULER(String userid, boolean request, String role) {
        return String.format("UPDATE roles SET request = %b WHERE userid = '%s',%s" + ";", request, userid, (role == null ? "null" : "'" + role + "'"));
    }

    /*----------------------------------------------------------------------------------------------------------------*/

    /*-- Prepared Statments Game -------------------------------------------------------------------------------------*/

    public static String INSERT_GAME(String userid) {
        return String.format("CALL INSERT_GAME('%s');", userid);
    }

    public static String SELECT_GAME(String userid) {
        return String.format("SELECT * FROM game WHERE userid = '%s';", userid);
    }

    public static String SELECT_GAME_FULL(String userid) {
        return String.format("SELECT * FROM (SELECT game.userid, game.daily, game.booster, game.daily_bonus, game.money" +
                ", game.select_deck, game.deck, rating.blackjack, rating.chest FROM game LEFT JOIN rating ON game.userid = rating.userid) AS sub WHERE sub.userid = '%s';", userid);
    }

    public static String UPDATE_GAME_DAILY(String userid, long value) {
        return String.format("UPDATE game SET daily = FALSE, money = money + %d WHERE userid = '%s';", value, userid);
    }

    public static String UPDATE_GAME_MONEY(String userid, long value) {
        return String.format("UPDATE game SET money = money + %d WHERE userid = '%s';", value, userid);
    }

    public static String GAME_DAILY() {
        return "CALL GAME_DAILY();";
    }

    public static String UPDATE_GAME_DAILY_RESET() {
        return "UPDATE game SET daily = TRUE WHERE daily = FALSE;";
    }

    public static String UPDATE_GAME_BOOST_RESET() {
        return "UPDATE game SET booster = '' WHERE booster NOT LIKE '';";
    }

    public static String UPDATE_GAME_CHEST_RATING() {
        return "UPDATE rating SET chest = chest - 2.0 WHERE chest > 500.0;";
    }

    public static String UPDATE_GAME_BLACKJACK(String userid, String rating, long value) {
        return String.format("UPDATE game, rating SET rating.blackjack = CONSTRAIN_RATING(%s), game.money = game.money + %d " +
                "WHERE game.userid = '%s' AND rating.userid = '%s';", rating, value, userid, userid);
    }

    public static String UPDATE_GAME_CHEST(String userid, String increase) {
        return String.format("UPDATE rating SET chest = chest + %s WHERE userid = '%s'", increase, userid);
    }

    public static String UPDATE_GAME_BOOST(String userid, String callerid, String booster) {
        return String.format("UPDATE game SET money = money + 500, booster = '%s' WHERE userid = '%s';", booster + callerid + ",", userid);
    }

    public static String SELECT_ORDER_GAME_MONEY() {
        return "SELECT * FROM game ORDER BY money DESC LIMIT 3;";
    }

    public static String SELECT_RATING() {
        return "SELECT * FROM rating;";
    }

    public static String UPDATE_GAME_DAILY_BONUS(String userid, int value) {
        return String.format("UPDATE game SET daily_bonus = daily_bonus + %d WHERE userid = '%s';", value, userid);
    }

    public static String UPDATE_GAME_DECKS(String userid, String decks) {
        return String.format("UPDATE game SET deck = '%s' WHERE userid = '%s';", decks, userid);
    }

    public static String UPDATE_GAME_SELECT_DECK(String userid, String deck) {
        return String.format("UPDATE game SET select_deck = '%s' WHERE userid = '%s';", deck, userid);
    }

    public static String UPDATE_GAME_CHEST(String userid, long money, String chestRating) {
        return String.format("UPDATE game, rating SET game.money = game.money + %d, rating.chest = CONSTRAIN_RATING(rating.chest + %s) " +
                "WHERE game.userid = '%s' AND rating.userid = '%s';", money, chestRating, userid, userid);
    }

    public static String UPDATE_GAME_CHEST(String userid, long money, String decks, String chestRating) {
        return String.format("UPDATE game, rating SET game.deck = %s, game.money = game.money + %d rating.chest = CONSTRAIN_RATING(rating.chest + %s) " +
                "WHERE game.userid = '%s' AND rating.userid = '%s';", decks, money, chestRating, userid, userid);
    }

    public static String UPDATE_GAME_CHEST(String userid, long money, long daily_bonus, String chestRating) {
        return String.format("UPDATE game, rating SET game.money = game.money + %d, game.daily_bonus = game.daily_bonus + %d" +
                ", rating.chest = CONSTRAIN_RATING(rating.chest + %s) WHERE game.userid = '%s' AND rating.userid = '%s';", money, daily_bonus, chestRating, userid, userid);
    }

    public static String SELECT_GAME_COUNT() {
        return "SELECT COUNT(*) FROM game;";
    }

    public static String UPDATE_GAME_BUNDLE(ChestRequest request) {
        final double chestAmount = ChestRequest.getRating(request);
        return String.format("UPDATE game, rating SET game.deck = '%s', game.money = game.money + %d, game.daily_bonus = game.daily_bonus + %d" +
                        ", rating.chest = CONSTRAIN_RATING(rating.chest + %s) WHERE game.userid = '%s' AND rating.userid = '%s';"
        , (request.getCreator().getAvaiableDecks()+request.getDecks()), request.getMoney(), request.getDaily(), String.valueOf(chestAmount).replace(",",".")
                , request.getCreator().getUserid(), request.getCreator().getUserid());
    }

    /*----------------------------------------------------------------------------------------------------------------*/
}
