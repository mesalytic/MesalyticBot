package org.virep.jdabot.commands.mining;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import org.virep.jdabot.Main;
import org.virep.jdabot.slashcommandhandler.SlashCommand;

import java.io.FileNotFoundException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static org.virep.jdabot.utils.MiningCollections.getChoices;
import static org.virep.jdabot.utils.MiningCollections.getCollectionProgression;

public class MiningCommand extends SlashCommand {

    public MiningCommand() throws FileNotFoundException {
        super("mining", "mining", "wip",
                new SubcommandData[] {
                        new SubcommandData("profile", "profile"),
                        new SubcommandData("start", "start"),
                        new SubcommandData("collections", "collections").addOptions(
                                new OptionData(OptionType.STRING, "collectionname", "Collection name", true).addChoices(getChoices())
                        )
                });
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        assert event.getSubcommandName() != null;

        if (event.getSubcommandName().equals("start")) {
            try (PreparedStatement profileStatement = Main.connectionDB.prepareStatement("SELECT * FROM miningProfile WHERE userID = ?")) {
                profileStatement.setString(1, event.getUser().getId());

                ResultSet result = profileStatement.executeQuery();

                if (!result.first()) {
                    try(PreparedStatement insertStatement1 = Main.connectionDB.prepareStatement("""
                        INSERT INTO miningProfile(userID)
                        VALUES (?)
                        """)) {
                        try(PreparedStatement insertStatement2 = Main.connectionDB.prepareStatement("""
                            INSERT INTO miningCollectionsAmount(userID)
                            VALUES (?)
                            """)) {
                            try(PreparedStatement insertStatement3 = Main.connectionDB.prepareStatement("""
                            INSERT INTO miningCollectionsLevels(userID)
                            VALUES (?)
                            """)) {
                                insertStatement1.setString(1, event.getUser().getId());
                                insertStatement2.setString(1, event.getUser().getId());
                                insertStatement3.setString(1, event.getUser().getId());

                                insertStatement1.executeUpdate();
                                insertStatement2.executeUpdate();
                                insertStatement3.executeUpdate();
                                event.reply("Your mining account has successfully been created! To learn more, use the `/mining tutorial` command !").setEphemeral(true).queue();
                                return;
                            } catch (SQLException e) {
                                throw new RuntimeException(e);
                            }
                        } catch (SQLException e) {
                            throw new RuntimeException(e);
                        }
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                } else {
                    event.reply("You already have an account! To learn more, use the `/mining tutorial` command.").setEphemeral(true).queue();
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }

        if (event.getSubcommandName().equals("profile")) {
            try (PreparedStatement statement = Main.connectionDB.prepareStatement("SELECT * FROM miningProfile WHERE userID=?")) {
                statement.setString(1, event.getUser().getId());

                ResultSet result = statement.executeQuery();

                if (!result.first()) {
                    event.reply("You don't have an account ! Use the `/mining start` to create an account !").setEphemeral(true).queue();
                    return;
                }

                MessageEmbed embed = new EmbedBuilder()
                        .setTitle("Mining profile of " + event.getUser().getAsTag())
                        .setDescription("__**Mana**__: %d/%d\n__**XP/LVL**__: Level %d (%d)\n\n__**Pickaxe**__: %s\n__**Current World**__: %s".formatted(result.getInt(2), result.getInt(3), result.getInt(4), result.getInt(5), result.getString(6), result.getString(7)))
                        .build();

                event.replyEmbeds(embed).queue();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }

        if (event.getSubcommandName().equals("collections")) {
            try (PreparedStatement statement = Main.connectionDB.prepareStatement("SELECT * FROM miningProfile WHERE userID=?")) {
                statement.setString(1, event.getUser().getId());

                ResultSet result = statement.executeQuery();

                if (!result.first()) {
                    event.reply("You don't have an account ! Use the `/mining start` to create an account !").setEphemeral(true).queue();
                    return;
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
            try {
                OptionMapping collectionName = event.getOption("collectionname");
                assert collectionName != null;

                event.reply(getCollectionProgression(collectionName.getAsString(), event.getUser().getId())).queue();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
}
