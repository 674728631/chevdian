package com.cvd.chevdian;

import org.springframework.beans.propertyeditors.UUIDEditor;

import java.util.*;

public class Test {
    public static void main(String[] args) {

        UUID s = UUID.randomUUID();
        System.out.println(s.toString());
        System.out.println(s.toString().replace("-",""));
//        String ss = UUID.fromString("chevhuzhu").toString();
//        System.out.println(ss);
    }

}
