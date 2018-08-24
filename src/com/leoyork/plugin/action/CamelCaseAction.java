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
 * @date 2018/8/23 16:32
 */
public class CamelCaseAction extends EditorAction {

    private static Pattern line1 = Pattern.compile("[A-Z]([a-z]|[A-Z])* +([a-z])+_+([a-z]|_)*");
    private static Pattern line2 = Pattern.compile("([a-z])+_+([a-z]|_)* +=");
    private static Pattern camel1 = Pattern.compile("[A-Z]([a-z]|[A-Z])* +[a-z]([a-z]|[A-Z])*");
    private static Pattern camel2 = Pattern.compile("[a-z]([a-z]|[A-Z])* +=");

    protected CamelCaseAction() {
        super(new CamelCaseAction.Handler());
    }

    public static class Handler extends EditorWriteActionHandler {
        public Handler() { }

        @Override
        public void executeWriteAction(final Editor var1, @Nullable Caret caret, DataContext var2) {
            //获取选中的内容
            String txt = MyEditorHelper.getTextFromSelectRow(var1);

            String code = getAnotherStyle(txt);

            EditorModificationUtil.insertStringAtCaret(var1, code, true, false);

        }

        private String getAnotherStyle(String txt) {
            StringBuilder sb = new StringBuilder();
            String[] singleLines = txt.split("\n");
            for (String singleLine : singleLines) {
                sb.append(lineToLine(singleLine)).append("\n");
            }
            sb.deleteCharAt(sb.length()-1);
            return sb.toString();
        }

        private String lineToLine(String line) {
            String newLine = line;
            Matcher matcher = line1.matcher(line);
            if(matcher.find()) {

                String txt = matcher.group();
                String variable = txt.substring(txt.lastIndexOf(" ") + 1);
                newLine = line.replaceAll(variable, lineToCamel(variable));

            } else {
                matcher = line2.matcher(line);
                if(matcher.find()) {

                    String txt = matcher.group();
                    String variable = txt.substring(0, txt.indexOf(" "));
                    newLine = line.replaceAll(variable, camelToLine(variable));

                } else {
                    matcher = camel1.matcher(line);
                    if(matcher.find()) {

                        String txt = matcher.group();
                        String variable = txt.substring(txt.lastIndexOf(" ") + 1);
                        newLine = line.replaceAll(variable, camelToLine(variable));

                    } else {
                        matcher = camel2.matcher(line);
                        if(matcher.find()) {

                            String txt = matcher.group();
                            String variable = txt.substring(0, txt.indexOf(" "));
                            newLine = line.replaceAll(variable, camelToLine(variable));

                        }
                    }
                }
            }
            return newLine;
        }

        private String lineToCamel(String line) {
            String[] words = line.split("_");
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < words.length; i++) {
                String word = words[i];
                if(i > 0) {
                    sb.append(word.substring(0, 1).toUpperCase());
                    sb.append(word.substring(1));
                } else {
                    sb.append(word);
                }
            }
            return sb.toString();
        }

        private String camelToLine(String camel) {
            byte[] bytes = camel.getBytes();
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < bytes.length; i++) {
                byte b = bytes[i];
                if(i > 0) {
                    if(b >= 65 && b <= 90) {
                        sb.append('_');
                        b += 32;
                    }
                }
                sb.append((char) b);
            }

            return sb.toString();
        }
    }
}
