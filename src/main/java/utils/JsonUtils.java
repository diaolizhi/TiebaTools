package utils;

import com.google.gson.*;
import domain.*;

import java.util.ArrayList;

/**
 * @program: tiebatool
 * @description: 存放解析 Json 的静态方法
 * @author: diaolizhi
 * @create: 2018-12-22 12:26
 **/
public class JsonUtils {

    /**
    * @Description: 解析【userpost】接口返回的数据
    * @Param: [res]
    * @return: domain.ReplyRecord[]
    * @Author: diaolizhi
    * @Date: 2018/12/22
    */
    public static ReplyRecord[] recordsParser(String res) {
        JsonElement element = new JsonParser().parse(res);
        JsonObject object = element.getAsJsonObject();
        JsonArray array = object.getAsJsonArray("post_list");

        ReplyRecord[] records = new ReplyRecord[array.size()];

        for (int i=0; i<array.size(); i++) {
            object = array.get(i).getAsJsonObject();
            String forum_id = object.get("forum_id").getAsString();
            String thread_id = object.get("thread_id").getAsString();
            String post_id = object.get("post_id").getAsString();
            String forumName = object.get("forum_name").getAsString();
            String title = object.get("title").getAsString();

            JsonArray contents = object.get("content").getAsJsonArray();

            String replyContent = "";
            String replyPostId;
            String replyTime;

            TinyReplyRecord[] tinyReplyRecords = new TinyReplyRecord[contents.size()];

//            contents 是同一个帖子的不同回复
//            contents[j] 是一条回复
            for (int j=0; j<contents.size(); j++) {
                JsonArray postList = contents.get(j).getAsJsonObject().get("post_content").getAsJsonArray();

//                一个 postList 其实是一条回复，但是由不同部分组成
                for (int k=0; k<postList.size(); k++) {
                    JsonObject postObject = postList.get(k).getAsJsonObject();
                    if (postObject.get("type").getAsInt() == 10) {
                        System.out.println("这是一条语音，暂不支持。");
                        continue;
                    }
                    replyContent += postObject.get("text").getAsString();
                }

                replyPostId = contents.get(j).getAsJsonObject().get("post_id").getAsString();
                replyTime = contents.get(j).getAsJsonObject().get("create_time").getAsString();

//                格式化时间戳
                replyTime = OtherUtils.formatTime(replyTime);

                tinyReplyRecords[j] = new TinyReplyRecord(replyPostId, replyContent, replyTime);

                replyContent = "";
                replyPostId = "";
                replyTime = "";
            }


            String quotePostId = "";
            String quoteUserName = "";
            String quoteContent = "";

            try {
                JsonObject quote = object.get("quote").getAsJsonObject();
                quotePostId = quote.get("post_id").getAsString();
                quoteUserName = quote.get("user_name").getAsString();
                quoteContent = quote.get("content").getAsString();
            } catch (Exception e) {

            }

            records[i] = new ReplyRecord(forum_id, thread_id, post_id, quotePostId,
                    quoteUserName, quoteContent, forumName, title, tinyReplyRecords);
        }

        return records;
    }

    /**
    * @Description: 解析【帖子】列表
    * @Param: [res]
    * @return: domain.ThreadRecord[]
    * @Author: diaolizhi
    * @Date: 2018/12/24
    */
    public static ThreadRecord[] threadListParser(String res) {

        JsonObject root = getJsonObject(res);
        JsonArray threadList = root.get("thread_list").getAsJsonArray();

        ThreadRecord[] threadRecords = new ThreadRecord[threadList.size()];

        for (int i=0; i<threadList.size(); i++) {
            JsonObject threadObject = threadList.get(i).getAsJsonObject();
            String tid = threadObject.get("tid").getAsString();
            String title = threadObject.get("title").getAsString();
            int replyNum = threadObject.get("reply_num").getAsInt();

            JsonObject authorInfo = threadObject.get("author").getAsJsonObject();
            String author = authorInfo.get("name").getAsString();
            int sex = authorInfo.get("sex").getAsInt();

            JsonArray contents = threadObject.get("abstract").getAsJsonArray();

            String content = "";

            for (int j=0; j<contents.size(); j++) {
                content += contents.get(j).getAsJsonObject().get("text");
            }

            threadRecords[i] = new ThreadRecord(tid, title, replyNum, author, content, sex);
        }

        return threadRecords;
    }

