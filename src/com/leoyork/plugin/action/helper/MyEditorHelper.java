package com.leoyork.plugin.action.helper;

import com.intellij.openapi.editor.CaretModel;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.SelectionModel;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author leoyork
 * @date 9:54 2018/6/13
 */
public class MyEditorHelper {

    private static Pattern PACKAGE_ALL = Pattern.compile("import .*\\*;");

    /**
     * 获取所选行的所有内容并选中所有选中行
     * @param var1
     * @return
     */
    public static String getTextFromSelectRow(final Editor var1) {
        SelectionModel sm = var1.getSelectionModel();
        CaretModel cm = var1.getCaretModel();

        int start = sm.getSelectionStart();
        int end = sm.getSelectionEnd();

        //光标移到第一行并选中该行获取首行头坐标
        cm.moveToOffset(start);
        sm.selectLineAtCaret();
        int newStart = sm.getSelectionStart();

        //清除选中，避免重复选中导致取消选中
        sm.removeSelection();

        //将光标移到首行头坐标
        cm.moveToOffset(newStart);

        //从首行头坐标向后选中直到选中末尾大于等于原末尾
        //9999作用为单行最大长度，每次换行可以选取最大9999
        cm.moveCaretRelatively(9999, 0, true, false, false);
        while (sm.getSelectionEnd() < end) {
            //参数含义：
            //向后移动字符数
            //向下移动行数
            //移动同时是否选中
            //4 5 不清楚
            //由于第一个参数无法跨行，每次选取一行
            cm.moveCaretRelatively(9999, 1, true, false, false);
        }

        //获取选中的内容
        return sm.getSelectedText();
    }

    /**
     * 获取当前选中内容并取消选中将光标移动至末尾
     * @param var1
     * @return
     */
    public static Map<String, String> getTextFromSelection(final Editor var1) {
        Map<String, String> map = new HashMap<String, String>();
        SelectionModel sm = var1.getSelectionModel();
        CaretModel cm = var1.getCaretModel();

        int end = sm.getSelectionEnd();
        String txt = sm.getSelectedText();

        cm.moveCaretRelatively(9999, 0, true, false, false);
        String context = getTextAbove(var1);

        cm.moveToOffset(end);
        cm.moveCaretRelatively(9999, 0, true, false, false);
        sm.removeSelection();
        map.put("txt", txt);
        map.put("context", context);

        return map;
    }

    /**
     * 获取光标位置至文件头的所有内容
     * @param var1
     * @return
     */
    public static String getTextAbove(final Editor var1){

        SelectionModel sm = var1.getSelectionModel();
        CaretModel cm = var1.getCaretModel();

        sm.removeSelection();
        cm.moveCaretRelatively(-9999, 0, true, false, false);
        while (sm.getSelectionStart() > 0) {
            //参数含义：
            //向后移动字符数
            //向下移动行数
            //移动同时是否选中
            //4 5 不清楚
            //由于第一个参数无法跨行，每次选取一行
            cm.moveCaretRelatively(-9999, -1, true, false, false);
        }
        String txt = sm.getSelectedText();
        sm.removeSelection();
        return txt;
    }

    /**
     * 获取当前文件中引入的该类的包名
     * @return
     */
    public static String getClassName(String name, String context){
        //获取类名
        StringBuffer sb = new StringBuffer();
        Pattern pattern = Pattern.compile("([A-Z][a-z]*)* *"+name+" *=");
        Matcher matcher = pattern.matcher(context);
        if(matcher.find()) {
            String group = matcher.group();
            sb.append(group.trim().substring(0, matcher.group().indexOf(name)-1));
        } else {
            pattern = Pattern.compile("([A-Z][a-z]*)* *" + name + " *;");
            matcher = pattern.matcher(context);
            if(matcher.find()) {
                String group = matcher.group();
                sb.append(group.trim().substring(0, matcher.group().indexOf(name)));
            }
        }
        String className = sb.toString();

        sb = new StringBuffer();
        pattern = Pattern.compile("import .*"+className);
        matcher = pattern.matcher(context);
        if(matcher.find()) {
            String row = matcher.group();
            sb.append(row.substring("import ".length()));
        } else {
            matcher = PACKAGE_ALL.matcher(context);
            while (matcher.find()) {
                if(sb.length() != 0) {
                    sb.append(",");
                }
                String group = matcher.group();
                sb.append(group.substring("import ".length(), group.indexOf("*")) + className);
            }
        }

        return sb.toString();
    }

    /**
     * 复制内容到剪切板
     * @param writeMe 复制内容
     */
    public static void setSysClipboardText(String writeMe) {
        Clipboard clip = Toolkit.getDefaultToolkit().getSystemClipboard();
        Transferable tText = new StringSelection(writeMe);
        clip.setContents(tText, null);
    }

}
