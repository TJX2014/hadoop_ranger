package org.example;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class Main {
    public static void main(String[] args) {
        System.out.println("Hello world!");

        Map map = new HashMap<String, String>(){
            {
                put("aa", "bb");
            }
        };

        File f = new File("/usr/dfs");
        f.exists();
    }
}