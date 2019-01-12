package sendHttp;

import com.google.gson.*;
import domain.*;
import okhttp3.*;
import utils.JsonUtils;
import utils.OtherUtils;

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

    private final OkHttpClient client;
    private final MediaType mediaType;

    public HttpClient() {

        client = new OkHttpClient()
                .newBuilder()
                .addInterceptor(new UnzippingInterceptor())
                .build();

        client.dispatcher().setMaxRequests(100);
        client.dispatcher().setMaxRequestsPerHost(100);

        mediaType = MediaType.parse("application/text; charset=UTF-8");
    }

    /**
    * @Description: 获取用户的用户名和 uid
    * @Param: [tbUser]
    * @return: void
    * @Author: diaolizhi
    * @Date: 2018/12/23
    */
    public void getUserInfo(TBUser tbUser) throws GetInfoException{
        Headers.Builder builder = getBuilder();

        TreeMap<String, String> map = new TreeMap<>();
        map.put("bdusstoken", tbUser.getBDUSS());

        String url = "http://c.tieba.baidu.com/c/s/login";

        String res;

        try {
            res = exePostRequest(url, builder, map).body().string();

            String[] info = JsonUtils.userInfoParser(res);

            tbUser.setUid(info[0]);
            tbUser.setUserName(info[1]);
        } catch (Exception e) {
            throw new GetInfoException("获取用户信息失败，可能是 BDUSS 有误。");
        }

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
        builder.add("Accept-Encoding", "gzip");
        return builder;
    }

    /** 
    * @Description: 获取【关注的贴吧】，保存贴吧的名字和 fid
    * @Param: [BDUSS] 
    * @return: java.util.ArrayList<java.lang.String> 
    * @Author: diaolizhi
    * @Date: 2018/11/12 
    */ 
    public ArrayList<String[]> getForumList(String BDUSS) {

        boolean hasMore = true;
        int pageNo = 1;

        Headers.Builder builder = getBuilder();

        TreeMap<String, String> map = new TreeMap<>();
        map.put("BDUSS", BDUSS);
        map.put("_client_type", "2");
        map.put("page_size", "200");
        map.put("_client_version", "9.8.8.7");

        ArrayList<String[]> resList = new ArrayList<>();

        String url = "http://c.tieba.baidu.com/c/f/forum/like";

        while (hasMore) {
            map.put("page_no", String.valueOf(pageNo++));

            try {
                String res = exePostRequest(url, builder, map).body().string();

                UserForumsInfo forumsInfo = JsonUtils.userForumListParser(res);

                hasMore = (forumsInfo.getHasMore() == 1);

                if (forumsInfo.getOneForumInfos() != null) {
                    addForumToList(resList, forumsInfo);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return resList;
    }

    private void addForumToList(ArrayList<String[]> resList, UserForumsInfo forumsInfo) {
        for (int i=0; i<forumsInfo.getOneForumInfos().size(); i++) {
            String[] strings = new String[2];
            OneForumInfo oneForumInfo = forumsInfo.getOneForumInfos().get(i);
            strings[0] = oneForumInfo.getName();
            strings[1] = oneForumInfo.getId();
            resList.add(strings);
        }
    }

    /**
    * @Description: 通过 TreeMap 获取 RequestBody
    * @Param: [map]
    * @return: okhttp3.RequestBody
    * @Author: diaolizhi
    * @Date: 2018/11/12
    */
    private RequestBody getRequestBody(TreeMap<String, String> map) {
        String body = OtherUtils.countSign(map);
        return RequestBody.create(mediaType, body);
    }

    /**
    * @Description: 签到。num 记录重试次数。
    * @Param: [BDUSS, forumList, num]
    * @return: void
    * @Author: diaolizhi
    * @Date: 2018/12/23
    */
    public void Sign(String BDUSS, ArrayList<String[]> forumList, int num) {

        ArrayList<String[]> errorList = new ArrayList<>();

        String url = "http://c.tieba.baidu.com/c/c/forum/sign";
        Headers.Builder builder = getBuilder();

        TreeMap<String, String> map = new TreeMap<>();
        map.put("BDUSS", BDUSS);

        String tbs = getTbs(BDUSS);

        for(int i=0; i<forumList.size(); i++) {

            map.put("kw", forumList.get(i)[0]);
            map.put("fid", forumList.get(i)[1]);
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
                    String[] err = new String[2];
                    err[0] = forumList.get(j)[0];
                    err[1] = forumList.get(j)[1];
                    synchronized (errorList) {
                        errorList.add(err);
                    }
                    System.err.println(forumList.get(j)[0] + "网络请求失败");
                }
                @Override
                public void onResponse(Call call, Response response){
                    String res = null;
                    try {
                        res = response.body().string();
                    } catch (IOException e) {
                        System.err.println(forumList.get(j)[0] + "签到失败。。。。。");
                    }
                    JsonObject root = getJsonObject(res);
                    String errorCode = root.get("error_code").getAsString();

                    if (!errorCode.equals("160002") && !errorCode.equals("340008")
                            && !errorCode.equals("340006") && !errorCode.equals("0")) {

                        String[] err = new String[2];
                        err[0] = forumList.get(j)[0];
                        err[1] = forumList.get(j)[1];
                        synchronized (errorList) {
                            errorList.add(err);
                        }
                    }

                }
            });
        }

        while (client.dispatcher().runningCallsCount() != 0) {

        }

        if (errorList.size() != 0 && num < 5) {
            Sign(BDUSS, errorList, ++num);
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
            return JsonUtils.fidParser(res);
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

        Request request = getGetRequest(url, builder);
        try {
            Response response = client.newCall(request).execute();

            String res = response.body().string();

            return JsonUtils.tbsParser(res);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "9653f1eb543e342b1542030308";
    }

    private Request getGetRequest(String url, Headers.Builder builder) {
        return new Request.Builder()
                    .url(url)
                    .headers(builder.build())
                    .build();
    }

    /**
    * @Description: 获取别人关注的贴吧
    * @Param: [name, pn]
    * @return: domain.UserForumsInfo
    * @Author: diaolizhi
    * @Date: 2018/12/24
    */
    public UserForumsInfo getOtherForumList(String name, String pn) {
        String uid = getFriendUid(name);

//        如果该用户不存在，直接返回 null
        if (uid == null) {
            return null;
        }

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
        map.put("page_no", pn);
        map.put("uid", uid);
        
        String url = "http://c.tieba.baidu.com/c/f/forum/like";

        try {
            String res = exePostRequest(url, builder, map).body().string();

//            解析返回的 json
            return JsonUtils.userForumListParser(res);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    private Response exePostRequest(String url, Headers.Builder builder, TreeMap<String, String> map) {
        Request request = new Request.Builder()
                .url(url)
                .headers(builder.build())
                .post(getRequestBody(map))
                .build();
        try {
            return client.newCall(request).execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /** 
    * @Description: 通过用户名获取 uid 
    * @Param: [searchKey] 
    * @return: java.lang.String 
    * @Author: diaolizhi
    * @Date: 2018/11/12 
    */ 
    public String getFriendUid(String searchKey) {

        Headers.Builder builder = new Headers.Builder();
        builder.add("Cookie", "ka=open");
        builder.add("Host", "c.tieba.baidu.com");

        TreeMap<String, String> map = new TreeMap<>();
        map.put("search_key", searchKey);
        map.put("_client_version", "7.0.0.0");

        String url = "http://c.tieba.baidu.com/c/r/friend/searchFriend";

        String res;
        try {
            res = exePostRequest(url, builder, map).body().string();

            return JsonUtils.fuidParser(res);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
    * @Description: 获取某人的回帖记录，BDUSS 可以传入任意字符串
    * @Param: [BDUSS, userName, pn]
    * @return: void
    * @Author: diaolizhi
    * @Date: 2018/12/20
    */
    public ReplyRecord[] getUserPost(String BDUSS, String userName, String pn) {

        Headers.Builder builder = getBuilder();

        TreeMap<String, String> map = new TreeMap<>();
        map.put("BDUSS", BDUSS);
        map.put("need_content", "1");
//        is_thread 表明获取的是不是主题帖，但是只能查看自己的（通过自己的 BDUSS），没什么用。
        map.put("net_type", "2");
        map.put("thread_type", "0");
        map.put("_client_id", "wappc_1545325162964_493.0");
        map.put("timestamp", String.valueOf(System.currentTimeMillis()));
        map.put("_client_type", "2");
        map.put("pn", pn);
        map.put("uid", getFriendUid(userName));
        map.put("_client_version", "9.2.2");

        String url = "http://c.tieba.baidu.com/c/u/feed/userpost";

        try {
            String res = exePostRequest(url, builder, map).body().string();

            return JsonUtils.recordsParser(res);
        } catch (IOException e) {
//            对于网络请求失败的请求，究竟应不应该打印错误信息呢
//            e.printStackTrace();
        }

        return null;
    }

//    todo: http://c.tieba.baidu.com/c/u/feed/replyme

    /**
    * @Description: 回帖。对【回帖失败】的情况没有进行处理
    * @Param: [BDUSS, content, forumName, tid]
    * @return: void
    * @Author: diaolizhi
    * @Date: 2018/12/24
    */
    public void postAdd(String BDUSS, String content, String forumName, String tid) {

        Headers.Builder builder = getBuilder();

        TreeMap<String, String> map = new TreeMap<>();

        map.put("BDUSS", BDUSS);
        map.put("_client_type", "2");
        map.put("_client_version", "9.2.0.0");
        map.put("anonymous", "1");
        map.put("content", content);
        map.put("fid", getFid(forumName));
        map.put("from", "mini_baidu_appstore");
        map.put("is_ad", "0");
        map.put("kw", forumName);
        map.put("new_vcode", "1");
        map.put("subapp_type", "mini");
        map.put("tbs", getTbs(BDUSS));
        map.put("tid", tid);
        map.put("vcode_tag", "11");

        String url = "http://c.tieba.baidu.com/c/c/post/add";

        Response response = exePostRequest(url, builder, map);
        response.close();
    }


    /**
    * @Description: 关注一个贴吧。对【关注失败】的情况没有处理
    * @Param: [BDUSS, kw, url]
    * @return: void
    * @Author: diaolizhi
    * @Date: 2018/12/24
    */
    public void likeOrUnfavoForum(String BDUSS, String kw, int type) {

        String url = "http://c.tieba.baidu.com/c/c/forum/";
        url += (type == 1 ? "like" : "unfavo");

        Headers.Builder builder = getBuilder();

        TreeMap<String, String> map = new TreeMap<>();
        map.put("BDUSS", BDUSS);
        map.put("_client_type", "2");
        map.put("_client_version", "7.2.0.0");
        map.put("fid", getFid(kw));
        map.put("from", "mini_baidu_appstore");
        map.put("kw", kw);
        map.put("st_type", "from_frs");
        map.put("subapp_type", "mini");
        map.put("tbs", getTbs(BDUSS));

        exePostRequest(url, builder, map);

    }

    /**
    * @Description: 进入一个吧的首页，返回帖子列表
    * @Param: [BDUSS, kw, pn]
    * @return: domain.ThreadRecord[]
    * @Author: diaolizhi
    * @Date: 2018/12/24
    */
    public ThreadRecord[] intoAForum(String kw, String pn) {
        String url = "http://c.tieba.baidu.com/c/f/frs/page";

        Headers.Builder builder = getBuilder();

        TreeMap<String, String> map = new TreeMap<>();

        map.put("kw", kw);
        map.put("pn", pn);
        map.put("_client_type", "2");

        try {
            String res = exePostRequest(url, builder, map).body().string();

            return JsonUtils.threadListParser(res);

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /** 
    * @Description: 获取通过【帖子id:kz】查看一个帖子内容，如果存在【楼层id:pid】将返回之后的数据。返回结果未处理
    * @Param: [BDUSS, kz] 
    * @return: void 
    * @Author: diaolizhi
    * @Date: 2018/12/20 
    */ 
    public String seeAThread(String kz, String pid) {
        String url = "http://c.tieba.baidu.com/c/f/pb/page";

        Headers.Builder builder = getBuilder();

        TreeMap<String, String> map = new TreeMap<>();

        map.put("kz", kz);

//        可以不要 pid 字段，如果有这个字段的话，将返回那一层之后的楼层
        if (!pid.isEmpty()) {
            map.put("pid", pid);
        }

        try {
            String res = exePostRequest(url, builder, map).body().string();
            return res;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

//    TODO：该方法未完成
    /** 
    * @Description: 通过【帖子id】和【回复id / 楼层id】获取某一楼的内容，直接返回内容
    * @Param: [BDUSS, kz, pid] 
    * @return: void 
    * @Author: diaolizhi
    * @Date: 2018/12/20 
    */ 
    public String seeAFloor(String kz, String pid, String pn, int type) {
        String url = "http://c.tieba.baidu.com/c/f/pb/floor";

        Headers.Builder builder = getBuilder();

        String key = type == 1 ? "pid" : "spid";

//        如果是 pid：获取某一层楼下的回复
//        如果是 spid：获取某个楼中楼，以及它附近的回复
//        具体用法还需要调试

        TreeMap<String, String> map = new TreeMap<>();
        map.put("kz", kz);
        map.put(key, pid);
        map.put("_client_type", "2");
        map.put("pn", pn);
        map.put("rn", "20");
        map.put("_client_version", "9.6.0");
        map.put("net_type", "1");

        try {
            String res = exePostRequest(url, builder, map).body().string();

            return res;

        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
    * @Description: 根据【帖子id】和【回复id】删除帖子，对【删除失败】的情况没有进行处理
    * @Param: [BDUSS, tid, pid]
    * @return: void
    * @Author: diaolizhi
    * @Date: 2018/12/24
    */
    public void deletePost(String BDUSS, String tid, String pid) {
        String url = "http://c.tieba.baidu.com/c/c/bawu/delpost";

        Headers.Builder builder = getBuilder();

        TreeMap<String, String> map = new TreeMap<>();
        map.put("BDUSS", BDUSS);
        map.put("_client_type", "2");
        map.put("_client_version", "4.0.0");
        map.put("z", tid);
        map.put("tbs", getTbs(BDUSS));
        map.put("pid", pid);

        exePostRequest(url, builder, map);
    }


    /**
    * @Description: 修改个人资料
    * @Param: [BDUSS, intro]
    * @return: void
    * @Author: diaolizhi
    * @Date: 2018/12/24
    */
    public void modify(String BDUSS, String intro) {
        String url = "http://c.tieba.baidu.com/c/c/profile/modify";

        Headers.Builder builder = getBuilder();

        TreeMap<String, String> map = new TreeMap<>();
        map.put("intro", intro);
        map.put("BDUSS", BDUSS);

        exePostRequest(url, builder, map);
    }

    public void cloneForums(String BDUSS, String username) {
        boolean hasMore = true;
        int pageNo = 1;

        ArrayList<String[]> forumList = new ArrayList<>();

        UserForumsInfo forumsInfo;

        while (hasMore && forumList.size() <= 3001) {
            forumsInfo = getOtherForumList(username, String.valueOf(pageNo++));
            hasMore = forumsInfo.getHasMore() == 1;
            if (forumsInfo.getOneForumInfos() != null) {
                addForumToList(forumList, forumsInfo);
            }
        }

        likeForums(BDUSS, forumList);

    }

    public void likeForums(String BDUSS, ArrayList<String[]> forumList) {

        String url = "http://c.tieba.baidu.com/c/c/forum/like";

        Headers.Builder builder = getBuilder();

        for (int i=0; i<forumList.size(); i++) {
            TreeMap<String, String> map = new TreeMap<>();
            map.put("BDUSS", BDUSS);
            map.put("_client_type", "2");
            map.put("_client_version", "7.2.0.0");
            map.put("fid", forumList.get(i)[1]);
            map.put("from", "mini_baidu_appstore");
            map.put("kw", forumList.get(i)[0]);
            map.put("st_type", "from_frs");
            map.put("subapp_type", "mini");
            map.put("tbs", getTbs(BDUSS));

            Request request = new Request.Builder()
                    .url(url)
                    .headers(builder.build())
                    .post(getRequestBody(map))
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {

                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    response.close();
                }
            });
        }

    }
}