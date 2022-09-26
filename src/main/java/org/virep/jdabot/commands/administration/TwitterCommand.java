package org.virep.jdabot.commands.administration;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.unions.GuildChannelUnion;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.exceptions.ErrorHandler;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.requests.ErrorResponse;
import org.virep.jdabot.Main;
import org.virep.jdabot.slashcommandhandler.Command;
import org.virep.jdabot.utils.DatabaseUtils;

import java.util.EnumSet;

public class TwitterCommand implements Command {
    @Override
    public String getName() {
        return "twitter";
    }

    @Override
    public SlashCommandData getCommandData() {
        return Commands.slash(getName(), "Receive Twitter notifications !")
                .setGuildOnly(true)
                .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.MANAGE_SERVER))
                .addSubcommands(
                        new SubcommandData("add", "Add a Twitter account to Twitter Notifier")
                                .addOptions(
                                        new OptionData(OptionType.CHANNEL, "channel", "The channel to receive notifications.", true),
                                        new OptionData(OptionType.STRING, "account", "The account that should be retrieved.", true)
                                ),
                        new SubcommandData("remove", "Remove an account from Twitter Notifier")
                                .addOptions(
                                        new OptionData(OptionType.STRING, "account", "The account that should be removed.", true)
                                ),
                        new SubcommandData("list", "List all accounts configured in the server for Twitter Notifier.")
                );
    }

    @Override
    public boolean isDev() {
        return false;
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        if (!event.getMember().hasPermission(Permission.MANAGE_SERVER)) {
            event.reply("You do not have permission to use this command.").setEphemeral(true).queue();
            return;
        }

        ErrorHandler errorHandler = new ErrorHandler()
                .handle(EnumSet.of(ErrorResponse.MISSING_PERMISSIONS),
                        (ex) -> event.reply("The webhook could not be created because of permission discrepancy.").setEphemeral(true).queue())
                .handle(EnumSet.of(ErrorResponse.MISSING_ACCESS),
                        (ex) -> event.reply("The webhook could not be created because the channel is not viewable.").setEphemeral(true).queue())
                .handle(EnumSet.of(ErrorResponse.MAX_WEBHOOKS),
                        (ex) -> event.reply("The webhook could not be created, as this channel has reached the maximum webhook limit.").setEphemeral(true).queue());

        if (event.getSubcommandName().equals("remove")) {
            String twitterUser = event.getOption("account", OptionMapping::getAsString);

            DatabaseUtils.removeTwitterWebhook(event.getGuild().getId(), twitterUser);

            event.reply("Twitter Notifier deleted for " + twitterUser).queue();

            if (Main.getInstance().getNotifier().isTwitterRegistered(twitterUser)) {
                Main.getInstance().getNotifier().unregisterTwitterUser(twitterUser);
            }
        }

        if (event.getSubcommandName().equals("add")) {
            GuildChannelUnion channel = event.getOption("channel", OptionMapping::getAsChannel);
            String twitterUser = event.getOption("account", OptionMapping::getAsString);

            if (channel.getType() != ChannelType.TEXT) {
                event.reply("Please specify a TEXT channel.").setEphemeral(true).queue();
                return;
            }

            channel.asTextChannel().createWebhook("Twitter Notifier (" + twitterUser + ")").queue(webhook -> {
                DatabaseUtils.addTwitterWebhook(channel.getId(), event.getGuild().getId(), webhook.getUrl(), twitterUser);

                event.reply("Twitter Notifier created for " + twitterUser).queue();

                if (Main.getInstance().getNotifier().isTwitterRegistered(twitterUser)) {
                    Main.getInstance().getNotifier().registerTwitterUser(twitterUser);
                }
            }, errorHandler);
        }

        if (event.getSubcommandName().equals("list")) {
            StringBuilder string = new StringBuilder("```\n");

            for (String users : DatabaseUtils.getAllTwitterNames(event.getGuild().getId())) {
                string.append(users).append("\n");
            }

            string.append("```");

            event.reply(string.toString()).queue();
        }
    }
}
