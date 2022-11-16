package model.command;

import model.command.type.ButtonInteraction;
import model.command.type.ServerCommand;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import util.ServerUtil;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

public class Subscription implements ServerCommand, ButtonInteraction {

    public static final Map<String, Integer> INTERACTIONS = new HashMap<>();

    @Override
    public void performTextChannelCommand(Member member, TextChannel channel, String message) {
        if (!member.hasPermission(Permission.ADMINISTRATOR)) {
            return;
        }

        if (!ServerUtil.isChannelId(channel, "subscription")) {
            return;
        }

        if (!message.equals("!sub")) {
            return;
        }

        createMessage(channel);
    }

    @Override
    public void performTextChannelReaction(Member member, TextChannel channel, MessageReaction reaction) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void performPrivateChannelCommand(User user, String message) {
        throw new UnsupportedOperationException();
    }

    private void sendFeedback(ButtonInteractionEvent event, String description, int color, boolean ephemeral) {
        Member m = event.getMember();
        assert m != null;
        if (INTERACTIONS.get(m.getId()) >= 10) {
            description += "\n\nSperre: Du hast die maximale Menge an Anfragen pro Tag überschritten!\n" +
                    "Versuch es morgen wieder";
            color = ServerUtil.RED;
            ephemeral = false;
        }
        EmbedBuilder accept = new EmbedBuilder()
                .setAuthor(m.getEffectiveName(), m.getEffectiveAvatarUrl(), m.getEffectiveAvatarUrl())
                .setColor(color)
                .setDescription(description)
                .setFooter("Anfragen - (" + INTERACTIONS.get(m.getId()) + "/10)");
        event.replyEmbeds(accept.build()).setEphemeral(ephemeral).queue();
    }

    private void createMessage (TextChannel channel) {
        final Emoji league = channel.getGuild().getEmojiById("804426711479484417");
        final Emoji warframe = channel.getGuild().getEmojiById("804427054015447062");
        final Emoji moon = channel.getGuild().getEmojiById("804427189969747999");
        final Emoji mc = channel.getGuild().getEmojiById("815238918496583710");
        final Emoji sati = channel.getGuild().getEmojiById("815238883620159548");
        final Emoji casino = channel.getGuild().getEmojiById("845057339145191434");
        final String ex = "✖️";

        assert league != null;
        assert warframe != null;
        assert mc != null;
        assert sati != null;
        EmbedBuilder eb = new EmbedBuilder()
                .setTitle("News Channel - Abonnieren")
                .setDescription("Über diese Nachricht kann der Nutzer bestimmen, welchen Subscription Channel er abonnieren/sehen möchte")
                .addField("","Verfügbare Channel:",false)
                .addField(league.getFormatted() + " League of Legends,","Updates, game changes usw. von dem eigenen League Discord Server",false)
                .addField(warframe.getFormatted() + " Warframe,","Updates, game changes usw. von dem eigenen Warframe Discord Server",false)
                .addField(mc.getFormatted() + " Minecraft,","Updates, game changes usw. von dem eigenen Minecraft Discord Server",false)
                .addField(sati.getFormatted() + " Satisfactory,","Updates, game changes usw. von dem eigenen Satisfactory Discord Server",false)
                .addField("❌ Entfernen,","Entfernt alle abonnierten Channel von dir",false);

        MessageCreateBuilder messageBuilder = new MessageCreateBuilder()
                .addEmbeds(eb.build())
                .setComponents(ActionRow.of(
                        Button.secondary("sub league", Emoji.fromUnicode("<:league:804426711479484417>")),
                        Button.secondary("sub warframe", Emoji.fromUnicode("<:warframe:804427054015447062>")),
                        Button.secondary("sub minecraft", Emoji.fromUnicode("<:mc:815238918496583710>")),
                        Button.secondary("sub satisfactory", Emoji.fromUnicode("<:sati:815238883620159548>")),
                        Button.danger("sub delete_news", Emoji.fromUnicode(ex))
                ));
        channel.sendMessage(messageBuilder.build()).queue();

        assert moon != null;
        assert casino != null;
        eb = new EmbedBuilder()
                .setTitle("Partner & Specials - Abonnieren")
                .addField(moon.getFormatted() + " Moonstruck,","Information über den Streamer, sowie Ankündigungen für den Stream",false)
                .addField(casino.getFormatted() + " Games,","Schaltet alle Server privaten Games Channel frei\n" +
                        "```diff\n- Du benötigst ebenfalls mindestens den Member Rang und bestätigst damit auch das du über 18 bist\n```",false)
                .addField("❌ Entfernen,","Entfernt alle abonnierten Partner und Specials von dir",false)
                .setFooter("Das Missbrauchen der Funktion führt zu einem Ausschluss (10 Anfrage pro Tag/Person)");

        messageBuilder.clear().addEmbeds(eb.build())
                .setComponents(ActionRow.of(
                        Button.secondary("sub moon", Emoji.fromUnicode("<:moon:804427189969747999>")),
                        Button.secondary("sub casino", Emoji.fromUnicode("<:casino:845057339145191434>")),
                        Button.danger("sub delete_special", Emoji.fromUnicode(ex))
                ));
        channel.sendMessage(messageBuilder.build()).queue();

    }

