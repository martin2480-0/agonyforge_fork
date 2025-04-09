package com.agonyforge.mud.core.web;


public class FileTransferDTO {
    private String filename;
    private String contentType;
    private String base64Content;
    private String type;


    public FileTransferDTO(String filename) {
        this.filename = filename;
    }

    public FileTransferDTO(String filename, String contentType, String base64Content, String type) {
        this.filename = filename;
        this.contentType = contentType;
        this.base64Content = base64Content;
        this.type = type;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
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
