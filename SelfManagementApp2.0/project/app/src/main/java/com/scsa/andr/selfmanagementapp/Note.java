package com.scsa.andr.selfmanagementapp;

import java.io.Serializable;

public class Note implements Serializable {
    String title;
    String body;
    long regDate;

    Note(String title, String body, long regDate){
        this.title = title;
        this.body = body;
        this.regDate = regDate;
    }
}
