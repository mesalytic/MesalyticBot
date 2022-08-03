package org.virep.jdabot.commands.administration;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.channel.unions.GuildChannelUnion;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandGroupData;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.ItemComponent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.internal.interactions.CommandDataImpl;
import net.dv8tion.jda.internal.interactions.component.ButtonImpl;
import org.virep.jdabot.Main;
import org.virep.jdabot.slashcommandhandler.Command;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/*
* TODO: Add roles to Selection Menus
* TODO: Remove roles from Selection Menus
* TODO: List roles from Selection Menus
*
* TODO: Add new Interaction Messages to specific channels
* TODO: Possibility to edit Interaction Messages
*/

public class InteractionroleCommand implements Command {
    @Override
    public String getName() {
        return "interactionrole";
    }

    @Override
    public CommandData getCommandData() {
        return new CommandDataImpl(getName(), "Configure roles that can be given for specific actions.")
                .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.MANAGE_SERVER))
                .addSubcommandGroups(
                        new SubcommandGroupData("button", "Button related cmds")
                                .addSubcommands(
                                        new SubcommandData("set", "set button to message")
                                                .addOptions(
                                                        new OptionData(OptionType.STRING, "messageid", "the message id", true),
                                                        new OptionData(OptionType.ROLE, "role", "the role", true),
                                                        new OptionData(OptionType.STRING, "name", "name of the button", true)
                                                ),
                                        new SubcommandData("remove", "remove button from message")
                                                .addOptions(
                                                        new OptionData(OptionType.STRING, "messageid", "the message id", true),
                                                        new OptionData(OptionType.ROLE, "role", "the role", true)
                                                ),
                                        new SubcommandData("list", "list buttons roles")
                                )
                )
                .addSubcommands(
                        new SubcommandData("channel", "Set the channel that will be used. (Advised on it being empty.)")
                                .addOption(OptionType.CHANNEL, "channel", "Channel used for the interaction roles.", true)
                );
    }

    @Override
    public boolean isDev() {
        return true;
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        if (!event.getMember().hasPermission(Permission.MANAGE_SERVER)) {
            event.reply("You do not have permission to use this command.").setEphemeral(true).queue();
            return;
        }

        String subcommandName = event.getSubcommandName();
        assert subcommandName != null;

        if (subcommandName.equals("list")) {
            if (event.getSubcommandGroup().equals("button")) {
                try (PreparedStatement statement = Main.connectionDB.prepareStatement("SELECT * FROM interactionrole_buttons WHERE guildID = ? ORDER BY messageID DESC")) {
                    statement.setString(1, event.getGuild().getId());

                    ResultSet result = statement.executeQuery();

                    if (!result.first()) {
                        event.reply("You don't have any roles linked.").setEphemeral(true).queue();
                        return;
                    }

                    StringBuilder sb = new StringBuilder();

                    while(result.next()) {
                        sb.append(result.getString(2)).append(" - ").append(event.getGuild().getRoleById(result.getString(4)).getAsMention()).append("\n");
                    }

                    MessageEmbed embed = new EmbedBuilder()
                            .setDescription(sb.toString())
                            .build();

                    event.replyEmbeds(embed).queue();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }

        if (subcommandName.equals("set")) {
            if (event.getSubcommandGroup().equals("button")) {
                String messageID = event.getOption("messageid").getAsString();
                Role role = event.getOption("role").getAsRole();
                String buttonName = event.getOption("name").getAsString();

                try (PreparedStatement statement = Main.connectionDB.prepareStatement("SELECT * FROM buttonrole WHERE messageID = ?")) {
                    statement.setString(1, messageID);

                    ResultSet result = statement.executeQuery();

                    if (result.first()) {
                        try (PreparedStatement otherStatement = Main.connectionDB.prepareStatement("SELECT * FROM interactionrole_buttons WHERE messageID = ?")) {
                            otherStatement.setString(1, messageID);

                            ResultSet otherResult = otherStatement.executeQuery();

                            otherResult.last();

                            if (!otherResult.first()) {
                                try (PreparedStatement insertStatement = Main.connectionDB.prepareStatement("INSERT INTO interactionrole_buttons(guildID, messageID, buttonID, roleID) VALUES (?,?,?,?)")) {
                                    insertStatement.setString(1, event.getGuild().getId());
                                    insertStatement.setString(2, messageID);
                                    insertStatement.setString(3, "interactionrole:" + event.getGuild().getId() + ":" + role.getId());
                                    insertStatement.setString(4, role.getId());

                                    insertStatement.executeUpdate();

                                    TextChannel channel = event.getGuild().getTextChannelById(result.getString(1));

                                    channel.retrieveMessageById(messageID).queue(msg -> {
                                        msg.editMessageComponents().setActionRow(
                                                Button.primary("interactionrole:" + event.getGuild().getId() + ":" + role.getId(), buttonName)
                                        ).queue();

                                        event.reply("!otherResult.first() success").queue();
                                    });
                                } catch (SQLException e) {
                                    e.printStackTrace();
                                }
                            } else {
                                try (PreparedStatement insertStatement = Main.connectionDB.prepareStatement("INSERT INTO interactionrole_buttons(guildID, messageID, buttonID, roleID) VALUES (?,?,?,?)")) {
                                    insertStatement.setString(1, event.getGuild().getId());
                                    insertStatement.setString(2, messageID);
                                    insertStatement.setString(3, "interactionrole:" + event.getGuild().getId() + ":" + role.getId());
                                    insertStatement.setString(4, role.getId());

                                    insertStatement.executeUpdate();

                                    TextChannel channel = event.getGuild().getTextChannelById(result.getString(1));

                                    channel.retrieveMessageById(messageID).queue(msg -> {
                                        if (msg.getButtons().size() >= 5) {
                                            event.reply("You cannot put more than 5 buttons on a message. Please create a new one.").setEphemeral(true).queue();
                                            return;
                                        }
                                        List<Button> buttons = new ArrayList<>();

                                        msg.getButtons().forEach(button -> {
                                            buttons.add(Button.primary(button.getId(), button.getLabel()));
                                        });

                                        buttons.add(Button.primary("interactionrole:" + event.getGuild().getId() + ":" + role.getId(), buttonName));

                                        msg.editMessageComponents().setActionRow(buttons).queue();
                                        event.reply("otherResult.first() success").queue();
                                    });
                                } catch (SQLException e) {
                                    e.printStackTrace();
                                }
                            }
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                    } else {
                        event.reply("Please enable Interaction Roles first by selecting a channel.").setEphemeral(true).queue();
                        return;
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }

        if (subcommandName.equals("remove")) {
            if (event.getSubcommandGroup().equals("button")) {
                String messageID = event.getOption("messageid").getAsString();
                Role role = event.getOption("role").getAsRole();

                try (PreparedStatement statement = Main.connectionDB.prepareStatement("SELECT * FROM buttonrole WHERE messageID = ?")) {
                    statement.setString(1, messageID);

                    ResultSet result = statement.executeQuery();

                    if (result.first()) {
                        try (PreparedStatement otherStatement = Main.connectionDB.prepareStatement("SELECT * FROM interactionrole_buttons WHERE messageID = ? AND roleID = ?")) {
                            otherStatement.setString(1, messageID);
                            otherStatement.setString(2, role.getId());

                            ResultSet otherResult = otherStatement.executeQuery();

                            if (otherResult.first()) {
                                TextChannel channel = event.getGuild().getTextChannelById(result.getString(1));

                                try (PreparedStatement removeStatement = Main.connectionDB.prepareStatement("DELETE FROM interactionrole_buttons WHERE messageID = ? AND roleID = ?")) {
                                    removeStatement.setString(1, messageID);
                                    removeStatement.setString(2, role.getId());

                                    removeStatement.executeUpdate();

                                    channel.retrieveMessageById(messageID).queue(msg -> {
                                        List<Button> buttons = new ArrayList<>();

                                        msg.getButtons().forEach(button -> {
                                            if (!button.getId().equals("interactionrole:" + event.getGuild().getId() + ":" + role.getId())) buttons.add(Button.primary(button.getId(), button.getLabel()));
                                        });

                                        if (buttons.isEmpty()) msg.editMessageComponents().setActionRows().queue();
                                        else msg.editMessageComponents().setActionRows(ActionRow.of(buttons)).queue();

                                        event.reply("otherResult.first() success").queue();
                                    });
                                } catch (SQLException e) {
                                    e.printStackTrace();
                                }
                            }
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                    } else {
                        event.reply("This message is not from an InteractionRole enabled channel.").setEphemeral(true).queue();
                        return;
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }

        if (subcommandName.equals("channel")) {
            GuildChannelUnion channelOption = event.getOption("channel").getAsChannel();
            TextChannel channel = channelOption.asTextChannel();

            try (PreparedStatement statement = Main.connectionDB.prepareStatement("SELECT * FROM buttonrole WHERE channelID = ?")) {
                statement.setString(1, event.getOption("channel").getAsChannel().getId());

                ResultSet result = statement.executeQuery();

                if (result.first()) {
                    event.reply("This channel is already used for the Interaction Role feature. Please select another channel.").setEphemeral(true).queue();
                    return;
                }

                channel.sendMessage("Hi there, this is the message that will be used for Interaction Roles. Interaction Roles are a neat way for users to claim specific roles.\n" +
                        "\n" +
                        "Right now, no roles have been configured, but there are multiple ways to do so :\n" +
                        "\n" +
                        "You can use `/interactionrole button set messageID:messageID role:@role name?:name` to set up a button that will be directly linked to a button on the message. Please consider right now there is a limit of 5 buttons per message.\n" +
                        "\n" +
                        "If you want to add a new message, to set up more roles using buttons, or to organize things up, you can use the `/interactionrole message add` command to create a new message.\n" +
                        "\n" +
                        "Instead, if you want to use selection menus, you can use the `/interactionrole menu set messageID:messageID role:@role name:name selectDescription:description` that will add a Selection Menu on the message, with the specified name and description as a choice.\n" +
                        "\n" +
                        "Of course, you can edit this message to not show this tutorial, use `/interactionrole message edit messageID:messageID text:text`\n" +
                        "\n" +
                        "Have fun configuring the Interaction Roles !").queue(message -> {
                            try (PreparedStatement insertStatement = Main.connectionDB.prepareStatement("INSERT INTO buttonrole (channelID, guildID, messageID) VALUES (?,?,?)")) {
                                insertStatement.setString(1, event.getOption("channel").getAsChannel().getId());
                                insertStatement.setString(2, event.getGuild().getId());
                                insertStatement.setString(3, message.getId());

                                insertStatement.executeUpdate();

                                event.reply("Sucessfully set up the channel. A setup message has been sent, here is the messageID that you'll need to configure things up: `" + message.getId() + "`").queue();
                            } catch (SQLException e) {
                                e.printStackTrace();
                            }
                        });
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}