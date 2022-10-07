package org.virep.jdabot.external;

import club.minnced.discord.webhook.WebhookClient;
import club.minnced.discord.webhook.send.WebhookEmbed;
import club.minnced.discord.webhook.send.WebhookEmbedBuilder;
import club.minnced.discord.webhook.send.WebhookMessageBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.virep.jdabot.utils.Config;
import org.virep.jdabot.utils.DatabaseUtils;
import org.virep.jdabot.utils.ErrorManager;
import twitter4j.*;
import twitter4j.conf.ConfigurationBuilder;

import java.awt.*;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Notifier {
    private final static Logger log = LoggerFactory.getLogger(Notifier.class);
    private final Twitter twitterClient;
    private final Map<String, TwitterStream> registeredTwitterUsers = new HashMap<>();

    public Notifier() {
        ConfigurationBuilder configurationBuilder = new ConfigurationBuilder();

        configurationBuilder
                .setOAuthConsumerKey(Config.get("TWITTER_CONSUMER_KEY"))
                .setOAuthConsumerSecret(Config.get("TWITTER_CONSUMER_SECRET"))
                .setOAuthAccessToken(Config.get("TWITTER_ACCESS_TOKEN"))
                .setOAuthAccessTokenSecret(Config.get("TWITTER_ACCESS_SECRET"))
                .setDebugEnabled(true);

        twitterClient = new TwitterFactory(configurationBuilder.build()).getInstance();

        log.info("Configured Notifier");
    }

    public void registerTwitterUser(List<String> twitterUsers) {
        twitterUsers.forEach(this::registerTwitterUser);
    }

    public boolean registerTwitterUser(String twitterUser) {
        log.info(String.format("Registering %s", twitterUser));
        if (getTwitterClient() == null) return false;

        twitterUser = twitterUser.toLowerCase();

        User user;

        try {
            user = getTwitterClient().showUser(twitterUser);
            if (user.isProtected()) return false;
        } catch (Exception ignore) {
            return false;
        }

        FilterQuery filterQuery = new FilterQuery();
        filterQuery.follow(user.getId());

        User finalUser = user;
        TwitterStream twitterStream = new TwitterStreamFactory(getTwitterClient().getConfiguration()).getInstance().addListener(new StatusListener() {
            @Override
            public void onStatus(Status status) {
                log.debug("spotted tweet from " + status.getUser().getScreenName());

                WebhookMessageBuilder webhookMessageBuilder = new WebhookMessageBuilder();

                webhookMessageBuilder.setUsername(status.getUser().getScreenName());
                webhookMessageBuilder.setAvatarUrl(status.getUser().getBiggerProfileImageURLHttps());

                WebhookEmbedBuilder webhookEmbedBuilder = new WebhookEmbedBuilder();

                webhookEmbedBuilder.setAuthor(new WebhookEmbed.EmbedAuthor(status.getUser().getName() + " (@" + status.getUser().getScreenName() + ")", status.getUser().getBiggerProfileImageURLHttps(), null));

                webhookEmbedBuilder.setDescription(status.getQuotedStatus() != null && !status.isRetweet() ? "**Quoted  " + status.getQuotedStatus().getUser().getScreenName() + "**: " + status.getText() + "\n" : status.getInReplyToScreenName() != null ? "**Reply to " + status.getInReplyToScreenName() + "**: " + status.getText() + "\n" : status.isRetweet() ? "**Retweeted from " + status.getRetweetedStatus().getUser().getScreenName() + "**: " + status.getText().split(": ")[1] + "\n" : status.getText() + "\n");

                if (status.getMediaEntities().length > 0 && status.getMediaEntities()[0].getType().equalsIgnoreCase("photo")) {
                    webhookEmbedBuilder.setImageUrl(status.getMediaEntities()[0].getMediaURLHttps());
                }

                webhookEmbedBuilder.setFooter(new WebhookEmbed.EmbedFooter("Sent via Mesalytic", null));
                webhookEmbedBuilder.setTimestamp(Instant.now());
                webhookEmbedBuilder.setColor(Color.CYAN.getRGB());

                webhookMessageBuilder.addEmbeds(webhookEmbedBuilder.build());

                List<String> webhooks = DatabaseUtils.getTwitterWebhookByName(status.getUser().getScreenName());

                if (!webhooks.isEmpty()) webhooks.forEach(webhook -> {
                    WebhookClient.withUrl(webhook).send(webhookMessageBuilder.build());
                });
            }

            @Override
            public void onDeletionNotice(StatusDeletionNotice statusDeletionNotice) {

            }

            @Override
            public void onTrackLimitationNotice(int numberOfLimitedStatuses) {

            }

            @Override
            public void onScrubGeo(long userId, long upToStatusId) {

            }

            @Override
            public void onStallWarning(StallWarning warning) {

            }

            @Override
            public void onException(Exception ex) {
                ErrorManager.handleNoEvent(ex);
            }
        }).filter(filterQuery);

        if (!isTwitterRegistered(twitterUser)) registeredTwitterUsers.put(twitterUser, twitterStream);

        return true;
    }

    public void unregisterTwitterUser(String twitterUser) {
        if (getTwitterClient() == null) return;

        twitterUser = twitterUser.toLowerCase();

        if (DatabaseUtils.getTwitterWebhookByName(twitterUser).isEmpty()) return;

        if (isTwitterRegistered(twitterUser)) {
            registeredTwitterUsers.get(twitterUser).cleanUp();

            registeredTwitterUsers.remove(twitterUser);
        }
    }

    public boolean isTwitterRegistered(String twitterUser) {
        return registeredTwitterUsers.containsKey(twitterUser.toLowerCase());
    }

    public Twitter getTwitterClient() {
        return twitterClient;
    }
}
