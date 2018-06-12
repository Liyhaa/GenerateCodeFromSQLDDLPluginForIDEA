package com.leoyork.plugin.action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.editor.*;
import com.intellij.openapi.editor.actionSystem.EditorAction;
import com.intellij.openapi.editor.actionSystem.EditorWriteActionHandler;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.ui.NonEmptyInputValidator;
import com.intellij.openapi.util.registry.Registry;
import org.jetbrains.annotations.Nullable;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author leoyork
 * @date 11:04 2018/6/12
 */
public class GetCodeAction extends EditorAction {

    protected GetCodeAction() {
        super(new GetCodeAction.Handler());
    }

    public static class Handler extends EditorWriteActionHandler {
        public Handler() { }

        @Override
        public void executeWriteAction(final Editor var1, @Nullable Caret caret, DataContext var2) {
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
            String txt = sm.getSelectedText();

            if (txt != null && !"".equals(txt.trim())) {
                String pattern1 = "`.*`";
                Pattern r = Pattern.compile(pattern1);
                Matcher m = r.matcher(txt);
                if(m.find()) {
                    //SQL语句
                    String code = getCodeFromSql(txt);
                    //覆盖插入
                    EditorModificationUtil.insertStringAtCaret(var1, code, true, false);
                }
            }
        }
    }

    private static String getCodeFromSql(String sql) {
        String code = "";
        String[] sqls = sql.split(",");

        for (String singleSql:sqls) {
            String tempStr = "\n    /** %s */\n    private %s %s;\n";
            String pattern1 = "`.*`";
            String pattern2 = "` [a-z0-9\\(\\)]* ";
            String pattern3 = "COMMENT '.*'";
            Pattern r = Pattern.compile(pattern1);
            Matcher m = r.matcher(singleSql);

            String name = "not_found";
            if(m.find()) {
                String sqlType = m.group(0);
                name = sqlType.substring(sqlType.indexOf("`")+1, sqlType.lastIndexOf("`"));
            }

            r = Pattern.compile(pattern2);
            m = r.matcher(singleSql);

            String type = "not_found";
            if(m.find()){
                String sqlType = m.group(0);
                if(sqlType.contains("(")) {
                    type = sqlType.substring(sqlType.indexOf("` ")+2, sqlType.indexOf("("));
                } else {
                    type = sqlType.substring(sqlType.indexOf("` ")+2, sqlType.lastIndexOf(" "));
                }
            }
            if("tinyint".equals(type)) {
                type = "Byte";
            } else if("bigint".equals(type)) {
                type = "Long";
            } else if("varchar".equals(type)) {
                type = "String";
            } else if("int".equals(type)) {
                type = "Integer";
            } else if("decimal".equals(type)) {
                type = "BigDecimal";
            } else if("datetime".equals(type)) {
                type = "Date";
            }
            r = Pattern.compile(pattern3);
            m = r.matcher(singleSql);

            String comment = "not_found";
            if(m.find()){
                String sqlType = m.group(0);
                comment = sqlType.substring(sqlType.indexOf("COMMENT '")+9, sqlType.lastIndexOf("'"));
            }

            code += String.format(tempStr, comment, type, name);

        }

        return code;
    }

    private static int countStr(String source, String str){
        int start = 0;
        int count = 0;
        while((start = source.indexOf(str,start)) >= 0){
            start += str.length();
            count ++;
        }
        return count;
    }

}
