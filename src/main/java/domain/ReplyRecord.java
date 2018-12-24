package domain;

import com.google.gson.Gson;

/**
 * @program: tiebatool
 * @description: 在【一个帖子中的回复】
 * @author: diaolizhi
 * @create: 2018-12-20 21:13
 **/
public class ReplyRecord {

    private String forumId;
    private String threadId;
    private String postId;
    /** 如果是楼中楼下面三个属性才会有值 **/
    private String quotePostId;
    private String quoteUsername;
    private String quoteContent;
    private String forumName;
    private String title;
    private TinyReplyRecord[] tinyReplyRecords;

    public ReplyRecord(String forumId, String threadId, String postId, String quotePostId, String quoteUsername,
                       String quoteContent, String forumName, String title, TinyReplyRecord[] tinyReplyRecords) {
        this.forumId = forumId;
        this.threadId = threadId;
        this.postId = postId;
        this.quotePostId = quotePostId;
        this.quoteUsername = quoteUsername;
        this.quoteContent = quoteContent;
        this.forumName = forumName;
        this.title = title;
        this.tinyReplyRecords = tinyReplyRecords;
    }

    public String getForumId() {
        return forumId;
    }

    public void setForumId(String forumId) {
        this.forumId = forumId;
    }

    public String getThreadId() {
        return threadId;
    }

    public void setThreadId(String threadId) {
        this.threadId = threadId;
    }

    public String getPostId() {
        return postId;
    }

    public void setPostId(String postId) {
        this.postId = postId;
    }

    public String getQuotePostId() {
        return quotePostId;
    }

    public void setQuotePostId(String quotePostId) {
        this.quotePostId = quotePostId;
    }

    public String getQuoteUsername() {
        return quoteUsername;
    }

    public void setQuoteUsername(String quoteUsername) {
        this.quoteUsername = quoteUsername;
    }

    public String getQuoteContent() {
        return quoteContent;
    }

    public void setQuoteContent(String quoteContent) {
        this.quoteContent = quoteContent;
    }

    public String getForumName() {
        return forumName;
    }

    public void setForumName(String forumName) {
        this.forumName = forumName;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    public String toString() {

        return new Gson().toJson(this);
    }

    public TinyReplyRecord[] getTinyReplyRecords() {
        return tinyReplyRecords;
    }

    public void setTinyReplyRecords(TinyReplyRecord[] tinyReplyRecords) {
        this.tinyReplyRecords = tinyReplyRecords;
    }

}
