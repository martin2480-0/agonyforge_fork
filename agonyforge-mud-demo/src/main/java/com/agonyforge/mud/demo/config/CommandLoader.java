package com.agonyforge.mud.demo.config;

import com.agonyforge.mud.demo.model.impl.CommandReference;
import com.agonyforge.mud.demo.model.impl.Role;
import com.agonyforge.mud.demo.model.repository.CommandRepository;

import com.agonyforge.mud.demo.model.repository.RoleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.*;

@Component
public class CommandLoader {
    private static final Logger LOGGER = LoggerFactory.getLogger(CommandLoader.class);

    private final CommandRepository commandRepository;
    private final RoleRepository roleRepository;

    @Autowired
    public CommandLoader(CommandRepository commandRepository, RoleRepository roleRepository) {
        this.commandRepository = commandRepository;
        this.roleRepository = roleRepository;
    }

    @PostConstruct
    public void loadCommands() {
        if (commandRepository.findAll().isEmpty()) {
            Map<String, CommandReference> refs = new HashMap<>();

            refs.put("NORTH", new CommandReference(1, "NORTH", "northCommand", "Move north.", true, false));
            refs.put("EAST", new CommandReference(1, "EAST", "eastCommand", "Move east.", true, false));
            refs.put("RIGHT", new CommandReference(1, "RIGHT", "eastCommand", "Move east.", true, false));
            refs.put("SOUTH", new CommandReference(1, "SOUTH", "southCommand", "Move south.", true, false));
            refs.put("WEST", new CommandReference(1, "WEST", "westCommand", "Move west.", true, false));
            refs.put("LEFT", new CommandReference(1, "LEFT", "westCommand", "Move west.", true, false));
            refs.put("UP", new CommandReference(1, "UP", "upCommand", "Move up.", true, false));
            refs.put("DOWN", new CommandReference(1, "DOWN", "downCommand", "Move down.", true, false));

            refs.put("NORTHEAST", new CommandReference(2, "NORTHEAST", "northeastCommand", "Move northeast.", true, false));
            refs.put("NORTHWEST", new CommandReference(2, "NORTHWEST", "northwestCommand", "Move northwest.", true, false));
            refs.put("SOUTHEAST", new CommandReference(2, "SOUTHEAST", "southeastCommand", "Move southeast.", true, false));
            refs.put("SOUTHWEST", new CommandReference(2, "SOUTHWEST", "southwestCommand", "Move southwest.", true, false));
            refs.put("NE", new CommandReference(2, "NE", "northeastCommand", "Move northeast.", true, false));
            refs.put("NW", new CommandReference(2, "NW", "northwestCommand", "Move northwest.", true, false));
            refs.put("SE", new CommandReference(2, "SE", "southeastCommand", "Move southeast.", true, false));
            refs.put("SW", new CommandReference(2, "SW", "southwestCommand", "Move southwest.", true, false));

            refs.put("HELP", new CommandReference(4, "HELP", "helpCommand", "Get help with commands.", false, true));

            refs.put("LOOK", new CommandReference(5, "LOOK", "lookCommand", "Look at things in the world.", true, false));
            refs.put("WHO", new CommandReference(5, "WHO", "whoCommand", "See who is playing.", false, true));
            refs.put("SCORE", new CommandReference(5, "SCORE", "scoreCommand", "See your character sheet.", true, true));
            refs.put("EQUIPMENT", new CommandReference(5, "EQUIPMENT", "equipmentCommand", "See what you're wearing.", true, true));
            refs.put("INVENTORY", new CommandReference(5, "INVENTORY", "inventoryCommand", "See what you're carrying.", true, true));
            refs.put("TITLE", new CommandReference(5, "TITLE", "titleCommand", "Change your title message on the who list.", false, true));

            refs.put("DROP", new CommandReference(10, "DROP", "dropCommand", "Drop an item.", true, false));
            refs.put("GET", new CommandReference(10, "GET", "getCommand", "Pick up an item.", true, false));
            refs.put("GIVE", new CommandReference(10, "GIVE", "giveCommand", "Give an item to someone.", true, false));
            refs.put("REMOVE", new CommandReference(10, "REMOVE", "removeCommand", "Stop wearing an item.", true, false));
            refs.put("WEAR", new CommandReference(10, "WEAR", "wearCommand", "Wear an item that you're carrying.", true, false));
            refs.put("EMOTE", new CommandReference(10, "EMOTE", "emoteCommand", "Perform a social action.", true, false));
            refs.put("GOSSIP", new CommandReference(10, "GOSSIP", "gossipCommand", "Talk on the worldwide channel.", true, true));
            refs.put("SAY", new CommandReference(10, "SAY", "sayCommand", "Talk in the room you're in.", true, true));
            refs.put("SHOUT", new CommandReference(10, "SHOUT", "shoutCommand", "Talk in the area you're in.", true, true));
            refs.put("TELL", new CommandReference(10, "TELL", "tellCommand", "Say something privately to someone anywhere in the world.", true, true));
            refs.put("WHISPER", new CommandReference(10, "WHISPER", "whisperCommand", "Say something privately to someone in the same room.", true, true));

            refs.put("ROLL", new CommandReference(15, "ROLL", "rollCommand", "Roll some dice.", false, false));
            refs.put("TIME", new CommandReference(15, "TIME", "timeCommand", "See what time it is.", false, true));
            refs.put("QUIT", new CommandReference(15, "QUIT", "quitCommand", "Return to the character menu.", false, true));

            refs.put("REDIT", new CommandReference(20, "REDIT", "roomEditorCommand", "Edit a room.", false, false));
            refs.put("IEDIT", new CommandReference(20, "IEDIT", "itemEditorCommand", "Edit an item.", false, false));
            refs.put("MEDIT", new CommandReference(20, "MEDIT", "nonPlayerCreatureEditorCommand", "Edit a creature.", false, false));
            refs.put("CEDIT", new CommandReference(20, "CEDIT", "commandEditorCommand", "Edit commands.", false, false));

            refs.put("GOTO", new CommandReference(30, "GOTO", "gotoCommand", "Go to a room or player.", false, false));
            refs.put("TRANSFER", new CommandReference(30, "TRANSFER", "transferCommand", "Bring a player to you.", false, false));
            refs.put("TELEPORT", new CommandReference(30, "TELEPORT", "teleportCommand", "Send a player to a room.", false, false));
            refs.put("CREATE", new CommandReference(30, "CREATE", "createCommand", "Create an item.", false, false));
            refs.put("SPAWN", new CommandReference(30, "SPAWN", "spawnCommand", "Spawn a character.", false, false));
            refs.put("PURGE", new CommandReference(30, "PURGE", "purgeCommand", "Destroy an item.", false, false));
            refs.put("SLAY", new CommandReference(30, "SLAY", "slayCommand", "Slay a character.", false, false));
            refs.put("FORCE", new CommandReference(30, "FORCE", "forceCommand", "Force a character to do something.", false, false));
            refs.put("BAN", new CommandReference(30, "FREEZE", "freezeCommand", "Freezes character.", false, false));


            LOGGER.info("Creating command references");
            commandRepository.saveAll(refs.values());

            Role implementor = new Role();

            implementor.setName("Implementor");
            implementor.setImplementor(true);

            Role player = new Role();

            String[] commandsForPlayer = {
                "HELP", "LOOK", "WHO", "SCORE", "TITLE",
                "EQUIPMENT", "INVENTORY", "DROP", "GET", "GIVE", "REMOVE", "WEAR",
                "EMOTE", "GOSSIP", "SAY", "SHOUT", "TELL", "WHISPER", "TIME",
                "NORTH", "EAST", "SOUTH", "WEST", "UP", "DOWN",
                "NORTHEAST", "NORTHWEST", "SOUTHEAST", "SOUTHWEST",
                "NE", "NW", "SE", "SW"
            };

            player.setName("Player");

            for (String command : commandsForPlayer) {
                player.getCommands().add(refs.get(command));
            }

            roleRepository.saveAll(List.of(implementor, player));

        }
    }
}