    /**
    * @Description: 解析 /c/f/forum/like 接口返回的数据
    * @Param: [res]
    * @return: domain.UserForumsInfo
    * @Author: diaolizhi
    * @Date: 2018/12/24
    */
    public static UserForumsInfo userForumListParser(String res) {
        JsonObject root = getJsonObject(res);

        int hasMore = root.get("has_more").getAsInt();

//        userForumsInfo 包含的信息：1、是否还有下一页。 2、贴吧列表(使用 ArrayList<OneForumInfo> 保存)
        UserForumsInfo userForumsInfo = new UserForumsInfo();
        userForumsInfo.setHasMore(hasMore);

        JsonObject forumList = root.get("forum_list").getAsJsonObject();

        ArrayList<OneForumInfo> forumInfos = new ArrayList<>();

//        获取【官方贴吧】，可能不存在。
        try {
            JsonArray gconList = forumList.get("gconforum").getAsJsonArray();
            forumListParser(gconList, forumInfos);
        } catch (NullPointerException e) {
        }

        try {

//        获取【普通贴吧】，普通贴吧也可能不存在。
            JsonArray nonList = forumList.get("non-gconforum").getAsJsonArray();
            forumListParser(nonList, forumInfos);

            userForumsInfo.setOneForumInfos(forumInfos);
        } catch (Exception e) {
            //不做处理
        }



        return userForumsInfo;

    }

    private static void forumListParser(JsonArray nonList, ArrayList<OneForumInfo> forumInfos) {
        for (int i=0; i<nonList.size(); i++) {
            String id = nonList.get(i).getAsJsonObject().get("id").getAsString();
            String name = nonList.get(i).getAsJsonObject().get("name").getAsString();
            String levelId = nonList.get(i).getAsJsonObject().get("level_id").getAsString();
            String curScore = nonList.get(i).getAsJsonObject().get("cur_score").getAsString();
            String levelupScore = nonList.get(i).getAsJsonObject().get("levelup_score").getAsString();

            forumInfos.add(new OneForumInfo(id, name, levelId, curScore, levelupScore));
        }
    }

    public static String[] userInfoParser(String res) {
        String[] info = new String[2];
        JsonObject userInfo = getJsonObject(res).getAsJsonObject();

        userInfo = userInfo.getAsJsonObject("user");

        String id = userInfo.get("id").getAsString();
        info[0] = id;

        String name = userInfo.get("name").getAsString();
        info[1] = name;

        return info;
    }

    public static String fidParser(String res) {
        JsonObject root = getJsonObject(res);
        JsonObject data = root.getAsJsonObject("data");
        JsonElement fid = data.get("fid");
        String fidStr = fid.getAsString();
        return fidStr;
    }

    public static String tbsParser(String res) {
        JsonObject root = getJsonObject(res);
        return root.get("tbs").getAsString();
    }

    private static JsonObject getJsonObject(String res) {
        return new JsonParser().parse(res).getAsJsonObject();
    }

    public static String fuidParser(String res) {
        JsonObject root = getJsonObject(res);
        JsonArray userInfo = root.get("user_info").getAsJsonArray();
        return userInfo.get(0).getAsJsonObject().get("user_id").getAsString();
    }

    /**
    * @Description: /c/f/pb/floor 接口，保存某一层楼下某一个人的回复，只保存文本
    * @Param: [res, username]
    * @return: java.util.ArrayList<java.lang.String>
    * @Author: diaolizhi
    * @Date: 2018/12/25
    */
    public static ArrayList<String> subPostsParser(String res, String username) {
        JsonObject root = getJsonObject(res);
        JsonArray subpostList = root.get("subpost_list").getAsJsonArray();

        if (subpostList.size() == 0) {
            return null;
        }

        ArrayList<String> posts = new ArrayList<>();
        for (int i=0; i<subpostList.size(); i++) {
            JsonObject supPost = subpostList.get(i).getAsJsonObject();
            JsonArray contents = supPost.get("content").getAsJsonArray();

            String name = supPost.get("author").getAsJsonObject().get("name").getAsString();

            if (name.equals(username)) {
                String text = new String();
                for (int j=0; j<contents.size(); j++) {
                    JsonObject content = contents.get(j).getAsJsonObject();

                    if (content.get("type").getAsString().equals("0")) {
                        text += content.get("text").getAsString();
                    }

                }
                text = text.replaceFirst("回复.*?:", "")
                        .replace(" ", "");
                posts.add(text);
            }
        }
        return posts;
    }

}
