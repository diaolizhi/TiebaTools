import com.google.gson.*;
import domain.ReplyRecord;
import domain.TinyReplyRecord;
import netscape.javascript.JSObject;
import okhttp3.*;
import okhttp3.internal.http2.Header;
import okio.BufferedSink;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.TreeMap;

/**
 * @program: tiebatool
 * @description: http 客户端，用来发送请求的类
 * @author: diaolizhi
 * @create: 2018-11-12 14:46
 **/
public class HttpClient {

    private final OkHttpClient client = new OkHttpClient();

    private final MediaType mediaType =  MediaType.parse("application/text; charset=UTF-8");

    /**
    * @Description: 获取用户名
    * @Param: [BDUSS]
    * @return: void
    * @Author: diaolizhi
    * @Date: 2018/11/12
    */
    public void login(TBUser tbUser) {
        Headers.Builder builder = getBuilder();

        TreeMap<String, String> map = new TreeMap<>();
        map.put("bdusstoken", tbUser.getBDUSS());

        RequestBody requestBody = getRequestBody(map);

        Request request = new Request.Builder()
                .url("http://c.tieba.baidu.com/c/s/login")
                .headers(builder.build())
                .post(requestBody)
                .build();

        String res = null;

        try {
            Response response = client.newCall(request).execute();
            res = response.body().string();
            System.out.println(res);
        } catch (IOException e) {
            e.printStackTrace();
        }

        JsonObject userInfo = getJsonObject(res);
        userInfo = userInfo.getAsJsonObject("user");
        JsonPrimitive name = userInfo.getAsJsonPrimitive("name");
        tbUser.setUserName(name.getAsString());
        JsonPrimitive id = userInfo.getAsJsonPrimitive("id");
        tbUser.setUid(id.getAsString());
        System.out.println(name);
    }

    private JsonObject getJsonObject(String res) {
        JsonParser parser = new JsonParser();
        return (JsonObject) parser.parse(res);
    }

    /** 
    * @Description: 得到一个请求头，因为请求头包含一些通用字段
    * @Param: [] 
    * @return: okhttp3.Headers.Builder 
    * @Author: diaolizhi
    * @Date: 2018/11/12 
    */ 
    private Headers.Builder getBuilder() {
        Headers.Builder builder = new Headers.Builder();
        builder.add("User-Agent", "bdtb for Android 9.8.8.7");
        builder.add("Host", "c.tieba.baidu.com");
        return builder;
    }

