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
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import org.virep.jdabot.database.Database;
import org.virep.jdabot.language.Language;
import org.virep.jdabot.slashcommandhandler.Command;
import org.virep.jdabot.utils.ErrorManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;

public class AutoroleCommand implements Command {
    @Override
    public String getName() {
        return "autorole";
    }

    @Override
    public SlashCommandData getCommandData() {
        return Commands.slash(getName(), "Configure roles that are automatically given to new members.")
                .setGuildOnly(true)
                .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.MANAGE_SERVER))
                .setDescriptionLocalization(DiscordLocale.FRENCH, "Configure des rôles qui seront donnés automatiquement a l'arrivée de membres.")
                .addSubcommands(
                        new SubcommandData("add", "Add a role to autorole")
                                .setDescriptionLocalization(DiscordLocale.FRENCH, "Ajoute un rôle a l'autorole")
                                .addOptions(
                                        new OptionData(OptionType.ROLE, "role", "The role that will be given automatically to new members.", true)
                                                .setDescriptionLocalization(DiscordLocale.FRENCH, "Le role qui sera donné automatiquement aux nouveaux membres.")
                                ),
                        new SubcommandData("remove", "Removes any role that has been configured from autorole")
                                .setDescriptionLocalization(DiscordLocale.FRENCH, "Retire les rôles qui ont été configurés pour l'autorole.")
                );
    }

    @Override
    public boolean isDev() {
        return false;
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        Member member = event.getMember();
        Guild guild = event.getGuild();

        assert member != null;
        if (!member.hasPermission(Permission.MANAGE_SERVER)) {
            event.reply(Language.getString("NO_PERMISSION", guild)).setEphemeral(true).queue();
            return;
        }

        if (event.getSubcommandName().equals("add")) {
            try (Connection connection = Database.getConnection(); PreparedStatement statement = connection.prepareStatement("SELECT * FROM autorole WHERE guildID = ?")) {
                statement.setString(1, guild.getId());

                ResultSet result = statement.executeQuery();

                if (result.first()) {
                    try (PreparedStatement updateStatement = connection.prepareStatement("UPDATE autorole SET roleID = ? WHERE guildID = ?")) {
                        updateStatement.setString(1, event.getOption("role", OptionMapping::getAsRole).getId());
                        updateStatement.setString(2, guild.getId());

                        updateStatement.executeUpdate();

                        event.reply(Language.getString("AUTOROLE_ADD_UPDATE_SUCCESS", guild).replace("%ROLEMENTION", event.getOption("role", OptionMapping::getAsRole).getAsMention())).setEphemeral(true).queue();
                    }
                } else {
                    try (PreparedStatement insertStatement = connection.prepareStatement("INSERT INTO autorole (roleID, guildID) VALUES (?,?)")) {
                        insertStatement.setString(1, event.getOption("role", OptionMapping::getAsRole).getId());
                        insertStatement.setString(2, guild.getId());

                        insertStatement.executeUpdate();

                        event.reply(Language.getString("AUTOROLE_ADD_UPDATE_SUCCESS", guild).replace("%ROLEMENTION", event.getOption("role", OptionMapping::getAsRole).getAsMention())).setEphemeral(true).queue();
                    }
                }
                connection.close();
            } catch (SQLException e) {
                ErrorManager.handle(e, event);
            }
        } else {
            try (Connection connection = Database.getConnection(); PreparedStatement statement = connection.prepareStatement("SELECT * FROM autorole WHERE guildID = ?")) {
                statement.setString(1, guild.getId());

                ResultSet result = statement.executeQuery();

                if (!result.first()) {
                    event.reply(Language.getString("AUTOROLE_REMOVE_NOROLE", guild)).setEphemeral(true).queue();
                    return;
                }

                try (PreparedStatement insertStatement = connection.prepareStatement("DELETE FROM autorole WHERE guildID = ?")) {
                    insertStatement.setString(1, guild.getId());

                    insertStatement.executeUpdate();

                    event.reply(Language.getString("AUTOROLE_REMOVE_CLEARED", guild)).setEphemeral(true).queue();
                }
                connection.close();
            } catch (SQLException e) {
                ErrorManager.handle(e, event);
            }
        }
    }
}
