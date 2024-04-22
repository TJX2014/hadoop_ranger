package org.example;

import cn.hutool.core.util.HexUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

public class Main {
    public static void main(String[] args) throws IOException {
        FileInputStream keytabBytesInput = new FileInputStream("/usr/kerberos_xiaoxing/xiaoxing.keytab");
        byte[] keytabBytes = new byte[keytabBytesInput.available()];
        keytabBytesInput.read(keytabBytes);
        byte[] keytabHex = HexUtil.encodeHexStr(keytabBytes).getBytes(StandardCharsets.UTF_8);
        byte[] keytabBase64 = Base64.getEncoder().encode(keytabBytes);
        new FileOutputStream("/usr/keytabHex").write(keytabHex);
        new FileOutputStream("/usr/keytabBase64").write(keytabBase64);

        File mpa4src = new File("/usr/showcase.mp4");
        FileInputStream fis = new FileInputStream(mpa4src);
        byte[] inputBytes = new byte[fis.available()];
        fis.read(inputBytes);
        byte[] encodeBytes = Base64.getEncoder().encode(inputBytes);
        String outFile = "/tmp/showcase_base64";
        new File(outFile).delete();
        new FileOutputStream(outFile).write(encodeBytes);

        FileInputStream base64Is = new FileInputStream(outFile);
        byte[] base64DecodeBytes = new byte[base64Is.available()];
        base64Is.read(base64DecodeBytes);
        byte[] base64map = Base64.getDecoder().decode(base64DecodeBytes);
        new FileOutputStream("/tmp/java_showcase_base64.map").write(base64map);

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