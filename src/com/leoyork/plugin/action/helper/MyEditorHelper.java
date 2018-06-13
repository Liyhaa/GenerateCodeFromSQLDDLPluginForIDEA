package com.leoyork.plugin.action.helper;

import com.intellij.openapi.editor.CaretModel;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.SelectionModel;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;

/**
 * @author leoyork
 * @date 9:54 2018/6/13
 */
public class MyEditorHelper {

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
     * 复制内容到剪切板
     * @param writeMe 复制内容
     */
    public static void setSysClipboardText(String writeMe) {
        Clipboard clip = Toolkit.getDefaultToolkit().getSystemClipboard();
        Transferable tText = new StringSelection(writeMe);
        clip.setContents(tText, null);
    }

}
