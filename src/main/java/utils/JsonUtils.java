package utils;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import domain.ReplyRecord;
import domain.TinyReplyRecord;

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
            String replyPostId = "";
            String replyTime = "";

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


//    todo:该楼作者 该楼内容 该楼id 帖子标题 帖子id 楼中楼内容

//    public static

}
