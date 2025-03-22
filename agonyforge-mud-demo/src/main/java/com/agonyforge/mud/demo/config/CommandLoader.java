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

            refs.put("NORTH", new CommandReference(1, "NORTH", "northCommand", "Move north.", true));
            refs.put("EAST", new CommandReference(1, "EAST", "eastCommand", "Move east.", true));
            refs.put("SOUTH", new CommandReference(1, "SOUTH", "southCommand", "Move south.", true));
            refs.put("WEST", new CommandReference(1, "WEST", "westCommand", "Move west.", true));
            refs.put("UP", new CommandReference(1, "UP", "upCommand", "Move up.", true));
            refs.put("DOWN", new CommandReference(1, "DOWN", "downCommand", "Move down.", true));

            refs.put("NORTHEAST", new CommandReference(2, "NORTHEAST", "northeastCommand", "Move northeast.", true));
            refs.put("NORTHWEST", new CommandReference(2, "NORTHWEST", "northwestCommand", "Move northwest.", true));
            refs.put("SOUTHEAST", new CommandReference(2, "SOUTHEAST", "southeastCommand", "Move southeast.", true));
            refs.put("SOUTHWEST", new CommandReference(2, "SOUTHWEST", "southwestCommand", "Move southwest.", true));
            refs.put("NE", new CommandReference(2, "NE", "northeastCommand", "Move northeast.", true));
            refs.put("NW", new CommandReference(2, "NW", "northwestCommand", "Move northwest.", true));
            refs.put("SE", new CommandReference(2, "SE", "southeastCommand", "Move southeast.", true));
            refs.put("SW", new CommandReference(2, "SW", "southwestCommand", "Move southwest.", true));

            refs.put("HELP", new CommandReference(4, "HELP", "helpCommand", "Get help with commands.", false));

            refs.put("LOOK", new CommandReference(5, "LOOK", "lookCommand", "Look at things in the world.", true));
            refs.put("WHO", new CommandReference(5, "WHO", "whoCommand", "See who is playing.", false));
            refs.put("SCORE", new CommandReference(5, "SCORE", "scoreCommand", "See your character sheet.", true));
            refs.put("EQUIPMENT", new CommandReference(5, "EQUIPMENT", "equipmentCommand", "See what you're wearing.", true));
            refs.put("INVENTORY", new CommandReference(5, "INVENTORY", "inventoryCommand", "See what you're carrying.", true));
            refs.put("TITLE", new CommandReference(5, "TITLE", "titleCommand", "Change your title message on the who list.", false));

            refs.put("DROP", new CommandReference(10, "DROP", "dropCommand", "Drop an item.", true));
            refs.put("GET", new CommandReference(10, "GET", "getCommand", "Pick up an item.", true));
            refs.put("GIVE", new CommandReference(10, "GIVE", "giveCommand", "Give an item to someone.", true));
            refs.put("REMOVE", new CommandReference(10, "REMOVE", "removeCommand", "Stop wearing an item.", true));
            refs.put("WEAR", new CommandReference(10, "WEAR", "wearCommand", "Wear an item that you're carrying.", true));
            refs.put("EMOTE", new CommandReference(10, "EMOTE", "emoteCommand", "Perform a social action.", true));
            refs.put("GOSSIP", new CommandReference(10, "GOSSIP", "gossipCommand", "Talk on the worldwide channel.", true));
            refs.put("SAY", new CommandReference(10, "SAY", "sayCommand", "Talk in the room you're in.", true));
            refs.put("SHOUT", new CommandReference(10, "SHOUT", "shoutCommand", "Talk in the area you're in.", true));
            refs.put("TELL", new CommandReference(10, "TELL", "tellCommand", "Say something privately to someone anywhere in the world.", true));
            refs.put("WHISPER", new CommandReference(10, "WHISPER", "whisperCommand", "Say something privately to someone in the same room.", true));

            refs.put("ROLL", new CommandReference(15, "ROLL", "rollCommand", "Roll some dice.", false));
            refs.put("TIME", new CommandReference(15, "TIME", "timeCommand", "See what time it is.", false));
            refs.put("QUIT", new CommandReference(15, "QUIT", "quitCommand", "Return to the character menu.", false));

            refs.put("REDIT", new CommandReference(20, "REDIT", "roomEditorCommand", "Edit a room.", false));
            refs.put("IEDIT", new CommandReference(20, "IEDIT", "itemEditorCommand", "Edit an item.", false));
            refs.put("MEDIT", new CommandReference(20, "MEDIT", "nonPlayerCreatureEditorCommand", "Edit a creature.", false));
            refs.put("CEDIT", new CommandReference(20, "CEDIT", "commandEditorCommand", "Edit commands.", false));

            refs.put("GOTO", new CommandReference(30, "GOTO", "gotoCommand", "Go to a room or player.", false));
            refs.put("TRANSFER", new CommandReference(30, "TRANSFER", "transferCommand", "Bring a player to you.", false));
            refs.put("TELEPORT", new CommandReference(30, "TELEPORT", "teleportCommand", "Send a player to a room.", false));
            refs.put("CREATE", new CommandReference(30, "CREATE", "createCommand", "Create an item.", false));
            refs.put("SPAWN", new CommandReference(30, "SPAWN", "spawnCommand", "Spawn a character.", false));
            refs.put("PURGE", new CommandReference(30, "PURGE", "purgeCommand", "Destroy an item.", false));
            refs.put("SLAY", new CommandReference(30, "SLAY", "slayCommand", "Slay a character.", false));
            refs.put("FORCE", new CommandReference(30, "FORCE", "forceCommand", "Force a character to do something.", false));
            refs.put("BAN", new CommandReference(30, "BAN", "banCommand", "Bans player of character from the game.", false));

            LOGGER.info("Creating command references");
            commandRepository.saveAll(refs.values());

            Role implementor = new Role();

            implementor.setName("Implementor");
            implementor.setImplementor(true);

            Role player = new Role();

            player.setName("Player");
            player.getCommands().add(refs.get("NORTH"));
            player.getCommands().add(refs.get("EAST"));
            player.getCommands().add(refs.get("SOUTH"));
            player.getCommands().add(refs.get("WEST"));
            player.getCommands().add(refs.get("UP"));
            player.getCommands().add(refs.get("DOWN"));

            player.getCommands().add(refs.get("NORTHEAST"));
            player.getCommands().add(refs.get("NORTHWEST"));
            player.getCommands().add(refs.get("SOUTHEAST"));
            player.getCommands().add(refs.get("SOUTHWEST"));
            player.getCommands().add(refs.get("NE"));
            player.getCommands().add(refs.get("NW"));
            player.getCommands().add(refs.get("SE"));
            player.getCommands().add(refs.get("SW"));

            player.getCommands().add(refs.get("HELP"));
            player.getCommands().add(refs.get("LOOK"));
            player.getCommands().add(refs.get("WHO"));
            player.getCommands().add(refs.get("SCORE"));
            player.getCommands().add(refs.get("TITLE"));

            player.getCommands().add(refs.get("EQUIPMENT"));
            player.getCommands().add(refs.get("INVENTORY"));
            player.getCommands().add(refs.get("DROP"));
            player.getCommands().add(refs.get("GET"));
            player.getCommands().add(refs.get("GIVE"));
            player.getCommands().add(refs.get("REMOVE"));
            player.getCommands().add(refs.get("WEAR"));

            player.getCommands().add(refs.get("EMOTE"));
            player.getCommands().add(refs.get("GOSSIP"));
            player.getCommands().add(refs.get("SAY"));
            player.getCommands().add(refs.get("SHOUT"));
            player.getCommands().add(refs.get("TELL"));
            player.getCommands().add(refs.get("WHISPER"));

            player.getCommands().add(refs.get("TIME"));

            roleRepository.saveAll(List.of(implementor, player));

        }


    }
}
