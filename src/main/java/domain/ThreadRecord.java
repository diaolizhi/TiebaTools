package domain;

import com.google.gson.Gson;

/**
 * @program: tiebatool
 * @description: 进入某一个贴吧之后，获取到的一个【帖子】的信息
 * @author: diaolizhi
 * @create: 2018-12-22 21:51
 **/
public class ThreadRecord {

//    帖子id   标题  回复人数  作者 内容（可能由多个组成）

    private String tid;
    private String title;
    private int replyNum;
    private String author;
    private String content;
    private int sex;

    public ThreadRecord(String tid, String title, int replyNum, String author, String content, int sex) {
        this.tid = tid;
        this.title = title;
        this.replyNum = replyNum;
        this.author = author;
        this.content = content;
        this.sex = sex;
    }

    public String getTid() {
        return tid;
    }

    public void setTid(String tid) {
        this.tid = tid;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getReplyNum() {
        return replyNum;
    }

    public void setReplyNum(int replyNum) {
        this.replyNum = replyNum;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public int getSex() {
        return sex;
    }

    public void setSex(int sex) {
        this.sex = sex;
    }

    @Override
    public String toString() {
        return new Gson().toJson(this);
    }
}
