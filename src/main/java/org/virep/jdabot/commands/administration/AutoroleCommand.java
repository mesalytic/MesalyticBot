package org.virep.jdabot.commands.administration;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import org.virep.jdabot.database.Database;
import org.virep.jdabot.slashcommandhandler.Command;

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
                .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.MANAGE_SERVER))
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
        Member member = event.getMember();

        assert member != null;
        if (!member.hasPermission(Permission.MANAGE_SERVER)) {
            event.reply("You do not have permission to use this command.").setEphemeral(true).queue();
            return;
        }

        if (event.getSubcommandName().equals("add")) {
            try {
                ResultSet result = Database.executeQuery("SELECT * FROM autorole WHERE guildID = '" + event.getGuild().getId() + "'");

                if (result.first()) {
                    // replace role
                    Database.executeQuery("UPDATE autorole SET roleID = '" + event.getOption("role", OptionMapping::getAsRole).getId() + "' WHERE guildID = '" + event.getGuild().getId() + "'");

                    event.reply("The role " + Objects.requireNonNull(event.getOption("role")).getAsRole().getAsMention() + " has been added to the autorole !").setEphemeral(true).queue();
                } else {
                    // add role
                    Database.executeQuery("INSERT INTO autorole(roleID, guildID) VALUES ('" + event.getOption("role", OptionMapping::getAsRole).getId() + "','" + event.getGuild().getId());

                    event.reply("The role " + Objects.requireNonNull(event.getOption("role")).getAsRole().getAsMention() + " has been added to the autorole !").setEphemeral(true).queue();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else {
            try {
                ResultSet result = Database.executeQuery("SELECT * FROM autorole WHERE guildID = '" + event.getGuild().getId() + "'");

                if (!result.first()) {
                    event.reply("No role has been configured for the autorole.").setEphemeral(true).queue();
                    return;
                }

                Database.executeQuery("DELETE FROM autorole WHERE guildID = '" + event.getGuild().getId() + "'");

                event.reply("Roles configured for the autorole have been cleared.").setEphemeral(true).queue();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
