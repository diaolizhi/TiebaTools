package domain;

import sendHttp.HttpClient;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * @program: tiebatool
 * @description: 贴吧用户
 * @author: diaolizhi
 * @create: 2018-11-12 14:31
 **/
public class TBUser {

    private ArrayList<String[]> forumList;
    private int forumListNum;
    private ArrayList<String> replyAdds;
    private String BDUSS;
    private String userName;
    private String uid;

    private final HttpClient client = new HttpClient();

    public TBUser(String BDUSS) {
        this.BDUSS = BDUSS;
        getUserInfo();
    }

    /**
    * @Description: 更新所关注的贴吧
    * @Param: []
    * @return: void
    * @Author: diaolizhi
    * @Date: 2018/11/12
    */
    public void updateForumList() {
        forumList = client.getForumList(BDUSS);
        forumListNum = forumList.size();
    }

    /**
    * @Description: 登录，获取用户信息
    * @Param: []
    * @return: void
    * @Author: diaolizhi
    * @Date: 2018/11/12
    */
    private void getUserInfo() {
        client.getUserInfo(this);
    }

    /**
    * @Description: 进行签到
    * @Param: []
    * @return: void
    * @Author: diaolizhi
    * @Date: 2018/11/12
    */
    public void Sign() {
        client.Sign(this.BDUSS, this.forumList, 0);
    }

    /** 
    * @Description: 回复帖子 
    * @Param: [] 
    * @return: void 
    * @Author: diaolizhi
    * @Date: 2018/11/12 
    */ 
    public void reply() {
        
    }

    public void cloneForums(String userName) {
        client.cloneForums(this.BDUSS, userName);
    }

    public void setBDUSS(String BDUSS) {
        this.BDUSS = BDUSS;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public ArrayList<String[]> getForumList() {
        return forumList;
    }

    public ArrayList<String> getReplyAdds() {
        return replyAdds;
    }

    public String getBDUSS() {
        return BDUSS;
    }

    public String getUserName() {
        return userName;
    }

    public int getForumListNum() {
        return forumListNum;
    }

    public void setForumListNum(int forumListNum) {
        this.forumListNum = forumListNum;
    }

    public boolean isLogin() {
        return this.uid == null;
    }
}
