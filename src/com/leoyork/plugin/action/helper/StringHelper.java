package com.leoyork.plugin.action.helper;

public class StringHelper {

    public static String getClassName(String name) {
        StringBuffer sb = new StringBuffer();

        do{
            sb.append(name.substring(0, 1).toUpperCase());
            sb.append(name.substring(1, name.indexOf('_')));
            name = name.substring(name.indexOf('_') + 1);
        } while (name.indexOf('_') >= 0);

        return sb.toString();
    }

}
