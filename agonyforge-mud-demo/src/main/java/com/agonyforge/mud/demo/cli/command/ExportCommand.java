package com.agonyforge.mud.demo.cli.command;

import com.agonyforge.mud.core.cli.Question;
import com.agonyforge.mud.core.web.model.Input;
import com.agonyforge.mud.core.web.model.Output;
import com.agonyforge.mud.core.web.model.WebSocketContext;
import com.agonyforge.mud.demo.cli.RepositoryBundle;
import com.agonyforge.mud.demo.model.export.ImportExportService;
import com.agonyforge.mud.demo.model.impl.MudCharacter;
import com.agonyforge.mud.demo.service.CommService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

import static com.agonyforge.mud.demo.model.export.ImportExportService.writeYamlToFile;

@Component
public class ExportCommand extends AbstractCommand {

    final private ImportExportService importExportService;

    @Autowired
    public ExportCommand(RepositoryBundle repositoryBundle, CommService commService, ApplicationContext applicationContext, ImportExportService importExportService) {
        super(repositoryBundle, commService, applicationContext);
        this.importExportService = importExportService;
    }

    @Override
    public Question execute(Question question, WebSocketContext webSocketContext, List<String> tokens, Input input, Output output) {
        MudCharacter ch = getCurrentCharacter(webSocketContext, output);

        if (tokens.size() < 2) {
            output.append("[default]What would you like to export?");
            return question;
        }

        String type = tokens.get(1).toLowerCase();

        try {
            if (type.equals("items") || type.equals("map") || type.equals("character")) {
                String content = importExportService.export(type, ch);
                writeYamlToFile(content, webSocketContext.getPrincipal().getName(), type);
            } else {
                output.append("[default]Export of %s is not supported!", type);
                return question;
            }
        } catch (IOException e){
            output.append("[default]Export error!");
            return question;
        }

        getCommService().triggerDownload(webSocketContext.getPrincipal().getName());

        return question;
    }
}
