package org.virep.jdabot.commands.administration;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.internal.interactions.CommandDataImpl;
import org.virep.jdabot.Main;
import org.virep.jdabot.slashcommandhandler.Command;

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
    public CommandData getCommandData() {
        return new CommandDataImpl(getName(), "Configure roles that are automatically given to new members.")
                .addSubcommands(
                        new SubcommandData("add", "Add a role to autorole")
                                .addOption(OptionType.ROLE, "role", "Role that will be given automatically to new members.", true),
                        new SubcommandData("remove", "Removes any role that has been configured from autorole")
                );
    }

    @Override
    public boolean isDev() {
        return false;
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        if (event.getSubcommandName().equals("add")) {

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
                        e.printStackTrace();
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
                        e.printStackTrace();
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
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
                    e.printStackTrace();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
