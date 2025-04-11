package com.agonyforge.mud.core.web;


public class FileTransferDTO {
    private String base64Content;
    private String type;

    public FileTransferDTO(String filename, String contentType, String base64Content, String type) {
        this.base64Content = base64Content;
        this.type = type;
    }

    public String getBase64Content() {
        return base64Content;
    }

    public void setBase64Content(String base64Content) {
        this.base64Content = base64Content;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
