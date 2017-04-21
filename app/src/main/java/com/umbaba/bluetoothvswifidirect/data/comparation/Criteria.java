package com.umbaba.bluetoothvswifidirect.data.comparation;


public class Criteria {

   private String title;

   private String left;

   private String right;


    public Criteria(String title, String left, String right) {
        this.title = title;
        this.left = left;
        this.right = right;
    }

    public String getTitle() {
        return title;
    }

    public String getLeft() {
        return left;
    }

    public String getRight() {
        return right;
    }
}
