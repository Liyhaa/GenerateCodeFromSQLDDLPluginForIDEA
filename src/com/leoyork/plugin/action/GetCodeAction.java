package com.leoyork.plugin.action;

import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.editor.*;
import com.intellij.openapi.editor.actionSystem.EditorAction;
import com.intellij.openapi.editor.actionSystem.EditorWriteActionHandler;
import com.leoyork.plugin.action.helper.MyEditorHelper;
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

            //获取选中的内容
            String txt = MyEditorHelper.getTextFromSelectRow(var1);

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
        StringBuilder code = new StringBuilder();
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

            code.append(String.format(tempStr, comment, type, name));

        }

        return code.toString();
    }

}
