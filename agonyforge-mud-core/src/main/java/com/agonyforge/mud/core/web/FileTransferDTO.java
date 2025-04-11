package com.agonyforge.mud.core.web;


public class FileTransferDTO {
    private String base64Content;
    private String type;
    private String principal;

    public FileTransferDTO(String base64Content, String type, String principal) {
        this.base64Content = base64Content;
        this.type = type;
        this.principal = principal;
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

    public void setPrincipal(String principal) {
        this.principal = principal;
    }

    public String getPrincipal() {
        return principal;
    }
}
