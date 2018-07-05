package com.leoyork.plugin.action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.editor.Caret;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorModificationUtil;
import com.intellij.openapi.editor.actionSystem.EditorAction;
import com.intellij.openapi.editor.actionSystem.EditorActionHandler;
import com.intellij.openapi.editor.actionSystem.EditorWriteActionHandler;
import com.intellij.psi.*;
import com.intellij.psi.search.GlobalSearchScope;
import com.leoyork.plugin.action.helper.MyEditorHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author leoyork
 * @date 17:48 2018/7/4
 */
public class GetSetterAction extends EditorAction {

    protected GetSetterAction() {
        super(new GetSetterAction.Handler());
    }

    public static class Handler extends EditorWriteActionHandler {
        public Handler() { }

        @Override
        public void executeWriteAction(final Editor var1, @Nullable Caret caret, DataContext var2) {
            //获取选中的内容
            Map<String, String> map = MyEditorHelper.getTextFromSelection(var1);

            String code = getSetter(map.get("txt"), map.get("context"), var2);

            EditorModificationUtil.insertStringAtCaret(var1, code, true, false);


        }

    }

    private static String getSetter(String name, String context, DataContext var2){
        StringBuffer sb = new StringBuffer("\n\n");

        String className = MyEditorHelper.getClassName(name, context);

        //获取Psi对象的方式
        PsiFile psiFile = var2.getData(LangDataKeys.PSI_FILE);
        JavaPsiFacade psiFacade = JavaPsiFacade.getInstance(psiFile.getProject());

//        PsiElementFactory factory = psiFacade.getElementFactory();
//        PsiClass psiClass = factory.createClass(name.substring(0, 1).toUpperCase() + name.substring(1));
        PsiClass psiClass = null;
        for (String path:className.split(",")) {
            psiClass = psiFacade.findClass(path, GlobalSearchScope.everythingScope(psiFile.getProject()));
            if(null != psiClass) {
                break;
            }
        }
        if(null == psiClass) {
            return "";
        }
//        psiFile.getReference();
//        PsiDirectory psiDirectory = psiFile.getParent();
//        List<String> childDirectory = new ArrayList<>();
//        while (psiDirectory.getParent() != null) {
//            if(className.contains(psiDirectory.getName())) {
//                break;
//            }
//        }
//        String relativePath = className.substring(className.indexOf(psiDirectory.getName() + psiDirectory.getName().length()));
//        for (String pathName:relativePath.split(".")) {
//            psiDirectory = psiDirectory.findSubdirectory(pathName);
//        }
//
//        PsiClass psiClass = JavaDirectoryService.getInstance().createClass(psiDirectory, name);

        PsiField[] psiFields = psiClass.getAllFields();
        PsiMethod[] psiMethods = psiClass.getAllMethods();
        StringBuffer allMethods = new StringBuffer();
        for (PsiMethod method:psiMethods) {
            allMethods.append(method.getName());
        }
        for (PsiField psiField:psiFields) {
            String fieldName = psiField.getName();
            String methodName = "set"+fieldName.substring(0, 1).toUpperCase()+fieldName.substring(1);
            if(0 <= allMethods.indexOf(methodName)) {
                sb.append("\t\t" + name + "." + methodName + "();\n");
            }
        }
        sb.deleteCharAt(sb.length()-1);

        return sb.toString();
    }
}
