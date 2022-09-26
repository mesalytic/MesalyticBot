package org.virep.jdabot.commands.moderation;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import org.virep.jdabot.slashcommandhandler.Command;

import java.util.List;
import java.util.Objects;

public class PurgeCommand implements Command {
    @Override
    public String getName() {
        return "purge";
    }

    @Override
    public SlashCommandData getCommandData() {
        return Commands.slash(getName(), "Bulk delete messages in the channel. You can specify a channel.")
                .setGuildOnly(true)
                .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.MESSAGE_MANAGE))
                .addSubcommands(
                        new SubcommandData("any", "Delete any message")
                                .addOptions(
                                        new OptionData(OptionType.INTEGER, "amount", "The amount of messages to delete (max 100)", true).setMaxValue(100),
                                        new OptionData(OptionType.CHANNEL, "channel", "The channel where to delete the messages.")
                                ),
                        new SubcommandData("bots", "Delete messages from bots (from the latest 500 messages).")
                                .addOptions(
                                        new OptionData(OptionType.INTEGER, "amount", "The amount of messages to delete (max 100)", true).setMaxValue(100),
                                        new OptionData(OptionType.CHANNEL, "channel", "The channel where to delete the messages.")
                                ),
                        new SubcommandData("user", "Delete messages from specific user (from the latest 500 messages).")
                                .addOptions(
                                        new OptionData(OptionType.INTEGER, "amount", "The amount of messages to delete (max 100)", true).setMaxValue(100),
                                        new OptionData(OptionType.USER, "user", "The author of the messages", true),
                                        new OptionData(OptionType.CHANNEL, "channel", "The channel where to delete the messages.")
                                )
                );
    }

    @Override
    public boolean isDev() {
        return false;
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        if (!Objects.requireNonNull(event.getMember()).hasPermission(Permission.MESSAGE_MANAGE)) {
            event.reply("You do not have permission to use this command.").setEphemeral(true).queue();
            return;
        }

        OptionMapping channelOption = event.getOption("channel");
        OptionMapping amountOption = event.getOption("amount");

        assert amountOption != null;
        int amount = amountOption.getAsInt();

        TextChannel channel = channelOption != null ? channelOption.getAsChannel().asTextChannel() : event.getChannel().asTextChannel();

        if (Objects.equals(event.getSubcommandName(), "any")) {
            channel.getIterableHistory().takeAsync(amount)
                    .thenAccept(list -> {
                        channel.purgeMessages(list);

                        event.reply("**" + amount + " messages have been deleted !").queue();
                    });
        }

        if (event.getSubcommandName().equals("bots")) {
            event.reply("Loading messages, please wait...").queue();

            channel.getIterableHistory().takeAsync(500)
                    .thenAccept(list -> {
                        List<Message> sortedList = list.stream().filter(m -> m.getAuthor().isBot()).limit(amount).toList();

                        channel.purgeMessages(sortedList);

                        event.getHook().editOriginal("**" + amount + " messages have been deleted !").queue();
                    });
        }

        if (event.getSubcommandName().equals("user")) {
            OptionMapping userOption = event.getOption("user");

            assert userOption != null;
            User user = userOption.getAsUser();
            event.reply("Loading messages, please wait...").queue();

            channel.getIterableHistory().takeAsync(500)
                    .thenAccept(list -> {
                        List<Message> sortedList = list.stream().filter(m -> m.getAuthor().getId().equals(user.getId())).limit(amount).toList();

                        channel.purgeMessages(sortedList);

                        event.getHook().editOriginal("**" + amount + " messages have been deleted !").queue();
                    });
        }
    }
}
