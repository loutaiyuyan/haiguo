package com.xingchuang.haiguo.chooseicon;

public class FolderBean {

    /**
     * 当前文件夹路径
     */
    private String dir;
    /**
     * 当前文件夹第一个图片的路径
     */
    private String firstImgPath;
    /**
     * 当前文件夹名称
     */
    private String name;
    /**
     * 当前文件夹的图片的数量
     */
    private int count;

    public String getDir() {
        return dir;
    }

    public void setDir(String dir) {
        this.dir = dir;
        int lastIndexOf = this.dir.lastIndexOf("/");
        this.name = this.dir.substring(lastIndexOf + 1);

    }

    public String getFirstImgPath() {
        return firstImgPath;
    }

    public void setFirstImgPath(String firstImgPath) {
        this.firstImgPath = firstImgPath;
    }

    public void setName(String name){this.name=name;}
    public String getName() {
        return name;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }


}
