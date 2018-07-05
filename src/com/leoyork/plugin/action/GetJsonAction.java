package com.leoyork.plugin.action;

import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.editor.Caret;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorModificationUtil;
import com.intellij.openapi.editor.actionSystem.EditorAction;
import com.intellij.openapi.editor.actionSystem.EditorWriteActionHandler;
import com.leoyork.plugin.action.helper.MyEditorHelper;
import org.jetbrains.annotations.Nullable;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author leoyork
 * @date 9:51 2018/6/13
 */
public class GetJsonAction extends EditorAction {

    protected GetJsonAction() {
        super(new GetJsonAction.Handler());
    }

    public static class Handler extends EditorWriteActionHandler {
        public Handler() { }

        @Override
        public void executeWriteAction(final Editor var1, @Nullable Caret caret, DataContext var2) {
            //获取选中的内容
            String txt = MyEditorHelper.getTextFromSelectRow(var1);

            //复制到剪切板
            MyEditorHelper.setSysClipboardText(getJsonFromFields(txt));
        }

    }

    private static String getJsonFromFields(String source){
        String pattern = " [^ ]*;";
        Pattern r = Pattern.compile(pattern);

        StringBuilder sb = new StringBuilder();
        String[] rows = source.split("\n");
        for (String row:rows) {
            if(!row.contains("private") && !row.contains("public") && !row.contains(";")) {
                continue;
            }
            Matcher m = r.matcher(row);
            if(m.find()) {
                String word = m.group(0);
                word = word.substring(1, word.length()-1);
                sb.append(word + ": '',\n");
            }
        }
        sb.deleteCharAt(sb.length()-1);
        return sb.toString();
    }
}
