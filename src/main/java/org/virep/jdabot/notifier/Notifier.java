package org.virep.jdabot.notifier;

import club.minnced.discord.webhook.WebhookClient;
import club.minnced.discord.webhook.send.WebhookEmbed;
import club.minnced.discord.webhook.send.WebhookEmbedBuilder;
import club.minnced.discord.webhook.send.WebhookMessageBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.virep.jdabot.utils.Config;
import org.virep.jdabot.utils.DatabaseUtils;
import twitter4j.*;
import twitter4j.conf.ConfigurationBuilder;

import java.awt.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class Notifier implements StatusListener {
    private final static Logger log = LoggerFactory.getLogger(Notifier.class);
    private final Twitter twitterClient;
    private TwitterStream twitterStream;
    private FilterQuery filterQuery = new FilterQuery();
    private List<String> filteredUsers = new ArrayList<>();

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

    public Twitter getTwitterClient() {
        return twitterClient;
    }

    public TwitterStream getTwitterStream() {
        return twitterStream;
    }

    public void initialize() {
        List<String> users = DatabaseUtils.getAllTwitterNames();
        if (users.isEmpty()) {
            log.info("no twitter names registered !");
        } else {
            streamInitialization();

            users.forEach(user -> {
                log.info("Adding {} to stream", user);
                addUserToFilter(user, false);
            });

            launchStream(false);

            System.out.println(filterQuery.toString());
        }
    }

    public void streamInitialization() {
        twitterStream = new TwitterStreamFactory(getTwitterClient().getConfiguration()).getInstance();
        twitterStream.addListener(this);
    }

    public void launchStream(boolean shutdown) {
        if (shutdown) {
            twitterStream.cleanUp();

            streamInitialization();
        }

        twitterStream.filter(filterQuery);
    }

    public void addUserToFilter(String twitterUser, boolean update) {
        long[] userIDs = null;
        ArrayList<Long> userIDObj = new ArrayList<>();

        if (!isUserFiltered(twitterUser)) filteredUsers.add(twitterUser);

        filteredUsers.forEach(filteredUser -> {
            User user;

            try {
                user = getTwitterClient().showUser(filteredUser);

                if (!user.isProtected()) {
                    userIDObj.add(user.getId());
                }
            } catch (TwitterException e) {
                e.printStackTrace();
            }
        });

        userIDs = new long[userIDObj.size()];
        for (int i = 0; i < userIDObj.size(); i++) {
            userIDs[i] = userIDObj.get(i);
        }

        filterQuery.follow(userIDs);

        if (update) {
            launchStream(true);
            System.out.println(filterQuery.toString());
        }
    }

    public void removeUserFromFilter(String twitterUser, boolean update) {
        long[] userIDs = null;
        ArrayList<Long> userIDObj = new ArrayList<>();

        if (isUserFiltered(twitterUser)) filteredUsers.remove(twitterUser);

        filteredUsers.forEach(filteredUser -> {
            User user;

            try {
                user = getTwitterClient().showUser(filteredUser);

                if (!user.isProtected()) {
                    userIDObj.add(user.getId());
                }
            } catch (TwitterException e) {
                e.printStackTrace();
            }
        });

        userIDs = new long[userIDObj.size()];
        for (int i = 0; i < userIDObj.size(); i++) {
            userIDs[i] = userIDObj.get(i);
        }

        filterQuery.follow(userIDs);

        if (update) {
            launchStream(true);
            System.out.println(filterQuery.toString());
        }
    }

    public boolean isUserFiltered(String twitterUser) {
        return filteredUsers.contains(twitterUser);
    }

    @Override
    public void onStatus(Status status) {
        log.debug("Spotted tweet from {}", status.getUser().getScreenName());

        List<String> webhooks = DatabaseUtils.getTwitterWebhookByName(status.getUser().getScreenName());
        if (webhooks.isEmpty()) return;

        webhooks.forEach(webhook -> {
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

            WebhookClient.withUrl(webhook).send(webhookMessageBuilder.build());
        });
    }

    @Override
    public void onDeletionNotice(StatusDeletionNotice statusDeletionNotice) {

    }

    @Override
    public void onTrackLimitationNotice(int i) {

    }

    @Override
    public void onScrubGeo(long l, long l1) {

    }

    @Override
    public void onStallWarning(StallWarning stallWarning) {

    }

    @Override
    public void onException(Exception e) {

    }
}
