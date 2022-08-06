package org.virep.jdabot.commands.administration;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.channel.unions.GuildChannelUnion;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.entities.emoji.EmojiUnion;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandGroupData;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.Component;
import net.dv8tion.jda.api.interactions.components.ItemComponent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.selections.SelectMenu;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;
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
 * TODO: Allow configuration of max selection for select menu
 * TODO: Once finished, CLEAN THE GODDAMN COMMAND
 * TODO: Change buttonrole db name to interactionrole
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
                        new SubcommandGroupData("button", "Configure buttons interactions that'll give roles.")
                                .addSubcommands(
                                        new SubcommandData("set", "Set buttons to the specified message.")
                                                .addOptions(
                                                        new OptionData(OptionType.STRING, "messageid", "The ID of the message that the button will be added.", true),
                                                        new OptionData(OptionType.ROLE, "role", "The role that'll be given.", true),
                                                        new OptionData(OptionType.STRING, "name", "The name for the button.", true)
                                                ),
                                        new SubcommandData("remove", "Remove buttons from the specified message.")
                                                .addOptions(
                                                        new OptionData(OptionType.STRING, "messageid", "The ID of the message that the button will be removed.", true),
                                                        new OptionData(OptionType.ROLE, "role", "The role that corresponds to the button.", true)
                                                ),
                                        new SubcommandData("list", "List roles that corresponds to interaction buttons.")
                                ),
                        new SubcommandGroupData("selectmenu", "Configure selection menus that'll give roles.")
                                .addSubcommands(
                                        new SubcommandData("set", "Set select menus to the specified message.")
                                                .addOptions(
                                                        new OptionData(OptionType.STRING, "messageid", "The ID of the message that the select menu will be added or updated.", true),
                                                        new OptionData(OptionType.ROLE, "role", "The role that will be added to the select menu", true),
                                                        new OptionData(OptionType.STRING, "name", "Name of the select option", true),
                                                        new OptionData(OptionType.STRING, "description", "Description of the select option"),
                                                        new OptionData(OptionType.STRING, "emoji", "Emoji of the select option")
                                                ),
                                        new SubcommandData("remove", "Remove selection menu or roles from the select menu from the specified message.")
                                                .addOptions(
                                                        new OptionData(OptionType.STRING, "messageid", "The ID of the message that the select menu will be removed or updated.", true),
                                                        new OptionData(OptionType.ROLE, "role", "The role that will be removed from the select menu", true)
                                                ),
                                        new SubcommandData("list", "List roles that are configured on a selection menu.")
                                ),
                        new SubcommandGroupData("message", "Configure messages for Interaction Roles")
                                .addSubcommands(
                                        new SubcommandData("create", "Create a message on the specified channel.")
                                                .addOption(OptionType.CHANNEL, "channel", "Channel used for the interaction roles.", true),
                                        new SubcommandData("edit", "Edit a message from the specified channel.")
                                                .addOptions(
                                                        new OptionData(OptionType.STRING, "messageid", "The ID of the message that you want to edit.", true),
                                                        new OptionData(OptionType.STRING, "text", "The content of the edited message.", true)
                                                )
                                )
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

        if (event.getSubcommandGroup().equals("message")) {
            if (subcommandName.equals("create")) {
                GuildChannelUnion channelOption = event.getOption("channel").getAsChannel();
                TextChannel channel = channelOption.asTextChannel();

                try (PreparedStatement statement = Main.connectionDB.prepareStatement("SELECT * FROM buttonrole WHERE channelID = ?")) {
                    statement.setString(1, event.getOption("channel").getAsChannel().getId());

                    ResultSet result = statement.executeQuery();

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

                            event.reply("Sucessfully set up the message. Here is the messageID that you'll need to configure things up: `" + message.getId() + "`").queue();
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                    });
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }

            if (subcommandName.equals("edit")) {
                try (PreparedStatement statement = Main.connectionDB.prepareStatement("SELECT * FROM buttonrole WHERE messageID = ?")) {
                    statement.setString(1, event.getOption("messageid").getAsString());

                    ResultSet result = statement.executeQuery();

                    if (!result.first()) {
                        event.reply("This message ID is either invalid or does not corresponds to a message linked to Interaction Roles.").setEphemeral(true).queue();
                        return;
                    }

                    String channelID = result.getString(1);
                    TextChannel channel = event.getGuild().getTextChannelById(channelID);

                    channel.retrieveMessageById(event.getOption("messageid").getAsString()).queue(msg -> {
                        msg.editMessage(event.getOption("text").getAsString()).queue();

                        event.reply("The message has been successfully edited!").queue();
                    });
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }

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

                    while (result.next()) {
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

            if (event.getSubcommandGroup().equals("selectmenu")) {
                try (PreparedStatement statement = Main.connectionDB.prepareStatement("SELECT * FROM interactionrole_selectmenu WHERE guildID = ? ORDER BY messageID DESC")) {
                    statement.setString(1, event.getGuild().getId());

                    ResultSet result = statement.executeQuery();

                    if (!result.first()) {
                        event.reply("You don't have any roles linked.").setEphemeral(true).queue();
                        return;
                    }

                    StringBuilder sb = new StringBuilder();

                    while (result.next()) {
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
                                TextChannel channel = event.getGuild().getTextChannelById(result.getString(1));

                                channel.retrieveMessageById(messageID).queue(msg -> {

                                    if (!msg.getActionRows().isEmpty() && msg.getActionRows().get(0).getComponents().get(0).getType().equals(Component.Type.SELECT_MENU)) {
                                        event.reply("Only one Component Row is supposed to be on a message. Please create a new message or remove the configured selection menu.").setEphemeral(true).queue();
                                        return;
                                    }

                                    try (PreparedStatement insertStatement = Main.connectionDB.prepareStatement("INSERT INTO interactionrole_buttons(guildID, messageID, buttonID, roleID) VALUES (?,?,?,?)")) {
                                        insertStatement.setString(1, event.getGuild().getId());
                                        insertStatement.setString(2, messageID);
                                        insertStatement.setString(3, "interactionrole:" + event.getGuild().getId() + ":" + role.getId());
                                        insertStatement.setString(4, role.getId());

                                        insertStatement.executeUpdate();

                                        msg.editMessageComponents().setActionRow(
                                                Button.primary("interactionrole:" + event.getGuild().getId() + ":" + role.getId(), buttonName)
                                        ).queue();

                                        event.reply("Sucessfully added the button for the " + role.getAsMention() + " role.").queue();
                                    } catch (SQLException e) {
                                        e.printStackTrace();
                                    }
                                });
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
                                            event.reply("You cannot put more than 5 buttons on a message. Please create a new message or remove a button.").setEphemeral(true).queue();
                                            return;
                                        }
                                        List<Button> buttons = new ArrayList<>();

                                        msg.getButtons().forEach(button -> {
                                            buttons.add(Button.primary(button.getId(), button.getLabel()));
                                        });

                                        buttons.add(Button.primary("interactionrole:" + event.getGuild().getId() + ":" + role.getId(), buttonName));

                                        msg.editMessageComponents().setActionRow(buttons).queue();
                                        event.reply("Sucessfully added the button for the " + role.getAsMention() + " role.").queue();
                                    });
                                } catch (SQLException e) {
                                    e.printStackTrace();
                                }
                            }
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                    } else {
                        event.reply("Please enable Interaction Roles first by creating a message.").setEphemeral(true).queue();
                        return;
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }

            if (event.getSubcommandGroup().equals("selectmenu")) {
                String messageID = event.getOption("messageid").getAsString();

                try (PreparedStatement statement = Main.connectionDB.prepareStatement("SELECT * FROM buttonrole WHERE messageID = ?")) {
                    statement.setString(1, messageID);

                    ResultSet result = statement.executeQuery();

                    if (result.first()) {
                        try (PreparedStatement selectMenuStatement = Main.connectionDB.prepareStatement("SELECT * FROM interactionrole_selectmenu WHERE messageID = ?")) {
                            selectMenuStatement.setString(1, messageID);

                            ResultSet smResult = selectMenuStatement.executeQuery();

                            if (!smResult.first()) {
                                TextChannel channel = event.getGuild().getTextChannelById(result.getString(1));

                                channel.retrieveMessageById(messageID).queue(msg -> {
                                    if (!msg.getActionRows().isEmpty() && msg.getActionRows().get(0).getComponents().get(0).getType().equals(Component.Type.BUTTON)) {
                                        event.reply("Only one Component Row is supposed to be on a message. Please create a new message or remove the configured button.").setEphemeral(true).queue();
                                        return;
                                    }
                                    try (PreparedStatement insertStatement = Main.connectionDB.prepareStatement("INSERT INTO interactionrole_selectmenu (messageID, guildID, choiceID, roleID) VALUES (?,?,?,?)")) {
                                        insertStatement.setString(1, messageID);
                                        insertStatement.setString(2, event.getGuild().getId());
                                        insertStatement.setString(3, "selectmenurole:" + event.getGuild().getId() + ":" + event.getOption("role").getAsRole().getId());
                                        insertStatement.setString(4, event.getOption("role").getAsRole().getId());

                                        insertStatement.executeUpdate();

                                        String choiceLabel = event.getOption("name").getAsString();
                                        String choiceValue = ("selectmenurole:" + event.getGuild().getId() + ":" + event.getOption("role").getAsRole().getId());
                                        String choiceDescription = event.getOption("description") != null ? event.getOption("description").getAsString() : null;

                                        Emoji emoji;
                                        if (event.getOption("emoji") != null) {
                                            EmojiUnion fromFormattedEmoji = Emoji.fromFormatted(event.getOption("emoji").getAsString());
                                            Emoji.Type emojiType = fromFormattedEmoji.getType();

                                            if (emojiType == Emoji.Type.CUSTOM) emoji = fromFormattedEmoji.asCustom();
                                            else emoji = fromFormattedEmoji.asUnicode();
                                        } else emoji = null;


                                        // new OptionData(OptionType.STRING, "name", "name of the select option", true),
                                        //      new OptionData(OptionType.STRING, "description", "description of the select option"),
                                        //       ew OptionData(OptionType.STRING, "emoji", "emoji of the select option")
                                        //
                                        msg.editMessageComponents().setActionRow(
                                                SelectMenu.create("selectmenurole:" + event.getGuild().getId())
                                                        .addOption(choiceLabel, choiceValue, choiceDescription, emoji)
                                                        .build()
                                        ).queue();

                                        event.reply("Sucessfully created selection menu!").queue();
                                    } catch (SQLException e) {
                                        e.printStackTrace();
                                    }
                                });
                            } else {
                                try (PreparedStatement insertStatement = Main.connectionDB.prepareStatement("INSERT INTO interactionrole_selectmenu (messageID, guildID, choiceID, roleID) VALUES (?,?,?,?)")) {
                                    insertStatement.setString(1, messageID);
                                    insertStatement.setString(2, event.getGuild().getId());
                                    insertStatement.setString(3, "selectmenurole:" + event.getGuild().getId() + ":" + event.getOption("role").getAsRole().getId());
                                    insertStatement.setString(4, event.getOption("role").getAsRole().getId());

                                    insertStatement.executeUpdate();

                                    TextChannel channel = event.getGuild().getTextChannelById(result.getString(1));

                                    channel.retrieveMessageById(messageID).queue(msg -> {
                                        String choiceLabel = event.getOption("name").getAsString();
                                        String choiceValue = ("selectmenurole:" + event.getGuild().getId() + ":" + event.getOption("role").getAsRole().getId());
                                        String choiceDescription = event.getOption("description") != null ? event.getOption("description").getAsString() : null;

                                        Emoji emoji;
                                        if (event.getOption("emoji") != null) {
                                            EmojiUnion fromFormattedEmoji = Emoji.fromFormatted(event.getOption("emoji").getAsString());
                                            Emoji.Type emojiType = fromFormattedEmoji.getType();

                                            if (emojiType == Emoji.Type.CUSTOM) emoji = fromFormattedEmoji.asCustom();
                                            else emoji = fromFormattedEmoji.asUnicode();
                                        } else emoji = null;


                                        ActionRow actionRow = msg.getActionRows().get(0);
                                        SelectMenu selectMenu = (SelectMenu) actionRow.getComponents().get(0);

                                        SelectMenu.Builder builder = selectMenu.createCopy().addOption(choiceLabel, choiceValue, choiceDescription, emoji);

                                        msg.editMessageComponents().setActionRow(builder.build()).queue();

                                        event.reply("Successfuly updated the selection menu!").queue();
                                    });
                                } catch (SQLException e) {
                                    e.printStackTrace();
                                }
                            }
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
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
                                            if (!button.getId().equals("interactionrole:" + event.getGuild().getId() + ":" + role.getId()))
                                                buttons.add(Button.primary(button.getId(), button.getLabel()));
                                        });

                                        if (buttons.isEmpty()) msg.editMessageComponents().setActionRows().queue();
                                        else msg.editMessageComponents().setActionRows(ActionRow.of(buttons)).queue();

                                        event.reply("Successfully removed the button.").queue();
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

            if (event.getSubcommandGroup().equals("selectmenu")) {
                String messageID = event.getOption("messageid").getAsString();
                Role role = event.getOption("role").getAsRole();

                try (PreparedStatement statement = Main.connectionDB.prepareStatement("SELECT * FROM buttonrole WHERE messageID = ?")) {
                    statement.setString(1, messageID);

                    ResultSet result = statement.executeQuery();

                    if (result.first()) {
                        try (PreparedStatement selectMenuStatement = Main.connectionDB.prepareStatement("SELECT * FROM interactionrole_selectmenu WHERE messageID = ? AND roleID = ?")) {
                            selectMenuStatement.setString(1, messageID);
                            selectMenuStatement.setString(2, role.getId());

                            ResultSet smResult = selectMenuStatement.executeQuery();

                            if (smResult.first()) {
                                TextChannel channel = event.getGuild().getTextChannelById(result.getString(1));

                                try (PreparedStatement removeStatement = Main.connectionDB.prepareStatement("DELETE FROM interactionrole_selectmenu WHERE messageID = ? AND roleID = ?")) {
                                    removeStatement.setString(1, messageID);
                                    removeStatement.setString(2, role.getId());

                                    removeStatement.executeUpdate();

                                    channel.retrieveMessageById(messageID).queue(msg -> {
                                        ActionRow actionRow = msg.getActionRows().get(0);
                                        SelectMenu selectMenu = (SelectMenu) actionRow.getComponents().get(0);

                                        List<SelectOption> selectOptions = new ArrayList<>();

                                        selectMenu.getOptions().forEach(option -> {
                                            if (!option.getValue().equals("selectmenurole:" + event.getGuild().getId() + ":" + role.getId()))
                                                selectOptions.add(option);
                                        });

                                        SelectMenu newMenu = SelectMenu.create("selectmenurole:" + event.getGuild().getId()).addOptions(selectOptions).build();

                                        msg.editMessageComponents().setActionRow(newMenu).queue();

                                        event.reply("Successfully removed selection from the select menu!").queue();
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
    }
}