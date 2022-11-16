package model.service.task;

import model.command.Game;
import model.database.models.GamePlayerModel;
import model.database.models.RatingModel;
import model.game.ChestRequest;
import model.game.blackjack.BlackJackGame;
import model.manager.DiscordBot;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import org.apache.logging.log4j.Logger;
import util.ConnectionUtil;
import util.ServerUtil;

import java.time.OffsetDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class GameDailyTask implements Runnable {

    private final Logger logger;

    public GameDailyTask(Logger logger) {
        this.logger = logger;
    }

    @Override
    public void run() {
        this.logger.info("Starting GameDailyTask CronJob ...");

        ConnectionUtil.executeSQL(ConnectionUtil.GAME_DAILY());

        ChestRequest.clearHistory();

        final String[] medals = {"\uD83E\uDD47","\uD83E\uDD48","\uD83E\uDD49","\uD83D\uDCA2"};

        Guild guild = DiscordBot.INSTANCE.getManager().getGuildById(ServerUtil.GUILD);
        List<GamePlayerModel> list = ConnectionUtil.executeSQL(ConnectionUtil.SELECT_ORDER_GAME_MONEY(), GamePlayerModel.class);
        Optional<Integer> player = ConnectionUtil.executeSingleSQL(ConnectionUtil.SELECT_GAME_COUNT(),Integer.class);
        EmbedBuilder leaderboard = new EmbedBuilder()
                .setThumbnail("https://s20.directupload.net/images/220619/4vo59odf.png")
                .setTitle("Bestenliste")
                .setFooter("Letzte Aktualisierung")
                .setTimestamp(OffsetDateTime.now());
        player.ifPresent(value -> leaderboard.setDescription("**Registrierte Spieler:** " + value));
        if (list.size() == 3) {
            StringBuilder s = new StringBuilder();
            for (int i = 0 ; i < 3 ; i++) {
                GamePlayerModel model = list.get(i);
                Member member = guild.retrieveMemberById(model.getUserid()).complete();
                s.append(medals[i]).append(" ").append("__").append(member.getUser().getAsTag()).append("__").append("\n")
                        .append(BlackJackGame.formatNumber(model.getMoney())).append(" <:gold_coin:886658702512361482>").append("\n\n");
            }
            leaderboard.addField("Meistes Geld",s.toString(),true);
        }
        list.clear();

        List<RatingModel> ratings = ConnectionUtil.executeSQL(ConnectionUtil.SELECT_RATING(), RatingModel.class);
        if (ratings.size() >= 3) {
            ratings.sort(Comparator.comparingDouble(RatingModel::getRating_blackjack).reversed());
            StringBuilder s = new StringBuilder();
            for (int i = 0 ; i < 3 ; i++) {
                RatingModel model = ratings.get(i);
                Member member = guild.retrieveMemberById(model.getUserid()).complete();
                s.append(medals[i]).append(" ").append("__").append(member.getUser().getAsTag()).append("__").append("\n")
                        .append(Game.pointsToRank((int)model.getRating_blackjack())).append(" ").append((int)model.getRating_blackjack()).append("\n\n");
            }
            leaderboard.addField("Blackjack",s.toString(),true);

            s.delete(0, s.length()-1);
            ratings.sort(Comparator.comparingDouble(RatingModel::getRating_chest).reversed());
            for (int i = 0 ; i < 3 ; i++) {
                RatingModel model = ratings.get(i);
                Member member = guild.retrieveMemberById(model.getUserid()).complete();
                s.append(medals[i]).append(" ").append("__").append(member.getUser().getAsTag()).append("__").append("\n")
                        .append(Game.pointsToRank((int)model.getRating_chest())).append(" ").append((int)model.getRating_chest()).append("\n\n");
            }
            leaderboard.addField("Chest Opening",s.toString(),true);

            ratings.clear();
        }

        guild.getTextChannelById(ServerUtil.getChannelID("hub")).editMessageEmbedsById(ServerUtil.getMessageID("game_leaderboard"), leaderboard.build()).queue();

        this.logger.info("Successfully finished GameDailyTask CronJob");
    }

}
