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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Ruler implements ServerCommand, ButtonInteraction {

    public static final Map<String, Integer> INTERACTIONS = new HashMap<>();

    @Override
    public void performTextChannelCommand(Member member, TextChannel channel, String message) {
        if (!member.hasPermission(Permission.ADMINISTRATOR)) {
            return;
        }

        if (!ServerUtil.isChannelId(channel, "roles")) {
            return;
        }

        if (!message.equals("!ruler")) {
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

    private void createMessage(TextChannel channel) {
        EmbedBuilder eb = new EmbedBuilder()
            .setTitle("Rollenverteilung - Nutzerhilfe")
            .setDescription("Über diese Nachricht ist jeder Nutzer in der Lage, eine Standard-Rolle zu erhalten." +
                    "\nNutzer mit höherem Rang können sich nicht über diese Nachricht degradieren.")
            .addField("","**Auswahl:**",false)
            .addField("<:bronze:1007022490037518526> Guest,","Für jeden Nutzer der nur gelegentlich/einmalig auf dem" +
                "Discord Server ist. Keine Besonderen Rechte",true)
            .addField("<:silber:1007022487411896320> Member,","Für bekannte Mitspieler und Freunde die neuer sind",true)
                .addField("<:gold:1007022493955010631> Veteran / <:platin:1007022498296103024> Moderator",
                        "Benachrichtige die Servermoderation um diese Ränge zu beantragen",false)
                .addBlankField(false)
                .addField("Freunde einladen","Nutze diesen Link dafür:\nhttps://discord.gg/6yJm5kpfDp",false)
            .setFooter("Das Missbrauchen der Funktion führt zu einem Ausschluss (2 Anfrage pro Tag/Person)");

        MessageCreateBuilder messageBuilder = new MessageCreateBuilder()
                .addEmbeds(eb.build())
                .setComponents(ActionRow.of(
                        Button.secondary("ruler guest", Emoji.fromUnicode("<:bronze:1007022490037518526>")),
                        Button.secondary("ruler member",Emoji.fromUnicode("<:silber:1007022487411896320>"))
                ));
        channel.sendMessage(messageBuilder.build()).queue();
    }

    @Override
    public void performButtonInteraction(Member m, TextChannel channel, String customID, ButtonInteractionEvent event) {

        if (INTERACTIONS.containsKey(m.getId()) && INTERACTIONS.get(m.getId()) >= 5) {
            return;
        }

        Role guest = channel.getGuild().getRoleById("384133791595102218");
        Role member = channel.getGuild().getRoleById("286631357772201994");

        if (INTERACTIONS.containsKey(m.getId())) {
            INTERACTIONS.replace(m.getId(), INTERACTIONS.get(m.getId())+1);
        } else {
            INTERACTIONS.put(m.getId(),1);
        }

        switch (customID) {
            case "guest":
                if (hasHigherRank(m.getRoles())) {
                    sendFeedback(event,"Du hast bereits einen höheren Rang auf dem Server!",ServerUtil.RED,true);
                    return;
                }
                assert guest != null;
                assert member != null;
                channel.getGuild().addRoleToMember(UserSnowflake.fromId(m.getId()), guest).queue();
                channel.getGuild().removeRoleFromMember(User.fromId(m.getId()), member).queue();
                sendFeedback(event,"Du wurdest erfolgreich zum **Guest** ernannt!",ServerUtil.GREEN,true);
                break;
            case "member":
                if (hasHigherRank(m.getRoles())) {
                    sendFeedback(event,"Du hast bereits einen höheren Rang auf dem Server!",ServerUtil.RED,true);
                    return;
                }
                assert member != null;
                assert guest != null;
                channel.getGuild().addRoleToMember(UserSnowflake.fromId(m.getId()), member).queue();
                channel.getGuild().removeRoleFromMember(User.fromId(m.getId()), guest).queue();
                sendFeedback(event,"Du wurdest erfolgreich zum **Member** ernannt!",ServerUtil.GREEN,true);
                break;
        }
    }

    private boolean hasHigherRank(List<Role> roles) {
        return roles.stream().anyMatch(role -> role.getId().equals("286631270258180117")
                || role.getId().equals("286631247315337219"));
    }

    private void sendFeedback(ButtonInteractionEvent event, String description, int color, boolean ephemeral) {
        Member m = event.getMember();
        if (INTERACTIONS.get(m.getId()) >= 2) {
            description += "\n\nSperre: Du hast die maximale Menge an Anfragen pro Tag überschritten!\n" +
                    "Versuch es morgen wieder";
            color = ServerUtil.RED;
            ephemeral = false;
        }
        EmbedBuilder accept = new EmbedBuilder()
                .setAuthor(m.getEffectiveName(), m.getEffectiveAvatarUrl(), m.getEffectiveAvatarUrl())
                .setColor(color)
                .setDescription(description)
                .setFooter("Anfragen - (" + INTERACTIONS.get(m.getId()) + "/2)");
        event.replyEmbeds(accept.build()).setEphemeral(ephemeral).queue();
    }
}
