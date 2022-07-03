package org.virep.jdabot.commands.administration;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandGroupData;
import org.virep.jdabot.Main;
import org.virep.jdabot.slashcommandhandler.SlashCommand;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;

public class RoleCommand extends SlashCommand {
    public RoleCommand() {
        super(
                "role",
                "Configure roles that can be given for specific actions.",
                true,
                new SubcommandGroupData[] {
                        new SubcommandGroupData("auto", "Configure roles that are automatically given to new members.")
                                .addSubcommands(new SubcommandData("add", "Add a role to autorole").addOption(OptionType.ROLE, "role", "Role that will be given automatically to new members.", true),
                                new SubcommandData("remove", "Removes any role that has been configured from autorole")),
                        new SubcommandGroupData("reaction", "Configure roles that are given when clicking on a reaction.")
                                .addSubcommands(new SubcommandData("add", "Add roles to the reaction role.")
                                        .addOptions(new OptionData(OptionType.STRING, "messageid", "The ID of the message that users will interact on.", true),
                                                new OptionData(OptionType.ROLE, "role", "The role that users will obtain.", true),
                                                new OptionData(OptionType.STRING, "emoji", "The emoji that users will interact with.", true)),
                                new SubcommandData("remove", "Removes roles from the reaction role.")
                                        .addOptions(new OptionData(OptionType.STRING, "messageid", "The ID of the message that users will interact on.", true),
                                                new OptionData(OptionType.ROLE, "role", "The role that users will obtain.", true),
                                                new OptionData(OptionType.STRING, "emoji", "The emoji that users will interact with.", true)))
                }
        );
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        String subcommandGroup = event.getSubcommandGroup();
        String subcommandName = event.getSubcommandName();

        assert subcommandGroup != null;
        assert subcommandName != null;

        if (subcommandGroup.equals("auto")) {
            if (subcommandName.equals("add")) {

                try (PreparedStatement statement = Main.connectionDB.prepareStatement("SELECT * FROM autorole WHERE guildID = ?")) {
                    statement.setString(1, Objects.requireNonNull(event.getGuild()).getId());

                    ResultSet result = statement.executeQuery();

                    if (result.first()) {
                        // replace role
                        try(PreparedStatement updateStatement = Main.connectionDB.prepareStatement("""
                                UPDATE autorole SET roleID = ? WHERE guildID = ?
                                """)) {
                            updateStatement.setString(1, Objects.requireNonNull(event.getOption("role")).getAsRole().getId());
                            updateStatement.setString(2, event.getGuild().getId());

                            updateStatement.executeUpdate();
                            event.reply("The role " + Objects.requireNonNull(event.getOption("role")).getAsRole().getAsMention() + " has been added to the autorole !").setEphemeral(true).queue();
                        } catch (SQLException e) {
                            throw new RuntimeException(e);
                        }
                    } else {
                        // add role
                        try(PreparedStatement updateStatement = Main.connectionDB.prepareStatement("""
                                INSERT INTO autorole(roleID, guildID)
                                VALUES (?,?)
                                """)) {
                            updateStatement.setString(1, Objects.requireNonNull(event.getOption("role")).getAsRole().getId());
                            updateStatement.setString(2, event.getGuild().getId());

                            updateStatement.executeUpdate();
                            event.reply("The role " + Objects.requireNonNull(event.getOption("role")).getAsRole().getAsMention() + " has been added to the autorole !").setEphemeral(true).queue();
                        } catch (SQLException e) {
                            throw new RuntimeException(e);
                        }
                    }
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            } else {
                try (PreparedStatement statement = Main.connectionDB.prepareStatement("SELECT * FROM autorole WHERE guildID = ?")) {
                    statement.setString(1, Objects.requireNonNull(event.getGuild()).getId());

                    ResultSet result = statement.executeQuery();

                    if (!result.first()) {
                        event.reply("No role has been configured for the autorole.").setEphemeral(true).queue();
                        return;
                    }

                    try(PreparedStatement updateStatement = Main.connectionDB.prepareStatement("""
                                DELETE FROM autorole WHERE guildID = ?
                                """)) {
                        updateStatement.setString(1, event.getGuild().getId());

                        updateStatement.executeUpdate();
                        event.reply("Roles configured for the autorole have been cleared.").setEphemeral(true).queue();
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }
}