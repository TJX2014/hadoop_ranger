package org.example;

import java.io.File;

public class Main {
    public static void main(String[] args) {
        System.out.println("Hello world!");

        File f = new File("/usr/dfs");
        f.exists();
    }
}