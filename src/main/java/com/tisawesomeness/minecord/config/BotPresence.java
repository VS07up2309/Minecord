package com.tisawesomeness.minecord.config;

import com.tisawesomeness.minecord.util.DiscordUtils;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.sharding.ShardManager;
import org.json.JSONObject;

import javax.annotation.Nullable;

/**
 * An extension of {@link Activity} that can have bot variables and constants.
 */
@ToString
@RequiredArgsConstructor
public class BotPresence {
    private final @NonNull OnlineStatus status;
    private final @Nullable Activity.ActivityType type;
    private final @Nullable String content;
    private final @Nullable String url;

    /**
     * Creates a new presence from the JSON input with parsed constants.
     * @param obj A JSONObject with {@code type} and {@code content} fields.
     * @param config The config file used to get constants.
     */
    public BotPresence(@NonNull JSONObject obj, @NonNull Config config) {
        status = parseStatus(obj.optString("status"));
        type = parseType(obj.getString("type"));
        if (type == null) {
            content = null;
        } else {
            String parsedContent = obj.optString("content");
            if (parsedContent == null) {
                throw new IllegalArgumentException("A " + type + " presence must have a content field");
            }
            content = DiscordUtils.parseConstants(parsedContent, config);
        }
        url = obj.optString("url");
    }

    public static BotPresence defaultPresence() {
        return new BotPresence(OnlineStatus.ONLINE, null, null, null);
    }

    private static @Nullable Activity.ActivityType parseType(String str) {
        if ("playing".equalsIgnoreCase(str)) {
            return Activity.ActivityType.DEFAULT;
        } else if ("streaming".equalsIgnoreCase(str)) {
            return Activity.ActivityType.STREAMING;
        } else if ("listening".equalsIgnoreCase(str)) {
            return Activity.ActivityType.LISTENING;
        }
        return null;
    }

    private static OnlineStatus parseStatus(String str) {
        OnlineStatus status = OnlineStatus.fromKey(str);
        return status == OnlineStatus.UNKNOWN ? OnlineStatus.ONLINE : status;
    }

    public boolean hasPresence() {
        return type != null;
    }

    /**
     * Changes the bot's status to this presence.
     * @param sm The ShardManager to pull variables from
     */
    public void setPresence(@NonNull ShardManager sm) {
        if (type == null) {
            sm.setPresence(status, null);
            return;
        }
        Activity jdaActivity = Activity.of(type, DiscordUtils.parseVariables(content, sm), url);
        sm.setPresence(status, jdaActivity);
    }
}
