package org.virep.jdabot.external;

import com.twitter.clientlib.ApiException;
import com.twitter.clientlib.TwitterCredentialsBearer;
import com.twitter.clientlib.api.TwitterApi;
import com.twitter.clientlib.model.AddOrDeleteRulesRequest;
import com.twitter.clientlib.model.AddOrDeleteRulesResponse;
import com.twitter.clientlib.model.DeleteRulesRequest;
import com.twitter.clientlib.model.DeleteRulesRequestDelete;
import com.twitter.clientlib.model.FilteredStreamingTweetResponse;
import com.twitter.clientlib.model.RuleNoId;
import org.json.JSONArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.virep.jdabot.utils.Config;
import org.virep.jdabot.utils.DatabaseUtils;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class Notifier {
    private final static Logger log = LoggerFactory.getLogger(Notifier.class);
    private final TwitterApi twitterClient;
    private final Map<String, RuleNoId> registeredRules = new HashMap<>();

    public Notifier() {
        TwitterCredentialsBearer credentials = new TwitterCredentialsBearer(Config.get("TWITTER_BEARER"));

        twitterClient = new TwitterApi(credentials);

        log.info("Configured Twitter Notifier");
    }

    public boolean isTwitterUserRegistered(String twitterUser) {
        return registeredRules.containsKey("from:" + twitterUser.toLowerCase());
    }

    public TwitterApi getTwitterClient() {
        return twitterClient;
    }
}
