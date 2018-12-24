package sendHttp;

import com.google.gson.Gson;
import okhttp3.*;

import java.io.IOException;
import java.util.HashMap;

/**
 * @program: tiebatool
 * @description: 图灵机器人
 * @author: diaolizhi
 * @create: 2018-12-22 23:26
 **/
public class Tuling {

    private final static OkHttpClient client = new OkHttpClient();
    private final static MediaType mediaType =  MediaType.parse("application/json; charset=UTF-8");

    public static String send(String input) {
        HashMap<String, String> map = new HashMap<>();
        map.put("key", "5eefea1bfb444ba79f7a3b6fb79c81fc");
        map.put("info", input);
        map.put("loc", "北京市中关村");

        String json = new Gson().toJson(map);

        Request request = new Request.Builder()
                .url("http://www.tuling123.com/openapi/api")
                .post(RequestBody.create(mediaType, json))
                .build();

        try {
            Response response = client.newCall(request).execute();
            String res = response.body().string();
            return res;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

}
