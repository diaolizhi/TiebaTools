package domain;

import com.google.gson.Gson;

/**
 * @program: tiebatool
 * @description: 同一个贴子不同回复
 * @author: diaolizhi
 * @create: 2018-12-21 21:49
 **/
public class TinyReplyRecord {

    private String replyPostId;
    private String replyContent;
    private String replyTime;

    public TinyReplyRecord(String replyPostId, String replyContent, String replyTime) {
        this.replyPostId = replyPostId;
        this.replyContent = replyContent;
        this.replyTime = replyTime;
    }

    public String getReplyContent() {
        return replyContent;
    }

    public void setReplyContent(String replyContent) {
        replyContent = replyContent;
    }

    public String getReplyPostId() {
        return replyPostId;
    }

    public void setReplyPostId(String replyPostId) {
        this.replyPostId = replyPostId;
    }

    public String getReplyTime() {
        return replyTime;
    }

    public void setReplyTime(String replyTime) {
        this.replyTime = replyTime;
    }

    @Override
    public String toString() {
        return new Gson().toJson(this);
    }
}
