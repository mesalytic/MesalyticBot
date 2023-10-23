package org.virep.jdabot.textcommands;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import org.virep.jdabot.database.Database;
import org.virep.jdabot.handlers.TextCommand;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class RegisterCommand implements TextCommand {
    @Override
    public String getName() {
        return "register";
    }
    @Override
    public boolean isDev() {
        return true;
    }

    @Override
    public void execute(MessageReceivedEvent event, String[] args) throws SQLException {
        Guild guild = event.getGuild();
        Message message = event.getMessage();

        try (Connection connection = Database.getConnection(); PreparedStatement statement = connection.prepareStatement("SELECT * FROM game_profile WHERE userID = ?")) {
            statement.setString(1, event.getAuthor().getId());

            ResultSet result = statement.executeQuery();

            if (result.first()) {
                message.reply("""
                        **Rift Pulse: Welcome Back, Netrunner!**
                                                
                        Greetings, seasoned Netrunner!
                        
                        Your presence in the Ciphered Nexus is recognized.
                        You stand as a stalwart guardian of digital secrets and a master of the virtual realm.
                                                
                        As you continue your journey through the world of Rift Pulse, remember that your choices carry weight and your skills are invaluable.
                        Dive deeper into the enigmatic missions, uncover hidden truths, and interact with the intricate web of characters that populate our digital universe.
                                                
                        Should you need assistance or wish to embark on a new adventure, type `!help` for available commands or `!missions` to explore ongoing tasks.
                                                
                        May your code be flawless, and your path in the Nexus be ever thrilling. Happy hacking, Netrunner!
                        """).queue();

                return;
            }

            message.reply("""
**Rift Pulse: Registration and Rules**

Welcome to **Rift Pulse: Chronicles of the Ciphered Nexus**!
In the depths of the digital abyss, where algorithms dance and secrets lie encoded, you stand on the precipice of a world veiled in shadows.

Rift Pulse is not just a game; it is an odyssey through the enigmatic corridors of cyberspace. Here, amidst the neon-lit networks and encrypted databases, you'll craft your destiny as a Netrunner, a digital adept skilled in the art of hacking.
Prepare to traverse a universe where hackers and corporations engage in an eternal dance of intrigue, where every keystroke could shape the fate of nations.
In this vast digital expanse, the line between reality and virtuality blurs, and the choices you make echo across the digital ether.

Before you enter this intricate realm of digital enigmas, we invite you to embark on a journey that transcends mere lines of code.
Embark on thrilling missions that challenge your intellect, navigate complex webs of conspiracy, and uncover secrets that hold the fate of worlds.

Alongside fellow Netrunners, you will confront corporate titans, decipher cryptic puzzles, and decide the destinies of digital empires.

The Ciphered Nexus awaits your presence, Netrunner.
Will you be the one to uncover the ultimate truth, or will you succumb to the shadows? 

Your adventure begins now.

Would you like to register and venture into this captivating world?
                    """)
                    .addActionRow(
                            Button.success("button:register:accept", "Register"),
                            Button.danger("button:register:cancel", "Cancel")
                    ).queue();
        }

//        message.reply().addComponents()

        /*try (Connection connection = Database.getConnection(); PreparedStatement statement = connection.prepareStatement("SELECT * FROM inventory WHERE userID = ?")) {
            statement.setString(1, event.getAuthor().getId());

            ResultSet result = statement.executeQuery();

            if (result.first()) {
                message.reply(Language.getString("REGISTER_ALREADY_REGISTERED", guild)).queue();

                return;
            }

            PreparedStatement insertStatement = connection.prepareStatement("INSERT INTO inventory (userID) VALUES (?)");
            insertStatement.setString(1, event.getAuthor().getId());

            insertStatement.executeUpdate();

            message.reply(Language.getString("REGISTER_REGISTERED", guild)).queue();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }*/
    }
}
