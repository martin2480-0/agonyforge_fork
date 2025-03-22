package com.agonyforge.mud.demo.config;

import com.agonyforge.mud.demo.model.impl.CommandForce;
import com.agonyforge.mud.demo.model.impl.CommandReference;
import com.agonyforge.mud.demo.model.impl.Role;
import com.agonyforge.mud.demo.model.repository.CommandForceRepository;
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
    private final CommandForceRepository commandForceRepository;

    @Autowired
    public CommandLoader(CommandRepository commandRepository, CommandForceRepository commandForceRepository, RoleRepository roleRepository) {
        this.commandRepository = commandRepository;
        this.roleRepository = roleRepository;
        this.commandForceRepository = commandForceRepository;
    }

    @PostConstruct
    public void loadCommands() {
        if (commandRepository.findAll().isEmpty()) {
            Map<String, CommandReference> refs = new HashMap<>();

            refs.put("NORTH", new CommandReference(1, "NORTH", "northCommand", "Move north."));
            refs.put("EAST", new CommandReference(1, "EAST", "eastCommand", "Move east."));
            refs.put("SOUTH", new CommandReference(1, "SOUTH", "southCommand", "Move south."));
            refs.put("WEST", new CommandReference(1, "WEST", "westCommand", "Move west."));
            refs.put("UP", new CommandReference(1, "UP", "upCommand", "Move up."));
            refs.put("DOWN", new CommandReference(1, "DOWN", "downCommand", "Move down."));

            refs.put("NORTHEAST", new CommandReference(2, "NORTHEAST", "northeastCommand", "Move northeast."));
            refs.put("NORTHWEST", new CommandReference(2, "NORTHWEST", "northwestCommand", "Move northwest."));
            refs.put("SOUTHEAST", new CommandReference(2, "SOUTHEAST", "southeastCommand", "Move southeast."));
            refs.put("SOUTHWEST", new CommandReference(2, "SOUTHWEST", "southwestCommand", "Move southwest."));
            refs.put("NE", new CommandReference(2, "NE", "northeastCommand", "Move northeast."));
            refs.put("NW", new CommandReference(2, "NW", "northwestCommand", "Move northwest."));
            refs.put("SE", new CommandReference(2, "SE", "southeastCommand", "Move southeast."));
            refs.put("SW", new CommandReference(2, "SW", "southwestCommand", "Move southwest."));

            refs.put("HELP", new CommandReference(4, "HELP", "helpCommand", "Get help with commands."));

            refs.put("LOOK", new CommandReference(5, "LOOK", "lookCommand", "Look at things in the world."));
            refs.put("WHO", new CommandReference(5, "WHO", "whoCommand", "See who is playing."));
            refs.put("SCORE", new CommandReference(5, "SCORE", "scoreCommand", "See your character sheet."));
            refs.put("EQUIPMENT", new CommandReference(5, "EQUIPMENT", "equipmentCommand", "See what you're wearing."));
            refs.put("INVENTORY", new CommandReference(5, "INVENTORY", "inventoryCommand", "See what you're carrying."));
            refs.put("TITLE", new CommandReference(5, "TITLE", "titleCommand", "Change your title message on the who list."));

            refs.put("DROP", new CommandReference(10, "DROP", "dropCommand", "Drop an item."));
            refs.put("GET", new CommandReference(10, "GET", "getCommand", "Pick up an item."));
            refs.put("GIVE", new CommandReference(10, "GIVE", "giveCommand", "Give an item to someone."));
            refs.put("REMOVE", new CommandReference(10, "REMOVE", "removeCommand", "Stop wearing an item."));
            refs.put("WEAR", new CommandReference(10, "WEAR", "wearCommand", "Wear an item that you're carrying."));
            refs.put("EMOTE", new CommandReference(10, "EMOTE", "emoteCommand", "Perform a social action."));
            refs.put("GOSSIP", new CommandReference(10, "GOSSIP", "gossipCommand", "Talk on the worldwide channel."));
            refs.put("SAY", new CommandReference(10, "SAY", "sayCommand", "Talk in the room you're in."));
            refs.put("SHOUT", new CommandReference(10, "SHOUT", "shoutCommand", "Talk in the area you're in."));
            refs.put("TELL", new CommandReference(10, "TELL", "tellCommand", "Say something privately to someone anywhere in the world."));
            refs.put("WHISPER", new CommandReference(10, "WHISPER", "whisperCommand", "Say something privately to someone in the same room."));

            refs.put("ROLL", new CommandReference(15, "ROLL", "rollCommand", "Roll some dice."));
            refs.put("TIME", new CommandReference(15, "TIME", "timeCommand", "See what time it is."));
            refs.put("QUIT", new CommandReference(15, "QUIT", "quitCommand", "Return to the character menu."));

            refs.put("REDIT", new CommandReference(20, "REDIT", "roomEditorCommand", "Edit a room."));
            refs.put("IEDIT", new CommandReference(20, "IEDIT", "itemEditorCommand", "Edit an item."));
            refs.put("MEDIT", new CommandReference(20, "MEDIT", "nonPlayerCreatureEditorCommand", "Edit a creature."));
            refs.put("CEDIT", new CommandReference(20, "CEDIT", "commandEditorCommand", "Edit commands."));

            refs.put("GOTO", new CommandReference(30, "GOTO", "gotoCommand", "Go to a room or player."));
            refs.put("TRANSFER", new CommandReference(30, "TRANSFER", "transferCommand", "Bring a player to you."));
            refs.put("TELEPORT", new CommandReference(30, "TELEPORT", "teleportCommand", "Send a player to a room."));
            refs.put("CREATE", new CommandReference(30, "CREATE", "createCommand", "Create an item."));
            refs.put("SPAWN", new CommandReference(30, "SPAWN", "spawnCommand", "Spawn a character."));
            refs.put("PURGE", new CommandReference(30, "PURGE", "purgeCommand", "Destroy an item."));
            refs.put("SLAY", new CommandReference(30, "SLAY", "slayCommand", "Slay a character."));
            refs.put("FORCE", new CommandReference(30, "FORCE", "forceCommand", "Force a character to do something."));
            refs.put("BAN", new CommandReference(30, "BAN", "banCommand", "Bans player of character from the game."));

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


            Map<CommandReference, Boolean> forcible = new HashMap<>();

            forcible.put(refs.get("DOWN"), true);
            forcible.put(refs.get("SOUTH"), true);
            forcible.put(refs.get("UP"), true);
            forcible.put(refs.get("NORTH"), true);
            forcible.put(refs.get("EAST"), true);
            forcible.put(refs.get("WEST"), true);
            forcible.put(refs.get("NORTHEAST"), true);
            forcible.put(refs.get("NE"), true);
            forcible.put(refs.get("NORTHWEST"), true);
            forcible.put(refs.get("NW"), true);
            forcible.put(refs.get("SOUTHWEST"), true);
            forcible.put(refs.get("SW"), true);
            forcible.put(refs.get("SOUTHEAST"), true);
            forcible.put(refs.get("SE"), true);

            forcible.put(refs.get("LOOK"), true);
            forcible.put(refs.get("EMOTE"), true);
            forcible.put(refs.get("WHISPER"), true);
            forcible.put(refs.get("GIVE"), true);
            forcible.put(refs.get("GET"), true);
            forcible.put(refs.get("GOSSIP"), true);
            forcible.put(refs.get("SAY"), true);
            forcible.put(refs.get("SHOUT"), true);
            forcible.put(refs.get("DROP"), true);
            forcible.put(refs.get("WEAR"), true);
            forcible.put(refs.get("TELL"), true);
            forcible.put(refs.get("REMOVE"), true);
            forcible.put(refs.get("SCORE"), true);
            forcible.put(refs.get("EQUIPMENT"), true);
            forcible.put(refs.get("INVENTORY"), true);

            forcible.put(refs.get("WHO"), false);
            forcible.put(refs.get("TITLE"), false);
            forcible.put(refs.get("HELP"), false);
            forcible.put(refs.get("ROLL"), false);
            forcible.put(refs.get("TIME"), false);
            forcible.put(refs.get("QUIT"), false);
            forcible.put(refs.get("REDIT"), false);
            forcible.put(refs.get("IEDIT"), false);
            forcible.put(refs.get("MEDIT"), false);
            forcible.put(refs.get("CEDIT"), false);
            forcible.put(refs.get("GOTO"), false);
            forcible.put(refs.get("TRANSFER"), false);
            forcible.put(refs.get("TELEPORT"), false);
            forcible.put(refs.get("CREATE"), false);
            forcible.put(refs.get("SPAWN"), false);
            forcible.put(refs.get("PURGE"), false);
            forcible.put(refs.get("SLAY"), false);
            forcible.put(refs.get("FORCE"), false);


            List<CommandForce> commandForces = new ArrayList<>();

            forcible.forEach((commandReference, isForcible) -> {
                CommandForce commandForce = new CommandForce(commandReference, isForcible);
                commandForces.add(commandForce);
            });

            commandForceRepository.saveAll(commandForces);
        }


    }
}
