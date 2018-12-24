package domain;

import java.util.ArrayList;

/**
 * @program: tiebatool
 * @description: 【别人关注的贴吧】的信息，包括是否还有下一页，以及贴吧列表
 * @author: diaolizhi
 * @create: 2018-12-24 11:24
 **/
public class UserForumsInfo {

    private int hasMore;
    private ArrayList<OneForumInfo> oneForumInfos;

    public UserForumsInfo() {
    }

    public int getHasMore() {
        return hasMore;
    }

    public void setHasMore(int hasMore) {
        this.hasMore = hasMore;
    }

    public ArrayList<OneForumInfo> getOneForumInfos() {
        return oneForumInfos;
    }

    public void setOneForumInfos(ArrayList<OneForumInfo> oneForumInfos) {
        this.oneForumInfos = oneForumInfos;
    }
}