    @Override
    public void performButtonInteraction(Member m, TextChannel channel, String customID, ButtonInteractionEvent event) {

        if (INTERACTIONS.containsKey(m.getId()) && INTERACTIONS.get(m.getId()) >= 10) {
            return;
        }

        if (INTERACTIONS.containsKey(m.getId())) {
            INTERACTIONS.replace(m.getId(), INTERACTIONS.get(m.getId())+1);
        } else {
            INTERACTIONS.put(m.getId(),1);
        }

        Role leaguerole = channel.getGuild().getRoleById("804433890143895552");
        Role warframerole = channel.getGuild().getRoleById("804433895202226197");
        Role moonrole = channel.getGuild().getRoleById("804433899136221254");
        Role mcrole = channel.getGuild().getRoleById("815012063013109790");
        Role satirole = channel.getGuild().getRoleById("815011932934504449");
        Role casinorole = channel.getGuild().getRoleById("845055075947905043");

        switch (customID) {
            case "league":
                assert leaguerole != null;
                channel.getGuild().addRoleToMember(UserSnowflake.fromId(m.getId()),leaguerole).queue();
                sendFeedback(event, "Du hast erfolgreich den **League of Legends** Channel abonniert!",ServerUtil.GREEN,true);
                break;
            case "warframe":
                assert warframerole != null;
                channel.getGuild().addRoleToMember(User.fromId(m.getId()),warframerole).queue();
                sendFeedback(event, "Du hast erfolgreich den **Warframe** Channel abonniert!",ServerUtil.GREEN,true);
                break;
            case "minecraft":
                assert mcrole != null;
                channel.getGuild().addRoleToMember(User.fromId(m.getId()), mcrole).queue();
                sendFeedback(event, "Du hast erfolgreich den **Minecraft** Channel abonniert!",ServerUtil.GREEN,true);
                break;
            case "satisfactory":
                assert satirole != null;
                channel.getGuild().addRoleToMember(User.fromId(m.getId()), satirole).queue();
                sendFeedback(event, "Du hast erfolgreich den **Satisfactory** Channel abonniert!",ServerUtil.GREEN,true);
                break;
            case "moon":
                assert moonrole != null;
                channel.getGuild().addRoleToMember(User.fromId(m.getId()), moonrole).queue();
                sendFeedback(event, "Du hast erfolgreich den **Moonstruck** Channel abonniert!",ServerUtil.GREEN,true);
                break;
            case "casino":
                if (m.getRoles().stream().map(Role::getName).noneMatch(name -> name.equals("Member")
                        || name.equals("Veteran") || name.equals("Moderator"))) {
                    sendFeedback(event, "Du benötigst mindestens den **Member** Rang für diese Aktion!",ServerUtil.RED,true);
                    return;
                }
                assert casinorole != null;
                channel.getGuild().addRoleToMember(User.fromId(m.getId()), casinorole).queue();
                sendFeedback(event, "Du hast erfolgreich die **Game (Casino)** Channels abonniert!",ServerUtil.GREEN,true);
                break;
            case "delete_news":
                List<Role> rNews = new ArrayList<>();
                rNews.add(leaguerole);
                rNews.add(warframerole);
                rNews.add(mcrole);
                rNews.add(satirole);

                channel.getGuild().modifyMemberRoles(m, new ArrayList<>(), rNews).queue();
                sendFeedback(event, "Alle deine Channel Abonnements wurden entfernt!",ServerUtil.GREEN,true);
                break;
            case "delete_special":
                List<Role> rSpecials = new ArrayList<>();
                rSpecials.add(moonrole);
                rSpecials.add(casinorole);

                channel.getGuild().modifyMemberRoles(m, new ArrayList<>(), rSpecials).queue();
                sendFeedback(event, "Alle deine Partner & Special Abonnements wurden entfernt!",ServerUtil.GREEN,true);
                break;
        }
    }
}
