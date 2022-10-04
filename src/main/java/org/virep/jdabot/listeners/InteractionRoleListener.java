package org.virep.jdabot.listeners;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.SelectMenuInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;
import org.virep.jdabot.language.Language;

import java.util.List;

public class InteractionRoleListener extends ListenerAdapter {
    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        Guild guild = event.getGuild();

        String buttonID = event.getButton().getId();

        if (buttonID.startsWith("interactionrole")) {
            String[] args = buttonID.split(":");

            String roleID = args[2];

            Role role = guild.getRoleById(roleID);

            if (event.getMember().getRoles().contains(role)) {
                guild.removeRoleFromMember(event.getUser(), role).queue();
                event.reply(Language.getString("INTERACTIONROLELISTENER_REMOVED", guild)).setEphemeral(true).queue();
            } else {
                guild.addRoleToMember(event.getUser(), role).queue();
                event.reply(Language.getString("INTERACTIONROLELISTENER_ADDED", guild)).setEphemeral(true).queue();
            }


        }
    }

    @Override
    public void onSelectMenuInteraction(SelectMenuInteractionEvent event) {
        Guild guild = event.getGuild();

        String selectMenuID = event.getSelectMenu().getId();
        List<SelectOption> selectedOptions = event.getSelectedOptions();
        List<SelectOption> allOptions = event.getSelectMenu().getOptions();

        if (selectMenuID.startsWith("selectmenurole")) {
            allOptions.forEach(option -> {
                String[] args = option.getValue().split(":");
                String roleID = args[2];

                Role role = guild.getRoleById(roleID);

                if (selectedOptions.contains(option)) {
                    guild.addRoleToMember(event.getUser(), role).queue();
                } else {
                    guild.removeRoleFromMember(event.getUser(), role).queue();
                }
            });

            event.reply(Language.getString("INTERACTIONROLELISTENER_UPDATED", guild)).setEphemeral(true).queue();
        }
    }
}
