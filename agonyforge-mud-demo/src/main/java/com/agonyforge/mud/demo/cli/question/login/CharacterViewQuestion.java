package com.agonyforge.mud.demo.cli.question.login;

import com.agonyforge.mud.core.cli.Question;
import com.agonyforge.mud.core.cli.Response;
import com.agonyforge.mud.core.service.SessionAttributeService;
import com.agonyforge.mud.core.web.model.Input;
import com.agonyforge.mud.core.web.model.Output;
import com.agonyforge.mud.core.web.model.WebSocketContext;
import com.agonyforge.mud.demo.cli.RepositoryBundle;
import com.agonyforge.mud.demo.cli.command.LookCommand;
import com.agonyforge.mud.demo.cli.question.BaseQuestion;
import com.agonyforge.mud.demo.model.export.ImportExportService;
import com.agonyforge.mud.demo.model.impl.LocationComponent;
import com.agonyforge.mud.demo.model.impl.MudCharacter;
import com.agonyforge.mud.demo.model.impl.MudRoom;
import com.agonyforge.mud.demo.service.CommService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Optional;

import static com.agonyforge.mud.core.config.SessionConfiguration.MUD_CHARACTER;
import static com.agonyforge.mud.demo.model.export.ImportExportService.writeYamlToFile;

@Component
public class CharacterViewQuestion extends BaseQuestion {
    static final Long START_ROOM = 100L;

    private static final Logger LOGGER = LoggerFactory.getLogger(CharacterViewQuestion.class);

    private final CommService commService;
    private final SessionAttributeService sessionAttributeService;
    private final CharacterSheetFormatter characterSheetFormatter;
    private final ImportExportService importExportService;

    @Autowired
    public CharacterViewQuestion(ApplicationContext applicationContext,
                                 RepositoryBundle repositoryBundle,
                                 CommService commService,
                                 SessionAttributeService sessionAttributeService,
                                 CharacterSheetFormatter characterSheetFormatter,
                                 ImportExportService importExportService) {
        super(applicationContext, repositoryBundle);
        this.commService = commService;
        this.sessionAttributeService = sessionAttributeService;
        this.characterSheetFormatter = characterSheetFormatter;
        this.importExportService = importExportService;
    }

    @Override
    public Output prompt(WebSocketContext wsContext) {
        Output output = new Output();
        Optional<MudCharacter> chOptional = getCharacter(wsContext, output);

        if (chOptional.isPresent()) {
            MudCharacter ch = chOptional.get();

            characterSheetFormatter.format(ch, output);

            output.append("");

            output.append("[green]P[black]) Play as this character");
            output.append("[red]D[black]) Delete this character");
            output.append("[yellow]E[black]) Export this character");
            output.append("[dwhite]B[black]) Go back");
            output.append("[black]Please [white]make your selection[black]: ");
        }

        return output;
    }

    @Override
    public Response answer(WebSocketContext wsContext, Input input) {
        Output output = new Output();
        Question next = this;
        Optional<MudCharacter> chOptional = getCharacter(wsContext, output);

        if ("P".equalsIgnoreCase(input.getInput())) {
            Optional<MudRoom> roomOptional = getRepositoryBundle().getRoomRepository().findById(START_ROOM);

            if (chOptional.isPresent() && roomOptional.isPresent()) {
                MudCharacter ch = chOptional.get();
                MudRoom startRoom = roomOptional.get();

                if (ch.getLocation() == null) {
                    ch.setLocation(new LocationComponent());
                } else if (ch.getLocation().getRoom() != null) {
                    output.append("[red]This character is already playing. Try a different one, or create a new one.");
                    return new Response(next, output);
                }

                ch.getPlayer().setWebSocketSession(wsContext.getSessionId());
                ch.getLocation().setRoom(startRoom);

                ch = getRepositoryBundle().getCharacterRepository().save(ch);
                wsContext.getAttributes().put(MUD_CHARACTER, ch.getId());

                output.append(LookCommand.doLook(getRepositoryBundle(), sessionAttributeService, ch, startRoom));

                LOGGER.info("{} has entered the game", ch.getCharacter().getName());
                commService.sendToAll(wsContext, new Output("[yellow]%s has entered the game!", ch.getCharacter().getName()), ch);

                next = getQuestion("commandQuestion");
            } else {
                if (roomOptional.isEmpty()) {
                    LOGGER.error("Start room with ID {} was empty!", START_ROOM);
                }
            }
        } else if ("D".equalsIgnoreCase(input.getInput())) {
            next = getQuestion("characterDeleteQuestion");

        } else if ("E".equalsIgnoreCase(input.getInput())) {
            next = getQuestion("characterViewQuestion");
            try {
                if (chOptional.isEmpty()){
                    throw new IOException(); // TODO add better exception
                }
                String content = importExportService.export("character", chOptional.get());
                writeYamlToFile(content, wsContext.getPrincipal().getName(), "character");
            }catch (IOException e) {
                next = getQuestion("characterViewQuestion");
                output.append("[red]Could not export the character");
                return new Response(next, output);
            }
            commService.triggerDownload(wsContext.getPrincipal().getName());
        }

        else if ("B".equalsIgnoreCase(input.getInput())) {
            next = getQuestion("characterMenuQuestion");
        } else {
            output.append("[red]Unknown selection. Please try again.");
        }

        return new Response(next, output);
    }
}
