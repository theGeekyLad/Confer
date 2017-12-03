package com.blacklotus.confer;

/**
 * Created by Mohit on 03-12-2017.
 */

public class Tools {

    public static String formatEmail(String email) {
        String str = "";
        for (int i=0; i<email.length(); i++) {
            char ch = email.charAt(i);
            if (ch == '.')
                str += '^';
            else
                str += ch;
        }
        return str;
    }

    public static String unFormatEmail(String email) {
        String str = "";
        for (int i=0; i<email.length(); i++) {
            char ch = email.charAt(i);
            if (ch == '^')
                str += '.';
            else
                str += ch;
        }
        return str;
    }

}

