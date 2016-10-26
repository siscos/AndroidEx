package com.siscos.interviewapp;

import java.util.ArrayList;
import java.util.List;

public class PageInfo {
    String category;
    String name;
    String id;
    String pageAccessToken;
    List<String> permList;

    public PageInfo() {
        permList = new ArrayList<String>();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getPageAccessToken() {
        return pageAccessToken;
    }

    public void setPageAccessToken(String pageAccessToken) {
        this.pageAccessToken = pageAccessToken;
    }

    public void addPerm(String perm) {
        permList.add(perm);
    }

    public String getPerm(int idx){
        return permList.get(idx);
    }
}
