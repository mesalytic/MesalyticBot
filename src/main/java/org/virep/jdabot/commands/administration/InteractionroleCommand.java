package org.virep.jdabot.commands.administration;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.unions.GuildChannelUnion;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.entities.emoji.EmojiUnion;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.*;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.Component;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.selections.SelectMenu;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;
import org.virep.jdabot.database.Database;
import org.virep.jdabot.language.Language;
import org.virep.jdabot.slashcommandhandler.Command;
import org.virep.jdabot.utils.ErrorManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class InteractionroleCommand implements Command {
    @Override
    public String getName() {
        return "interactionrole";
    }

    @Override
    public SlashCommandData getCommandData() {
        return Commands.slash(getName(), "Configure roles that can be given for specific actions.")
                .setDescriptionLocalization(DiscordLocale.FRENCH, "Configure des rôles qui peuvent être données après une intéraction.")
                .setGuildOnly(true)
                .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.MANAGE_SERVER))
                .addSubcommandGroups(
                        new SubcommandGroupData("button", "Configure buttons interactions that will give roles when interacting.")
                                .setDescriptionLocalization(DiscordLocale.FRENCH, "Configure des intéractions boutons qui donneront des rôles")
                                .addSubcommands(
                                        new SubcommandData("set", "Set buttons to the specified message.")
                                                .setDescriptionLocalization(DiscordLocale.FRENCH, "Configure des boutons pour le message spécifié")
                                                .addOptions(
                                                        new OptionData(OptionType.STRING, "messageid", "The ID of the message that the button will be added.", true)
                                                                .setDescriptionLocalization(DiscordLocale.FRENCH, "L'ID du message auquel le bouton sera ajouté."),
                                                        new OptionData(OptionType.ROLE, "role", "The role that will be given when interacting with the button.", true)
                                                                .setDescriptionLocalization(DiscordLocale.FRENCH, "Le rôle qui sera donné après avoir intéragi avec le bouton"),
                                                        new OptionData(OptionType.STRING, "name", "The name for the button.", true)
                                                                .setDescriptionLocalization(DiscordLocale.FRENCH, "Le nom du bouton")
                                                ),
                                        new SubcommandData("remove", "Remove buttons from the specified message.")
                                                .setDescriptionLocalization(DiscordLocale.FRENCH, "Retire des boutons du message spécifié")
                                                .addOptions(
                                                        new OptionData(OptionType.STRING, "messageid", "The ID of the message that the button will be removed.", true)
                                                                .setDescriptionLocalization(DiscordLocale.FRENCH, "L'ID du message auquel le bouton sera retiré."),
                                                        new OptionData(OptionType.ROLE, "role", "The role that corresponds to the button.", true)
                                                                .setDescriptionLocalization(DiscordLocale.FRENCH, "Le rôle qui corresponds au bouton retiré.")
                                                ),
                                        new SubcommandData("list", "List roles that corresponds to interaction buttons.")
                                                .setDescriptionLocalization(DiscordLocale.FRENCH, "Liste les rôles qui correspondent a toutes les intéractions boutons")
                                ),
                        new SubcommandGroupData("selectmenu", "Configure selection menus that will give roles when interacting.")
                                .setDescriptionLocalization(DiscordLocale.FRENCH, "Configure des menus de selections qui donneront des rôles")
                                .addSubcommands(
                                        new SubcommandData("set", "Set select menus to the specified message.")
                                                .setDescriptionLocalization(DiscordLocale.FRENCH, "Ajoute des menus de sélections a un message.")
                                                .addOptions(
                                                        new OptionData(OptionType.STRING, "messageid", "The ID of the message that the select menu will be added or updated.", true)
                                                                .setDescriptionLocalization(DiscordLocale.FRENCH, "L'ID du message auquel le menu de sélection sera ajouté ou mis a jour."),
                                                        new OptionData(OptionType.ROLE, "role", "The role that will be added to the select menu", true)
                                                                .setDescriptionLocalization(DiscordLocale.FRENCH, "Le rôle qui sera ajouté au menu de sélection"),
                                                        new OptionData(OptionType.STRING, "name", "Name of the select option", true)
                                                                .setDescriptionLocalization(DiscordLocale.FRENCH, "Nom de l'option de sélection"),
                                                        new OptionData(OptionType.STRING, "description", "Description of the select option")
                                                                .setDescriptionLocalization(DiscordLocale.FRENCH, "Description de l'option de sélection"),
                                                        new OptionData(OptionType.STRING, "emoji", "Emoji of the select option")
                                                                .setDescriptionLocalization(DiscordLocale.FRENCH, "Emoji de l'option de sélection")
                                                ),
                                        new SubcommandData("remove", "Remove selection menu or roles from the select menu from the specified message.")
                                                .setDescriptionLocalization(DiscordLocale.FRENCH, "Retire le menu de sélection ou une option lié au message")
                                                .addOptions(
                                                        new OptionData(OptionType.STRING, "messageid", "The ID of the message that the select menu will be removed or updated.", true)
                                                                .setDescriptionLocalization(DiscordLocale.FRENCH, "L'ID du message auquel le menu de sélection ou l'option sera retiré"),
                                                        new OptionData(OptionType.ROLE, "role", "The role that will be removed from the select menu", true)
                                                                .setDescriptionLocalization(DiscordLocale.FRENCH, "Le rôle qui sera retiré du menu de sélection")
                                                ),
                                        new SubcommandData("list", "List roles that are configured on a selection menu.")
                                                .setDescriptionLocalization(DiscordLocale.FRENCH, "List les rôles qui sont configurés a un menu de sélection")
                                ),
                        new SubcommandGroupData("message", "Configure messages for Interaction Roles")
                                .setDescriptionLocalization(DiscordLocale.FRENCH, "Configure des messages pour l'Interaction Role")
                                .addSubcommands(
                                        new SubcommandData("create", "Create a message on the specified channel.")
                                                .setDescriptionLocalization(DiscordLocale.FRENCH, "Créé un message dans le salon spécifié.")
                                                .addOptions(
                                                        new OptionData(OptionType.CHANNEL, "channel", "Channel used for the interaction roles.", true)
                                                                .setDescriptionLocalization(DiscordLocale.FRENCH, "Salon utilisé pour envoyer le message.")
                                                ),
                                        new SubcommandData("edit", "Edit a message from the specified channel.")
                                                .setDescriptionLocalization(DiscordLocale.FRENCH, "Modifie un message dans un salon spécifié.")
                                                .addOptions(
                                                        new OptionData(OptionType.STRING, "messageid", "The ID of the message that you want to edit.", true)
                                                                .setDescriptionLocalization(DiscordLocale.FRENCH, "L'ID du message que vous voulez modifier."),
                                                        new OptionData(OptionType.STRING, "text", "The content of the edited message.", true)
                                                                .setDescriptionLocalization(DiscordLocale.FRENCH, "Le contenu du message a modifier")
                                                )
                                )
                );
    }

    @Override
    public List<Permission> getBotPermissions() {
        List<Permission> perms = new ArrayList<>();
        Collections.addAll(perms, Permission.MANAGE_ROLES, Permission.MESSAGE_SEND);

        return perms;
    }

    @Override
    public boolean isDev() {
        return false;
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        Guild guild = event.getGuild();
        
        if (!event.getMember().hasPermission(Permission.MANAGE_SERVER)) {
            event.reply(Language.getString("NO_PERMISSION", guild)).setEphemeral(true).queue();
            return;
        }

        String subcommandName = event.getSubcommandName();
        assert subcommandName != null;

        if (event.getSubcommandGroup().equals("message")) {
            if (subcommandName.equals("create")) {
                GuildChannelUnion channelOption = event.getOption("channel").getAsChannel();
                TextChannel channel = channelOption.asTextChannel();

                channel.sendMessage(Language.getString("INTERACTIONROLE_MESSAGE_CREATE_MESSAGE", guild)).queue(message -> {
                            try (Connection connection = Database.getConnection(); PreparedStatement statement = connection.prepareStatement("INSERT INTO interactionrole (channelID, guildID, messageID) VALUES (?,?,?)")) {
                                statement.setString(1, event.getOption("channel", OptionMapping::getAsChannel).getId());
                                statement.setString(2, guild.getId());
                                statement.setString(3, message.getId());

                                statement.executeUpdate();

                                connection.close();

                                event.reply(Language.getString("INTERACTIONROLE_MESSAGE_CREATE_SUCCESS", guild).replace("%MESSAGEID", message.getId())).queue();
                            } catch (SQLException e) {
                                ErrorManager.handle(e, event);
                            }
                });
            }

            if (subcommandName.equals("edit")) {
                try (Connection connection = Database.getConnection(); PreparedStatement statement = connection.prepareStatement("SELECT * FROM interactionrole WHERE messageID = ?")) {
                    statement.setString(1, event.getOption("messageid", OptionMapping::getAsString));

                    ResultSet result = statement.executeQuery();

                    if (!result.first()) {
                        event.reply(Language.getString("INTERACTIONROLE_MESSAGE_EDIT_INVALIDMESSAGE", guild)).setEphemeral(true).queue();
                        connection.close();
                        return;
                    }

                    String channelID = result.getString(1);
                    TextChannel channel = guild.getTextChannelById(channelID);

                    channel.retrieveMessageById(event.getOption("messageid").getAsString()).queue(msg -> {
                        msg.editMessage(event.getOption("text").getAsString()).queue();

                        event.reply(Language.getString("INTERACTIONROLE_MESSAGE_EDIT_EDITED", guild)).queue();
                    });
                } catch (SQLException e) {
                    ErrorManager.handle(e, event);
                }
            }
        }

        if (subcommandName.equals("list")) {
            if (event.getSubcommandGroup().equals("button")) {
                try (Connection connection = Database.getConnection(); PreparedStatement statement = connection.prepareStatement("SELECT * FROM interactionrole_buttons WHERE guildID = ? ORDER BY messageID DESC")) {
                    statement.setString(1, guild.getId());

                    ResultSet result = statement.executeQuery();

                    if (!result.first()) {
                        event.reply(Language.getString("INTERACTIONROLE_LIST_BUTTON_NOBUTTONS", guild)).setEphemeral(true).queue();
                        connection.close();
                        return;
                    }

                    StringBuilder sb = new StringBuilder();

                    while (result.next()) {
                        sb.append(result.getString(2)).append(" - ").append(guild.getRoleById(result.getString(4)).getAsMention()).append("\n");
                    }

                    MessageEmbed embed = new EmbedBuilder()
                            .setDescription(sb.toString())
                            .build();

                    event.replyEmbeds(embed).queue();

                    connection.close();
                } catch (SQLException e) {
                    ErrorManager.handle(e, event);
                }
            }

            if (event.getSubcommandGroup().equals("selectmenu")) {
                try (Connection connection = Database.getConnection(); PreparedStatement statement = connection.prepareStatement("SELECT * FROM interactionrole_selectmenu WHERE guildID = ? ORDER BY messageID DESC")) {
                    statement.setString(1, guild.getId());

                    ResultSet result = statement.executeQuery();

                    if (!result.first()) {
                        event.reply(Language.getString("INTERACTIONROLE_LIST_BUTTON_NOBUTTONS", guild)).setEphemeral(true).queue();
                        connection.close();
                        return;
                    }

                    StringBuilder sb = new StringBuilder();

                    while (result.next()) {
                        sb.append(result.getString(2)).append(" - ").append(guild.getRoleById(result.getString(4)).getAsMention()).append("\n");
                    }

                    MessageEmbed embed = new EmbedBuilder()
                            .setDescription(sb.toString())
                            .build();

                    event.replyEmbeds(embed).queue();

                    connection.close();
                } catch (SQLException e) {
                    ErrorManager.handle(e, event);
                }
            }
        }

        if (subcommandName.equals("set")) {
            if (event.getSubcommandGroup().equals("button")) {
                String messageID = event.getOption("messageid").getAsString();
                Role role = event.getOption("role").getAsRole();
                String buttonName = event.getOption("name").getAsString();

                try (Connection connection = Database.getConnection(); PreparedStatement statement = connection.prepareStatement("SELECT * FROM interactionrole WHERE messageID = ?")) {
                    statement.setString(1, messageID);

                    ResultSet result = statement.executeQuery();

                    if (result.first()) {
                        try (PreparedStatement statement1 = connection.prepareStatement("SELECT * FROM interactionrole_buttons WHERE messageID = ?")) {
                            statement1.setString(1, messageID);

                            ResultSet otherResult = statement1.executeQuery();

                            boolean hasOtherButtons = otherResult.first();

                            TextChannel channel = guild.getTextChannelById(result.getString(1));

                            channel.retrieveMessageById(messageID).queue(msg -> {
                                if (!msg.getActionRows().isEmpty() && msg.getActionRows().get(0).getComponents().get(0).getType().equals(Component.Type.SELECT_MENU)) {
                                    event.reply(Language.getString("INTERACTIONROLE_SET_MAXCOMPONENTROW", guild)).setEphemeral(true).queue();

                                    try {
                                        connection.close();
                                    } catch (SQLException e) {
                                        ErrorManager.handle(e, event);
                                    }

                                    return;
                                }

                                try (PreparedStatement statement2 = connection.prepareStatement("INSERT INTO interactionrole_buttons(guildID, messageID, buttonID, roleID) VALUES (?,?,?,?)")) {
                                    statement2.setString(1, guild.getId());
                                    statement2.setString(2, messageID);
                                    statement2.setString(3, "interactionrole:" + guild.getId() + ":" + role.getId());
                                    statement2.setString(4, role.getId());

                                    statement2.executeUpdate();

                                    if (!hasOtherButtons) {
                                        msg.editMessageComponents().setActionRow(
                                                Button.primary("interactionrole:" + guild.getId() + ":" + role.getId(), buttonName)
                                        ).queue();
                                    } else {
                                        if (msg.getButtons().size() >= 5) {
                                            event.reply(Language.getString("INTERACTIONROLE_SET_BUTTON_MAXBUTTONS", guild)).setEphemeral(true).queue();
                                            connection.close();
                                            return;
                                        }
                                        List<Button> buttons = new ArrayList<>();

                                        msg.getButtons().forEach(button -> buttons.add(Button.primary(button.getId(), button.getLabel())));

                                        buttons.add(Button.primary("interactionrole:" + guild.getId() + ":" + role.getId(), buttonName));

                                        msg.editMessageComponents().setActionRow(buttons).queue();
                                    }
                                    event.reply(Language.getString("INTERACTIONROLE_SET_BUTTON_SUCCESS", guild).replace("%ROLEMENTION%", role.getAsMention())).queue();

                                } catch (SQLException e) {
                                    ErrorManager.handle(e, event);
                                }
                            });
                        }
                    }
                } catch (SQLException e) {
                    ErrorManager.handle(e, event);
                }
            }

            if (event.getSubcommandGroup().equals("selectmenu")) {
                String messageID = event.getOption("messageid").getAsString();

                try (Connection connection = Database.getConnection(); PreparedStatement statement = connection.prepareStatement("SELECT * FROM interactionrole WHERE messageID = ?")) {
                    statement.setString(1, messageID);

                    ResultSet result = statement.executeQuery();

                    if (result.first()) {
                        try (PreparedStatement statement1 = connection.prepareStatement("SELECT * FROM interaction_selectmenu WHERE messageID = ?")) {
                            statement1.setString(1, messageID);

                            ResultSet smResult = statement1.executeQuery();

                            boolean hasOtherMenus = smResult.first();

                            TextChannel channel = guild.getTextChannelById(result.getString(1));

                            channel.retrieveMessageById(messageID).queue(msg -> {
                                if (!msg.getActionRows().isEmpty() && msg.getActionRows().get(0).getComponents().get(0).getType().equals(Component.Type.BUTTON)) {
                                    event.reply(Language.getString("INTERACTIONROLE_SET_MAXCOMPONENTROW", guild)).setEphemeral(true).queue();

                                    try {
                                        connection.close();
                                    } catch (SQLException e) {
                                        ErrorManager.handle(e, event);
                                    }

                                    return;
                                }

                                try (PreparedStatement statement2 = connection.prepareStatement("INSERT INTO interactionrole_selectmenu (messageID, guildID, choiceID, roleID) VALUES (?,?,?,?)")) {
                                    statement2.setString(1, messageID);
                                    statement2.setString(2, guild.getId());
                                    statement2.setString(3, "selectmenurole:" + guild.getId() + ":" + event.getOption("role").getAsRole().getId());
                                    statement2.setString(4, event.getOption("role").getAsRole().getId());

                                    statement2.executeUpdate();

                                    String choiceLabel = event.getOption("name").getAsString();
                                    String choiceValue = ("selectmenurole:" + guild.getId() + ":" + event.getOption("role").getAsRole().getId());
                                    String choiceDescription = event.getOption("description") != null ? event.getOption("description").getAsString() : null;

                                    Emoji emoji;
                                    if (event.getOption("emoji") != null) {
                                        EmojiUnion fromFormattedEmoji = Emoji.fromFormatted(event.getOption("emoji").getAsString());
                                        Emoji.Type emojiType = fromFormattedEmoji.getType();

                                        if (emojiType == Emoji.Type.CUSTOM) emoji = fromFormattedEmoji.asCustom();
                                        else emoji = fromFormattedEmoji.asUnicode();
                                    } else emoji = null;

                                    if (!hasOtherMenus) {
                                        msg.editMessageComponents().setActionRow(
                                                SelectMenu.create("selectmenurole:" + guild.getId())
                                                        .addOption(choiceLabel, choiceValue, choiceDescription, emoji)
                                                        .setMinValues(0)
                                                        .build()
                                        ).queue();
                                    } else {
                                        ActionRow actionRow = msg.getActionRows().get(0);
                                        SelectMenu selectMenu = (SelectMenu) actionRow.getComponents().get(0);

                                        SelectMenu copyMenu = selectMenu.createCopy().build();
                                        SelectMenu.Builder builder = selectMenu.createCopy().addOption(choiceLabel, choiceValue, choiceDescription, emoji).setMaxValues(copyMenu.getOptions().size() + 1).setMinValues(0);

                                        msg.editMessageComponents().setActionRow(builder.build()).queue();

                                    }
                                    event.reply(Language.getString("INTERACTIONROLE_SET_SELECTMENU_SUCCESS", guild)).queue();
                                } catch (SQLException e) {
                                    ErrorManager.handle(e, event);
                                }
                            });
                        }
                    }
                    connection.close();
                } catch (SQLException e) {
                    ErrorManager.handle(e, event);
                }
            }
        }

        if (subcommandName.equals("remove")) {
            if (event.getSubcommandGroup().equals("button")) {
                String messageID = event.getOption("messageid").getAsString();
                Role role = event.getOption("role").getAsRole();

                try (Connection connection = Database.getConnection(); PreparedStatement statement = connection.prepareStatement("SELECT * FROM interactionrole WHERE messageID = ?")) {
                    statement.setString(1, messageID);

                    ResultSet result = statement.executeQuery();

                    if (result.first()) {
                        try (PreparedStatement statement1 = connection.prepareStatement("SELECT * FROM interactionrole_buttons WHERE messageID = ? AND roleID = ?")) {
                            statement1.setString(1, messageID);
                            statement1.setString(2, role.getId());

                            ResultSet otherResult = statement1.executeQuery();

                            if (otherResult.first()) {
                                TextChannel channel = guild.getTextChannelById(result.getString(1));

                                try (PreparedStatement statement2 = connection.prepareStatement("DELETE FROM interactionrole_buttons WHERE messageID = ? AND roleID = ?")) {
                                    statement2.setString(1, messageID);
                                    statement2.setString(2, role.getId());

                                    statement2.executeQuery();

                                    channel.retrieveMessageById(messageID).queue(msg -> {
                                        List<Button> buttons = new ArrayList<>();

                                        msg.getButtons().forEach(button -> {
                                            if (!button.getId().equals("interactionrole:" + guild.getId() + ":" + role.getId()))
                                                buttons.add(Button.primary(button.getId(), button.getLabel()));
                                        });

                                        if (buttons.isEmpty()) msg.editMessageComponents().setComponents().queue();
                                        else msg.editMessageComponents().setComponents(ActionRow.of(buttons)).queue();

                                        event.reply(Language.getString("INTERACTIONROLE_REMOVE_BUTTON_SUCCESS", guild)).queue();
                                    });
                                }
                            }
                        }
                    } else {
                        event.reply(Language.getString("INTERACTIONROLE_REMOVE_INVALIDMESSAGE", guild)).setEphemeral(true).queue();
                    }
                    connection.close();
                } catch (SQLException e) {
                    ErrorManager.handle(e, event);
                }
            }

            if (event.getSubcommandGroup().equals("selectmenu")) {
                String messageID = event.getOption("messageid").getAsString();
                Role role = event.getOption("role").getAsRole();

                try (Connection connection = Database.getConnection(); PreparedStatement statement = connection.prepareStatement("SELECT * FROM interactionrole WHERE messageID = ?")) {
                    statement.setString(1, messageID);

                    ResultSet result = statement.executeQuery();

                    if (result.first()) {
                        try (PreparedStatement statement1 = connection.prepareStatement("SELECT * FROM interactionrole_selectmenu WHERE messageID = ? AND roleID = ?")) {
                            statement1.setString(1, messageID);
                            statement1.setString(2, role.getId());

                            ResultSet smResult = statement1.executeQuery();

                            if (smResult.first()) {
                                TextChannel channel = guild.getTextChannelById(result.getString(1));

                                try (PreparedStatement statement2 = connection.prepareStatement("DELETE FROM interactionrole_selectmenu WHERE messageID = ? AND roleID = ?")) {
                                    statement2.setString(1, messageID);
                                    statement2.setString(2, role.getId());

                                    statement2.executeQuery();

                                    channel.retrieveMessageById(messageID).queue(msg -> {
                                        ActionRow actionRow = msg.getActionRows().get(0);
                                        SelectMenu selectMenu = (SelectMenu) actionRow.getComponents().get(0);

                                        List<SelectOption> selectOptions = new ArrayList<>();

                                        selectMenu.getOptions().forEach(option -> {
                                            if (!option.getValue().equals("selectmenurole:" + guild.getId() + ":" + role.getId()))
                                                selectOptions.add(option);
                                        });

                                        if (selectOptions.isEmpty()) msg.editMessageComponents().setComponents().queue();
                                        else {
                                            SelectMenu newMenu = SelectMenu.create("selectmenurole:" + guild.getId()).addOptions(selectOptions).setMaxValues(selectOptions.size()).build();
                                            msg.editMessageComponents().setActionRow(newMenu).queue();
                                        }

                                        event.reply(Language.getString("INTERACTIONROLE_REMOVE_SELECTMENU_SUCCESS", guild)).queue();
                                    });
                                }
                            }
                        }
                    } else {
                        event.reply(Language.getString("INTERACTIONROLE_REMOVE_INVALIDMESSAGE", guild)).setEphemeral(true).queue();
                    }
                    connection.close();
                } catch (SQLException e) {
                    ErrorManager.handle(e, event);
                }
            }
        }
    }
}