    /** 
    * @Description: 更新所关注的贴吧
    * @Param: [BDUSS] 
    * @return: java.util.ArrayList<java.lang.String> 
    * @Author: diaolizhi
    * @Date: 2018/11/12 
    */ 
    public ArrayList<String> updateForumList(String BDUSS) {

        boolean hasMore = true;
        int pageNo = 1;

        Headers.Builder builder = getBuilder();

        TreeMap<String, String> map = new TreeMap<>();
        map.put("BDUSS", BDUSS);
        map.put("page_no", "1");
        map.put("_client_type", "2");
        map.put("page_size", "200");
        map.put("_client_version", "9.8.8.7");

        ArrayList<String> resList = new ArrayList<>();

        while (hasMore) {
            map.put("page_no", String.valueOf(pageNo++));

            Request request = new Request.Builder()
                    .url("http://c.tieba.baidu.com/c/f/forum/like")
                    .headers(builder.build())
                    .post(getRequestBody(map))
                    .build();

//        名字对应那一个
            try {
                Response response = client.newCall(request).execute();
                String res = response.body().string();
                System.out.println(res);
                JsonObject root = getJsonObject(res);

                int has = root.get("has_more").getAsInt();
                if(has == 0) {
                    hasMore = false;
                }

                JsonObject forumList = root.getAsJsonObject("forum_list");

                try {
                    JsonArray non = forumList.getAsJsonArray("non-gconforum");
                    for(int i=0; i<non.size(); i++) {
                        JsonElement o = non.get(i);
                        String string = o.getAsJsonObject().getAsJsonPrimitive("name").getAsString();
                        resList.add(string);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                try {
                    JsonArray gcon = forumList.getAsJsonArray("gconforum");
                    for(int i=0; i<gcon.size(); i++) {
                        JsonElement o = gcon.get(i);
                        String string = o.getAsJsonObject().getAsJsonPrimitive("name").getAsString();
                        resList.add(string);
                    }
                } catch (Exception e) {
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }



        return resList;
    }

    /**
    * @Description: 通过 TreeMap 获取 RequestBody
    * @Param: [map]
    * @return: okhttp3.RequestBody
    * @Author: diaolizhi
    * @Date: 2018/11/12
    */
    private RequestBody getRequestBody(TreeMap<String, String> map) {
        String body = Tools.countSign(map);
        return RequestBody.create(mediaType, body);
    }

    /**
    * @Description: 签到
    * @Param: [BDUSS, forumList]
    * @return: void
    * @Author: diaolizhi
    * @Date: 2018/12/20
    */
    public void Sign(String BDUSS, ArrayList<String> forumList) {

        ArrayList<String> errorList = new ArrayList<>();
//        ArrayList<String>

        String url = "http://c.tieba.baidu.com/c/c/forum/sign";
        Headers.Builder builder = getBuilder();

        client.dispatcher().setMaxRequests(100);
        client.dispatcher().setMaxRequestsPerHost(100);

        TreeMap<String, String> map = new TreeMap<>();
        map.put("BDUSS", BDUSS);

        String tbs = getTbs(BDUSS);

        for(int i=0; i<forumList.size(); i++) {

            map.put("kw", forumList.get(i));
            map.put("fid", getFid(forumList.get(i)));
            map.put("tbs", tbs);

            Request request = new Request.Builder()
                    .url(url)
                    .headers(builder.build())
                    .post(getRequestBody(map))
                    .build();

            int j = i;
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    errorList.add(forumList.get(j));
                    System.out.println(forumList.get(j) + "签到失败");
                }
                @Override
                public void onResponse(Call call, Response response){
                    String res = null;
                    try {
                        res = response.body().string();
                    } catch (IOException e) {
//                        e.printStackTrace();
                        System.err.println(forumList.get(j) + "签到失败。。。。。");
                    }
                    JsonObject root = getJsonObject(res);
                    String errorCode = root.get("error_msg").getAsString();
                    System.out.println(forumList.get(j) + "  -->  " + errorCode);
                }
            });
        }

        while (client.dispatcher().runningCallsCount() != 0) {

        }

        if (errorList.size() != 0) {
            System.err.println("开始重新签到！！！！！！！！！！！！！！！！");
            Sign(BDUSS, errorList);
        }
    }

    /** 
    * @Description: 通过贴吧名获取 fid
    * @Param: [kw] 
    * @return: java.lang.String 
    * @Author: diaolizhi
    * @Date: 2018/11/14 
    */ 
    public String getFid(String kw) {
        String url = "http://tieba.baidu.com/f/commit/share/fnameShareApi?ie=utf-8&fname=" + kw;
        Request request = new Request.Builder()
                .url(url)
                .build();
        try {
            Response response = client.newCall(request).execute();
            String res = response.body().string();
            JsonObject root = getJsonObject(res);
            JsonObject data = root.getAsJsonObject("data");
            JsonElement fid = data.get("fid");
            String fidStr = fid.getAsString();
            return fidStr;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "596711";
    }

    /** 
    * @Description: 获取 tbs 
    * @Param: [BDUSS] 
    * @return: java.lang.String 
    * @Author: diaolizhi
    * @Date: 2018/11/14 
    */ 
    public String getTbs(String BDUSS) {
        String url = "http://tieba.baidu.com/dc/common/tbs";

        Headers.Builder builder = getBuilder();
        builder.add("Cookie", "BDUSS=" + BDUSS);

        Request request = new Request.Builder()
                .url(url)
                .headers(builder.build())
                .build();
        try {
            Response response = client.newCall(request).execute();
            String res = response.body().string();
            JsonObject root = getJsonObject(res);
            JsonElement tbs = root.get("tbs");
            return tbs.getAsString();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "9653f1eb543e342b1542030308";
    }

    /** 
    * @Description: 查看用户关注的贴吧，返回内容未处理
    * @Param: [name] 
    * @return: void 
    * @Author: diaolizhi
    * @Date: 2018/11/12 
    */ 
    public void ttttt(String name) {
        String uid = getFriendUid(name);

        Headers.Builder builder = getBuilder();
        builder.add("client_user_token", uid);
        builder.add("Charset", "UTF-8");
        builder.add("User-Agent", "bdtb for Android 9.8.8.7");
        builder.add("Host", "c.tieba.baidu.com");

        TreeMap<String, String> map = new TreeMap<>();
        map.put("_client_type", "2");
        map.put("_client_version", "9.8.8.7");
        map.put("friend_uid", "821487821");
        map.put("is_guest", "0");
        map.put("uid", uid);
        
        Request request = new Request.Builder()
                .url("http://c.tieba.baidu.com/c/f/forum/like")
                .headers(builder.build())
                .post(getRequestBody(map))
                .build();

        try {
            Response response = client.newCall(request).execute();
            String string = response.body().string();

            System.out.println(string);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /** 
    * @Description: 通过用户名获取 uid 
    * @Param: [searchKey] 
    * @return: java.lang.String 
    * @Author: diaolizhi
    * @Date: 2018/11/12 
    */ 
    public String getFriendUid(String searchKey) {

        Headers.Builder hBuilder = new Headers.Builder();
        hBuilder.add("Cookie", "ka=open");
        hBuilder.add("Host", "c.tieba.baidu.com");

        TreeMap<String, String> body = new TreeMap<>();
        body.put("search_key", searchKey);
        body.put("_client_version", "7.0.0.0");

        String url = "http://c.tieba.baidu.com/c/r/friend/searchFriend";



        String res = Tools.postData(url, hBuilder, body);
        System.out.println(res);

//        解析 Json 数据
        JsonObject root = getJsonObject(res);
        JsonArray userInfo = root.get("user_info").getAsJsonArray();
        String fUid = userInfo.get(0).getAsJsonObject().get("user_id").getAsString();

        return fUid;
    }

    /**
    * @Description: 获取某人的回帖记录【未命名】
    * @Param: [BDUSS, userName, pn]
    * @return: void
    * @Author: diaolizhi
    * @Date: 2018/12/20
    */
    public void test(String BDUSS, String userName, String pn) throws IOException {
        Headers.Builder builder = getBuilder();
        builder.add("Connection", "Keep-Alive");
        builder.add("Accept", "application/protobuf");
        builder.add("Content-Type", "application/x-www-form-urlencoded");

        TreeMap map = new TreeMap();
//        map.put("BDUSS", BDUSS);
//        map.put("_client_type", "2");
//        map.put("_client_version", "9.2.0.4");
//        map.put("uid", getFriendUid(userName));
////        map.put("hide_post", "0");
////        map.put("friend_uid", "79");
////        map.put("is_guest", "1");
//        map.put("is_thread", "1");
//        map.put("pn", pn);
////        map.put("type", "0");
////        map.put("post_content", "1");
//        map.put("need_content", "1");


        map.put("need_content", "1");
//        map.put("is_thread", "1");
        map.put("net_type", "2");
        map.put("thread_type", "0");
        map.put("_client_id", "wappc_1545325162964_493.0");
//        map.put("timestamp", "1545325162964");
        map.put("_client_type", "2");
//        map.put("from", "tieba");
//        map.put("rn", "20");
        map.put("pn", "2");
        map.put("is_guest", "0");
        map.put("is_friend", "1");
//        map.put("is_bawu", "1");
//        map.put("is_mask", "0");
//        map.put("is_hide", "0");
//        map.put("type", "0");
//        map.put("friend_id", getFriendUid("凶杀组"));
//        map.put("uid", getFriendUid("神相"));
        map.put("uid", getFriendUid("稍加"));
//        map.put("_phone_imei", "352316052799040");
        map.put("_client_version", "9.2.2");

//        todo: 如果 Cookie 和 Body 同时存在 BDUSS?

        Request request = new Request.Builder()
//                .url("http://c.tieba.baidu.com/c/u/feed/mypost")
                .url("http://c.tieba.baidu.com/c/u/feed/userpost")
                .headers(builder.build())
                .post(getRequestBody(map))
                .build();

        Response response = null;
        try {
            response = client.newCall(request).execute();


//            FileOutputStream stream = new FileOutputStream("C:\\Users\\dlz\\Desktop\\fuck.txt");
//            try {
//                stream.write(response.body().bytes());
//                System.out.println("保存成功");
//            } finally {
//                stream.close();
//            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        String res = response.body().string();

        System.out.println(res);

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

            TinyReplyRecord[] tinyReplyRecords = new TinyReplyRecord[contents.size()];

//            contents 是同一个帖子的不同回复
            for (int j=0; j<contents.size(); j++) {
                System.out.println(contents.get(j));
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

                tinyReplyRecords[j] = new TinyReplyRecord(replyPostId, replyContent);

                replyContent = "";
                replyPostId = "";

                System.out.println(replyContent);
            }


            String quotePostId = "";
            String quoteUserName = "";
            String quoteContent = "";

            try {
                JsonObject quote = object.get("quote").getAsJsonObject();
                quotePostId = quote.get("post_id").getAsString();
                quoteUserName = quote.get("user_name").getAsString();
                quoteContent = quote.get("content").getAsString();
//                System.out.println(quotePostId + quoteUserName + quoteContent);
            } catch (Exception e) {

            }


            records[i] = new ReplyRecord(forum_id, thread_id, post_id, quotePostId,
                    quoteUserName, quoteContent, forumName, title, tinyReplyRecords);



//            System.out.println("论坛id:" + forum_id + "  帖子id:" + thread_id + "  回复id:" + post_id);
        }

//        for (int i=0; i<records.length; i++) {
//            System.out.println(records[i]);
//        }

        System.out.println(new Gson().toJson(records));

//        System.out.println(array);

//        System.out.println(res);
    }

    public void test2(String BDUSS, String userName) throws IOException {
        Headers.Builder builder = getBuilder();
        builder.add("Connection", "Keep-Alive");
        builder.add("Content-Type", "application/x-www-form-urlencoded");

        TreeMap map = new TreeMap();
        map.put("BDUSS", BDUSS);
        map.put("_client_type", "1");
        map.put("_client_version", "9.2.0.4");
        map.put("uid", getFriendUid(userName));
//        https://tieba.baidu.com/mo/q/m?kz=5948151129&sc=122873201212
        map.put("kz", "5760392379");
        map.put("pid", "122877824424");
//        map.put("kz", "5729250665");
//        map.put("pid", "120102912378");
        map.put("pn", "1");


        Request request = new Request.Builder()
//                .url("http://c.tieba.baidu.com/c/u/feed/mypost")
//                .url("http://c.tieba.baidu.com/c/f/pb/page")
                .url("http://c.tieba.baidu.com/c/f/pb/floor")
                .headers(builder.build())
                .post(getRequestBody(map))
                .build();

        Response response = null;
        try {
            response = client.newCall(request).execute();


        } catch (IOException e) {
            e.printStackTrace();
        }
        String res = response.body().string();


//        JsonParser parser = new JsonParser();
//        JsonArray root = (JsonArray) parser.parse(res);
//        System.out.println(root.get(1));

        JsonObject root = getJsonObject(res);
//
        System.out.println(root);

    }


    public void test3() throws IOException {
        Headers.Builder builder = getBuilder();
        builder.add("Connection", "Keep-Alive");
        builder.add("Content-Type", "application/x-www-form-urlencoded");


        Request request = new Request.Builder()
                .url("https://tieba.baidu.com/mo/q/m?kz=5729250665&sc=120102912378\n")
                .headers(builder.build())
                .build();

        Response response = null;
        try {
            response = client.newCall(request).execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
        String res = response.body().string();
        System.out.println(res);
    }

    /** 
    * @Description: 进入一个吧的首页，返回数据未处理
    * @Param: [BDUSS, kw] 
    * @return: void 
    * @Author: diaolizhi
    * @Date: 2018/12/20 
    */ 
    public void intoAForum(String BDUSS, String kw) {
        String url = "http://c.tieba.baidu.com/c/f/frs/page";

        Headers.Builder builder = getBuilder();
//        builder.add("", "");

        TreeMap<String, String> map = new TreeMap<>();
        map.put("BDUSS", BDUSS);
        map.put("kw", kw);
        map.put("pn", "2");

        Request request = new Request.Builder()
                .url(url)
                .post(getRequestBody(map))
                .build();

        try {
            Response response = client.newCall(request).execute();
            System.out.println(response.body().string());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /** 
    * @Description: 获取通过【帖子id】查看一个帖子内容 
    * @Param: [BDUSS, kz] 
    * @return: void 
    * @Author: diaolizhi
    * @Date: 2018/12/20 
    */ 
    public void seeAThread(String BDUSS, String kz) {
        String url = "http://c.tieba.baidu.com/c/f/pb/page";

        Headers.Builder builder = getBuilder();
//        builder.add("", "");

        TreeMap<String, String> map = new TreeMap<>();
        map.put("BDUSS", BDUSS);
        map.put("kz", kz);
//        map.put("pn", "1");
        map.put("cid", "123315568179");

        Request request = new Request.Builder()
                .url(url)
                .post(getRequestBody(map))
                .build();

        try {
            Response response = client.newCall(request).execute();
            System.out.println(response.body().string());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /** 
    * @Description: 通过【帖子id】和【回复id】获取某一楼的内容，返回结果未处理
    * @Param: [BDUSS, kz, pid] 
    * @return: void 
    * @Author: diaolizhi
    * @Date: 2018/12/20 
    */ 
    public void seeAFloor(String BDUSS, String kz, String pid) {
        String url = "http://c.tieba.baidu.com/c/f/pb/floor";

        Headers.Builder builder = getBuilder();
//        builder.add("", "");

        TreeMap<String, String> map = new TreeMap<>();
//        map.put("BDUSS", BDUSS);
        map.put("kz", kz);
        map.put("spid", pid);
        map.put("_client_type", "1");
        map.put("pn", "1");
//        map.put("sn", "1");
//        map.put("cid", "123315568179");

        Request request = new Request.Builder()
                .url(url)
                .post(getRequestBody(map))
                .build();

        try {
            Response response = client.newCall(request).execute();

            String res = response.body().string();

            System.out.println(res);

            JsonElement element = new JsonParser().parse(res);


            JsonArray array = element.getAsJsonObject().getAsJsonArray("subpost_list");
            for (int i=0; i<array.size(); i++) {
                JsonObject object2 = array.get(i).getAsJsonObject();
                String id = object2.get("id").getAsString();
                JsonObject authorInfo = object2.get("author").getAsJsonObject();
                String name = authorInfo.get("name").getAsString();
                String nameShow = authorInfo.get("name_show").getAsString();
                System.out.println(id + " " + name + " " + nameShow);
            }
            System.out.println(array);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void testDelete(String BDUSS) throws IOException {
        Headers.Builder builder = getBuilder();
        builder.add("Connection", "Keep-Alive");
        builder.add("Content-Type", "application/x-www-form-urlencoded");
        builder.add("Cookie", "BDUSS=" + BDUSS);


        TreeMap map = new TreeMap();
//        map.put("BDUSS", BDUSS);
        map.put("is_guest", "1");
        map.put("_client_type", "1");
        map.put("_client_version", "4.0.0");
        map.put("fid", getFid("刁礼智"));
        map.put("word", "刁礼智");
        map.put("is_debug", "1");
        map.put("z", "5760392379");
        map.put("tbs", getTbs(BDUSS));
        map.put("pid", "122876714064");

        map.put("is_vipdel", "1");
        map.put("isfloor", "1");
//        map.put("src", "1");

        Request request = new Request.Builder()
                .url("http://c.tieba.baidu.com/c/c/bawu/delpost")
                .headers(builder.build())
                .post(getRequestBody(map))
                .build();

        Response response = null;
        try {
            response = client.newCall(request).execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
        String res = response.body().string();
        System.out.println(res);
    }


    public void modify(String BDUSS) throws IOException {
        Headers.Builder builder = getBuilder();
//        builder.add("Content-Type", "multipart/form-data; boundary=--------7da3d81520810*");
        builder.add("Cookie", "BDUSS=" + BDUSS);

        TreeMap<String, String> map = new TreeMap<>();
//        map.put("BDUSS", BDUSS);
//        map.put("intro", "%BD%AD%D4%F3%C3%F1");\u6cfd\UFFFD\u6c11
        map.put("intro", "444");

        Request request = new Request.Builder()
                .url("http://c.tieba.baidu.com/c/c/profile/modify")
                .headers(builder.build())
                .post(getRequestBody(map))
                .build();

        Response response = null;
        try {
            response = client.newCall(request).execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
        String res = response.body().string();
        System.out.println(res);

    }
}
