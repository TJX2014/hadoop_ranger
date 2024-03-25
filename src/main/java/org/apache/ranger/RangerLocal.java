package org.apache.ranger;

import org.apache.ranger.server.tomcat.EmbeddedServer;

public class RangerLocal {

    public static void main(String[] args) {
        System.setProperty("logdir", "/tmp/");
        System.setProperty("servername", "rangeradmin");

        (new EmbeddedServer(args)).start();
    }
}
