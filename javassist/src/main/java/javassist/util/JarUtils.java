package javassist.util;

public enum JarUtils {
    INSTANCE;

    private String rootProjectPath;

    public void setRootProjectPath(String rootProjectPath) {
        this.rootProjectPath = rootProjectPath;
    }

    public boolean isProjectJar(String filePath){
        return filePath.startsWith(rootProjectPath) && filePath.endsWith(".jar");
    }
}
