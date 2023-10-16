package org.virep.jdabot.handlers;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.sql.SQLException;

public interface TextCommand {
    public String getName();

    public boolean isDev();

    public void execute(MessageReceivedEvent event, String[] args) throws SQLException;
}
