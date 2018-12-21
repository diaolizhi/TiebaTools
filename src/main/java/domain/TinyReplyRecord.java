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
    private String ReplyContent;

    public TinyReplyRecord(String replyPostId, String replyContent) {
        this.replyPostId = replyPostId;
        ReplyContent = replyContent;
    }

    public String getreplyPostId() {
        return replyPostId;
    }

    public void setreplyPostId(String replyPostId) {
        this.replyPostId = replyPostId;
    }

    public String getReplyContent() {
        return ReplyContent;
    }

    public void setReplyContent(String replyContent) {
        ReplyContent = replyContent;
    }

    @Override
    public String toString() {

        return new Gson().toJson(this);

//        return "TinyReplyRecord{" +
//                "replyPostId='" + replyPostId + '\'' +
//                ", ReplyContent='" + ReplyContent + '\'' +
//                '}';
    }
}
