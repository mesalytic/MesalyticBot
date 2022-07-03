package org.virep.jdabot.listeners;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.audio.hooks.ConnectionStatus;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.ShutdownEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.managers.AudioManager;
import org.virep.jdabot.Main;
import org.virep.jdabot.lavaplayer.GuildAudioManager;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class EventListener extends ListenerAdapter {

    @Override
    public void onShutdown(ShutdownEvent event) {
        JDA jda = event.getJDA();

        if (jda.getStatus() != JDA.Status.SHUTTING_DOWN) {
            jda.getGuilds().forEach(g -> {
                AudioManager manager = g.getAudioManager();
                manager.getConnectionStatus();
                if (manager.getConnectionStatus() == ConnectionStatus.CONNECTED) {
                    g.getAudioManager().closeAudioConnection();

                    GuildAudioManager gam = new GuildAudioManager(g);
                    gam.getPlayer().getLink().destroy();

                }
            });
            jda.shutdown();
        }

        System.out.println("Shutdown !");
        System.exit(0);
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (event.getAuthor().isBot()) return;

        Message message = event.getMessage();
        String content = message.getContentRaw();

        if (content.equals("jda!shutdown")) {
            JDA jda = event.getJDA();

            if (jda.getStatus() != JDA.Status.SHUTTING_DOWN) {
                jda.getGuilds().forEach(g -> {
                    g.getAudioManager().closeAudioConnection();

                    GuildAudioManager gam = new GuildAudioManager(g);
                    gam.getPlayer().getLink().destroy();
                });

                event.getMessage().reply("Shutdown en cours").queue();

                jda.shutdown();
            }
        }
    }

    @Override
    public void onGuildMemberJoin(GuildMemberJoinEvent event) {
        System.out.println("Event triggered!");
        try (PreparedStatement statement = Main.connectionDB.prepareStatement("SELECT * FROM autorole WHERE guildID = ?")) {
            System.out.println("Triggered 2");
            Guild guild = event.getGuild();
            Member member = event.getMember();

            statement.setString(1, guild.getId());

            ResultSet result = statement.executeQuery();

            if (result.next()) {
                System.out.println("Triggered 3");
                String roleID = result.getString(1);
                Role role = guild.getRoleById(roleID);

                assert role != null;
                System.out.println(role.getName());

                event.getGuild().addRoleToMember(member, role).queue();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
