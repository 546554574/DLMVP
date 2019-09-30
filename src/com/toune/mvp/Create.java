package com.toune.mvp;

import com.intellij.codeInsight.CodeInsightActionHandler;
import com.intellij.codeInsight.generation.actions.BaseGenerateAction;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiUtilBase;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;

public class Create extends BaseGenerateAction {

    private PsiFile mFile;
    private PsiClass mClass;
    private PsiElementFactory mFactory;
    private String activityName;
    private String viewIName;
    private String presenterName;
    private PsiDirectory mvpViewDir;
    private PsiDirectory mvpPresenterDir;

    public Create() {
        super(null);
    }

    protected Create(CodeInsightActionHandler handler) {
        super(handler);
    }

    @Override
    public void actionPerformed(AnActionEvent event) {
        // TODO: insert action logic here
        //获取当前点击工程
        Project project = event.getData(PlatformDataKeys.PROJECT);
        Editor editor = event.getData(PlatformDataKeys.EDITOR);

        actionPerformedImpl(project, editor);
    }


    @Override
    public void actionPerformedImpl(@NotNull Project project, Editor editor) {
        mFile = PsiUtilBase.getPsiFileInEditor(editor, project); //获取点击的文件
        mClass = getTargetClass(editor, mFile); //获取点击的类
        if (mClass.getName() == null) {
            return;
        }
        mFactory = JavaPsiFacade.getElementFactory(project);
        createMVPDir(); //创建mvp文件夹
        activityName = mClass.getName();
        creatMVPFile();

    }

    private void creatMVPFile() {
        viewIName = mClass.getName() + "View"; //viewI的名称
        presenterName = mClass.getName() + "Presenter"; //presenter的名称

        boolean hasPresenter = false; //是否包含presenter
        boolean hasViewI = false; //是否包含viewI

        //查找是否已经包含有mvp文件，如果有的话，则不再创建
        for (PsiFile f : mvpViewDir.getFiles()) {
            if (f.getName().contains("View")) {
                String realName = f.getName().split("View")[0];
                if (mClass.getName().contains(realName)) {
                    hasViewI = true;
                    viewIName = f.getName().replace(".java", "");
                }
            }
        }

        for (PsiFile f : mvpPresenterDir.getFiles()) {
            if (f.getName().contains("Presenter")) {
                String realName = f.getName().split("Presenter")[0];
                if (mClass.getName().contains(realName)) {
                    hasPresenter = true;
                    presenterName = f.getName().replace(".java", "");
                }
            }

        }

        if (!hasPresenter) {
            createPresenter();
        }
        if (!hasViewI) {
            createView();
        }
    }

    private void createMVPDir() {
        mvpViewDir = mFile.getParent().getParentDirectory().findSubdirectory("view"); //获取mvp  view文件夹
        mvpPresenterDir = mFile.getParent().getParentDirectory().findSubdirectory("presenter"); //获取mvp  presenter文件夹
        if (mvpViewDir == null) {
            //如果没有找到mvp文件夹，则创建一个
            mvpViewDir = mFile.getParent().getParentDirectory().createSubdirectory("view");
        }
        if (mvpPresenterDir == null) {
            //如果没有找到mvp文件夹，则创建一个
            mvpPresenterDir = mFile.getParent().getParentDirectory().createSubdirectory("presenter");
        }

    }

    public static String getFilePackageName(VirtualFile dir) {
        if(!dir.isDirectory()) {
            // 非目录的取所在文件夹路径
            dir = dir.getParent();
        }
        String path = dir.getPath().replace("/", ".");
        String preText = "src.main.java";
        int preIndex = path.indexOf(preText) + preText.length() + 1;
        path = path.substring(preIndex);
        return path;
    }
    /**
     * 生成该代码生成的时间
     * @return
     */
    private String getHeaderAnnotation() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String time = sdf.format(System.currentTimeMillis());
        String annotation = "/**\n" +
                " * Created  on " + time + ".\n" +
                " */";
        return annotation;
    }

    /**
     * 将字符串写入文件
     * @param javatempelt
     * @param fileName
     */
    public static void string2Stream(String javatempelt, String fileName) {
        File file=new File(fileName);
        if (file.exists()){
            file.delete();
        }else {
            if (!file.getParentFile().exists()){
                file.getParentFile().mkdirs();
            }
        }

        try {
            PrintWriter printWriter=new PrintWriter(file);
            printWriter.print(javatempelt);
            printWriter.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }


    private void createPresenter() {

        //创建文件
        PsiFile presenterFile = mvpPresenterDir.createFile(presenterName + ".java");

        //生成要写入的字符串
        StringBuffer modelText = new StringBuffer();
        modelText.append("package " + getFilePackageName(mvpPresenterDir.getVirtualFile()) + ";\n\n\n");

        modelText.append("import "+getFilePackageName(mvpPresenterDir.getParent().getVirtualFile())+".view."+viewIName+";\n");
        modelText.append("import "+getFilePackageName(mvpPresenterDir.getParent().getVirtualFile())+".base.BasePresenterImpl;" + "\n\n\n");

        modelText.append(getHeaderAnnotation() + "\n");

        modelText.append("public class " + presenterName + " extends BasePresenterImpl<"+viewIName+">{\n\n\n");
//        modelText.append(viewIName + " mView;\n");
//        modelText.append(" @Inject\n");
//        modelText.append(modelName + " mModel;\n");
//        modelText.append("   public " + presenterName + "(" + viewIName + " arg) {\n" +
//                "        super(arg);\n" +
//                "        this.mView = arg;\n" +
//                "        this.mModel = this.mView.getActivityComponent().get" + modelName + "();\n" +
//                "\n" +
//                "    }\n");
//        modelText.append("    @Override\n" +
//                "    public BaseModel getBaseModel() {\n" +
//                "        return mModel;\n" +
//                "    }");
        modelText.append("}");

        //将字符串写入文件
        string2Stream(modelText.toString(), presenterFile.getVirtualFile().getPath());
    }



    private void createView() {

        //创建文件
        PsiFile presenterFile = mvpViewDir.createFile(viewIName + ".java");

        //生成要写入的字符串
        StringBuffer modelText = new StringBuffer();
        modelText.append("package " + getFilePackageName(mvpViewDir.getVirtualFile()) + ";\n\n\n");

        modelText.append("import "+getFilePackageName(mvpPresenterDir.getParent().getVirtualFile())+".base.BaseView;" + "\n\n\n");

        modelText.append(getHeaderAnnotation() + "\n");

        modelText.append("public interface " + viewIName + " extends BaseView{\n\n\n");
//        modelText.append(viewIName + " mView;\n");
//        modelText.append(" @Inject\n");
//        modelText.append(modelName + " mModel;\n");
//        modelText.append("   public " + presenterName + "(" + viewIName + " arg) {\n" +
//                "        super(arg);\n" +
//                "        this.mView = arg;\n" +
//                "        this.mModel = this.mView.getActivityComponent().get" + modelName + "();\n" +
//                "\n" +
//                "    }\n");
//        modelText.append("    @Override\n" +
//                "    public BaseModel getBaseModel() {\n" +
//                "        return mModel;\n" +
//                "    }");
        modelText.append("}");

        //将字符串写入文件
        string2Stream(modelText.toString(), presenterFile.getVirtualFile().getPath());
    }
}
