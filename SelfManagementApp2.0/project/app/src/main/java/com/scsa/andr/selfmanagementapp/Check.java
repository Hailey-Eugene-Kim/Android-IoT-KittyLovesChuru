package com.scsa.andr.selfmanagementapp;

public class Check {

    private String code;
    private String date;
    private String clean;
    private String ready;

    //추가
    private String title;
    private String link;
    private String description;

    private String response;
    private String request = "";

    public Check() {
        super();
        // TODO Auto-generated constructor stub
    }

    public Check(String code, String date, String clean, String ready, String title, String link,String description,
                 String response) {
        super();
        this.code = code;
        this.date = date;
        this.clean = clean;
        this.ready = ready;

        this.title = title;
        this.link = link;
        this.description = description;

        this.response = response;
    }

    public Check(String code, String date, String clean, String ready, String title, String link,String description,
                 String response, String request) {
        super();
        this.code = code;
        this.date = date;
        this.clean = clean;
        this.ready = ready;

        this.title = title;
        this.link = link;
        this.description = description;

        this.response = response;
        this.request = request;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getClean() {
        return clean;
    }

    public void setClean(String clean) {
        this.clean = clean;
    }

    public String getReady() {
        return ready;
    }

    public void setReady(String ready) {
        this.ready = ready;
    }

    public String getTitle() { return title; }

    public void setTitle(String title) { this.title = title; }

    public String getLink() { return link; }

    public void setLink(String link) { this.link = link; }

    public String getDescription() { return description; }

    public void setDescription(String description) { this.description = description; }

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }

    public String getRequest() {
        return request;
    }

    public void setRequest(String request) {
        this.request = request;
    }

    public String toString() {
        return code + "\t: " + date + "\t: "
                + clean + "\t: " + ready + "\t: " + title + "\t: " +link + "\t: " +description + "\t: " +response + "\t: " + request;
    }

}
