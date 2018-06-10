package com.example.android.newsreader;

public class Article {

    private String section ;
    private String title ;
    private String url ;
    private String date ;


    public Article (String section, String title, String date, String url){
        this.section = section ;
        this.title = title ;
        this.url = url ;
        this.date = date ;

    }
    public String getSection() {
        return section;
    }

    public String getTitle() {
        return title;
    }

    public String getUrl() {
        return url;
    }

    public String getDate() {
        return date;
    }


}
