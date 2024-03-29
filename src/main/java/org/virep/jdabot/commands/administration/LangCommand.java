package org.virep.jdabot.commands.administration;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import org.virep.jdabot.database.Database;
import org.virep.jdabot.language.Language;
import org.virep.jdabot.slashcommandhandler.Command;
import org.virep.jdabot.utils.ErrorManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

public class LangCommand implements Command {

    @Override
    public String getName() {
        return "lang";
    }

    @Override
    public SlashCommandData getCommandData() {
        return Commands.slash(getName(), "Configure the bot language for your server.")
                .setGuildOnly(true)
                .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.MANAGE_SERVER))
                .setDescriptionLocalization(DiscordLocale.FRENCH, "Configure la langue du bot sur votre serveur.")
                .addOptions(
                        new OptionData(OptionType.STRING, "lang", "The lang to configure.", true)
                                .setDescriptionLocalization(DiscordLocale.FRENCH, "La langue a configurer.")
                                .addChoices(
                                        new net.dv8tion.jda.api.interactions.commands.Command.Choice("English", "en"),
                                        new net.dv8tion.jda.api.interactions.commands.Command.Choice("Français", "fr")
                                )
                );
    }

    @Override
    public List<Permission> getBotPermissions() {
        return Collections.emptyList();
    }

    @Override
    public boolean isDev() {
        return true;
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        Member member = event.getMember();
        Guild guild = event.getGuild();

        if (!member.hasPermission(Permission.MANAGE_SERVER)) {
            event.reply(Language.getString("NO_PERMISSION", guild)).setEphemeral(true).queue();
            return;
        }

        String lang = event.getOption("lang", OptionMapping::getAsString);

        try (Connection connection = Database.getConnection(); PreparedStatement selectStatement = connection.prepareStatement("SELECT * FROM lang WHERE guildID = ?")) {
            selectStatement.setString(1, event.getGuild().getId());

            ResultSet result = selectStatement.executeQuery();

            if (result.first()) {
                try (PreparedStatement updateStatement = connection.prepareStatement("UPDATE lang SET lang = ? WHERE guildID = ?")) {
                    updateStatement.setString(1, lang);
                    updateStatement.setString(2, event.getGuild().getId());

                    updateStatement.executeUpdate();
                }
            } else {
                try (PreparedStatement insertStatement = connection.prepareStatement("INSERT INTO lang (guildID, lang) VALUES (?,?)")) {
                    insertStatement.setString(1, event.getGuild().getId());
                    insertStatement.setString(2, lang);

                    insertStatement.executeUpdate();
                }
            }

            Language.langs.put(event.getGuild().getId(), lang);

            event.reply(lang.equals("fr") ? "\u2705 - La langue a bien été mise en Français !" : "\u2705 - Language has been successfully set to English !").queue();

            connection.close();
        } catch (SQLException e) {
            ErrorManager.handle(e, event);
        }
    }
}
