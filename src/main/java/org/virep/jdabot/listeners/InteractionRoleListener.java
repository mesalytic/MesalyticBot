package org.virep.jdabot.listeners;

import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.SelectMenuInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;

import java.util.List;

public class InteractionRoleListener extends ListenerAdapter {
    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        String buttonID = event.getButton().getId();

        if (buttonID.startsWith("interactionrole")) {
            String[] args = buttonID.split(":");

            String roleID = args[2];

            Role role = event.getGuild().getRoleById(roleID);

            if (event.getMember().getRoles().contains(role)) {
                event.getGuild().removeRoleFromMember(event.getUser(), role).queue();
                event.reply("The role has successfully been removed !").setEphemeral(true).queue();
            } else {
                event.getGuild().addRoleToMember(event.getUser(), role).queue();
                event.reply("You successfully got the role !").setEphemeral(true).queue();
            }


        }
    }

    @Override
    public void onSelectMenuInteraction(SelectMenuInteractionEvent event) {

        String selectMenuID = event.getSelectMenu().getId();
        List<SelectOption> selectedOptions = event.getSelectedOptions();
        List<SelectOption> allOptions = event.getSelectMenu().getOptions();

        if (selectMenuID.startsWith("selectmenurole")) {
            allOptions.forEach(option -> {
                String[] args = option.getValue().split(":");
                String roleID = args[2];

                Role role = event.getGuild().getRoleById(roleID);

                if (selectedOptions.contains(option)) {
                    event.getGuild().addRoleToMember(event.getUser(), role).queue();
                } else {
                    event.getGuild().removeRoleFromMember(event.getUser(), role).queue();
                }
            });

            event.reply("Your roles have successfully been updated.").setEphemeral(true).queue();
        }
    }
}
