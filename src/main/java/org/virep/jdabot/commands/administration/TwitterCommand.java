package org.virep.jdabot.commands.administration;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.unions.GuildChannelUnion;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.exceptions.ErrorHandler;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.requests.ErrorResponse;
import org.virep.jdabot.Main;
import org.virep.jdabot.language.Language;
import org.virep.jdabot.notifier.Notifier;
import org.virep.jdabot.slashcommandhandler.Command;
import org.virep.jdabot.utils.DatabaseUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;

public class TwitterCommand implements Command {
    @Override
    public String getName() {
        return "twitter";
    }

    @Override
    public SlashCommandData getCommandData() {
        return Commands.slash(getName(), "Receive Twitter notifications !")
                .setDescriptionLocalization(DiscordLocale.FRENCH, "Recevez des notifications Twitter !")
                .setGuildOnly(true)
                .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.MANAGE_SERVER))
                .addSubcommands(
                        new SubcommandData("add", "Add a Twitter account to Twitter Notifier")
                                .setDescriptionLocalization(DiscordLocale.FRENCH, "Ajoute un compte Twitter aux Notifications Twitter")
                                .addOptions(
                                        new OptionData(OptionType.CHANNEL, "channel", "The channel to receive notifications.", true)
                                                .setDescriptionLocalization(DiscordLocale.FRENCH, "Le salon qui recevra les notifications."),
                                        new OptionData(OptionType.STRING, "account", "The account that should be retrieved.", true)
                                                .setDescriptionLocalization(DiscordLocale.FRENCH, "Le compte Twitter qui doit être ajouté.")
                                ),
                        new SubcommandData("remove", "Remove an account from Twitter Notifier")
                                .setDescriptionLocalization(DiscordLocale.FRENCH, "Retire un compte des Notifications Twitter")
                                .addOptions(
                                        new OptionData(OptionType.STRING, "account", "The account that should be removed.", true)
                                                .setDescriptionLocalization(DiscordLocale.FRENCH, "Le compte Twitter qui doit être retiré.")
                                ),
                        new SubcommandData("list", "List all accounts configured in the server for Twitter Notifier.")
                                .setDescriptionLocalization(DiscordLocale.FRENCH, "Liste les comptes configurés aux Notifications Twitter sur le serveur.")
                );
    }

    @Override
    public List<Permission> getBotPermissions() {
        List<Permission> perms = new ArrayList<>();
        Collections.addAll(perms, Permission.MESSAGE_EMBED_LINKS);

        return perms;
    }

    @Override
    public boolean isDev() {
        return false;
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        Guild guild = event.getGuild();
        Notifier notifier = Main.getInstance().getNotifier();

        if (!event.getMember().hasPermission(Permission.MANAGE_SERVER)) {
            event.reply(Language.getString("NO_PERMISSION", guild)).setEphemeral(true).queue();
            return;
        }

        ErrorHandler errorHandler = new ErrorHandler()
                .handle(EnumSet.of(ErrorResponse.MISSING_PERMISSIONS),
                        (ex) -> event.reply(Language.getString("ERRORHANDLER_MISSINGPERMISSIONS", guild)).setEphemeral(true).queue())
                .handle(EnumSet.of(ErrorResponse.MISSING_ACCESS),
                        (ex) -> event.reply(Language.getString("ERRORHANDLER_MISSINGACCESS", guild)).setEphemeral(true).queue())
                .handle(EnumSet.of(ErrorResponse.MAX_WEBHOOKS),
                        (ex) -> event.reply(Language.getString("ERRORHANDLER_MAXWEBHOOKS", guild)).setEphemeral(true).queue());

        if (event.getSubcommandName().equals("remove")) {
            String twitterUser = event.getOption("account", OptionMapping::getAsString);

            DatabaseUtils.removeTwitterWebhook(guild.getId(), twitterUser);

            event.reply(Language.getString("TWITTER_REMOVE_DELETED", guild).replaceAll("%TWITTERUSER%", twitterUser)).queue();

            if (notifier.isUserFiltered(twitterUser)) {
                notifier.removeUserFromStream(twitterUser, notifier.getTwitterStream());
            }
        }

        if (event.getSubcommandName().equals("add")) {
            GuildChannelUnion channel = event.getOption("channel", OptionMapping::getAsChannel);
            String twitterUser = event.getOption("account", OptionMapping::getAsString);

            if (channel.getType() != ChannelType.TEXT) {
                event.reply(Language.getString("TWITTER_ADD_WRONGCHANNEL", guild)).setEphemeral(true).queue();
                return;
            }

            if (DatabaseUtils.hasWebhook(channel.getId())) {
                String webhook = DatabaseUtils.getTwitterWebhookFromChannel(channel.getId());

                DatabaseUtils.addTwitterWebhook(channel.getId(), guild.getId(), webhook, twitterUser);

                event.reply(Language.getString("TWITTER_REMOVE_ADDED", guild).replaceAll("%TWITTERUSER%", twitterUser)).queue();

                if (!notifier.isUserFiltered(twitterUser)) {
                    notifier.addUserToStream(twitterUser, notifier.getTwitterStream());
                }
            } else {
                channel.asTextChannel().createWebhook("Twitter Notifier (" + twitterUser + ")").queue(webhook -> {
                    DatabaseUtils.addTwitterWebhook(channel.getId(), guild.getId(), webhook.getUrl(), twitterUser);

                    event.reply(Language.getString("TWITTER_REMOVE_ADDED", guild).replaceAll("%TWITTERUSER%", twitterUser)).queue();

                    if (!notifier.isUserFiltered(twitterUser)) {
                        notifier.addUserToStream(twitterUser, notifier.getTwitterStream());
                    }
                }, errorHandler);
            }
        }

        if (event.getSubcommandName().equals("list")) {
            StringBuilder string = new StringBuilder("```\n");

            for (String users : DatabaseUtils.getAllTwitterNames(guild.getId())) {
                string.append(users).append("\n");
            }

            string.append("```");

            event.reply(string.toString()).queue();
        }
    }
}
