import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import java.io.IOException;

/**
 * @program: tiebatool
 * @description: 测试
 * @author: diaolizhi
 * @create: 2018-11-12 14:54
 **/
public class Test1112 {

    String BDUSS = "kotbGZVLVV4c1Y2SnIyTXdaTDRYS3VwWjNYbEtIR1ZXc1FHa3lPdzlFY3JzeEJjQUFBQUFBJCQAAAAAAAAAAAEAAACBd1410ruw77~TzeZMT0wAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAACsm6VsrJulbS";

//    diao
//        String BDUSS = "1VPbld1RUtqQ2I4Z054RHFYZHVPdn5EV2dYSGQtaXpvaHJZTFNQSUlUd0x5QkJjQUFBQUFBJCQAAAAAAAAAAAEAAACPxNFGtfPA8dbHAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAs76VsLO-lbR";


    @Test
    void test() {
        TBUser tbUser = new TBUser(BDUSS);
        System.out.println(tbUser.getUserName() + " " + tbUser.getUid());

        System.out.println(tbUser.getForumList());
        tbUser.updateForumList();
        System.out.println(tbUser.getForumList());

        System.out.println("共计" + tbUser.getForumListNum() + "个贴吧");
//        tbUser.Sign();

//        try {
//            Thread.sleep(10000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }

//        HttpClient httpClient = new HttpClient();
//        httpClient.getFid("vivo");
//        System.out.println(httpClient.getTbs());
//        httpClient.ttttt("why130001");

//        System.out.println(httpClient.getFriendUid("投诉举报平台"));

    }

    @Test
    @DisplayName("测试签到")
    void testSign() {
        TBUser tbUser = new TBUser(BDUSS);
        tbUser.updateForumList();
        tbUser.Sign();
    }

    @Test
    @DisplayName("测试 某人的回帖记录")
    void testMyPost() throws IOException {
        HttpClient client = new HttpClient();
        client.test("", "益潇濛", "1");
//        client.test("", "李凉语", "1");
    }

    @Test
    @DisplayName("测试 某人的回帖记录【已登录】")
    void testMyPostLogined() throws IOException {
        HttpClient client = new HttpClient();
        client.test(BDUSS, "一帮坑玩LOL", "1");
//        client.test("", "李凉语", "1");
    }

    @Test
    @DisplayName("测试 查看某一个层楼")
    void testOtherPost() throws IOException {
        HttpClient client = new HttpClient();
        client.test2(BDUSS, "荒唐爸爸俏妹妹");
    }

    @Test
    @DisplayName("测试 查看别人的回帖内容")
    void testOtherReply() throws IOException {
        HttpClient client = new HttpClient();
        client.test3();
    }


    HttpClient client = new HttpClient();

    @Test
    @DisplayName("测试删除帖子")
    void testDelete() throws IOException {
        client.testDelete(BDUSS);
    }

    @Test
    @DisplayName("测试修改个人资料")
    void testModify() throws IOException {
        client.modify(BDUSS);
    }

    @Test
    @DisplayName("查看用户关注的贴吧")
    void testSeeTie() {
        client.ttttt("李凉语");
    }

    @Test
    @DisplayName("进入一个贴吧")
    void intoAForum() {
        client.intoAForum(BDUSS, "bug");
    }

    @Test
    @DisplayName("测试 看帖")
    void seeAThread() {
        client.seeAThread(BDUSS, "5983121478");
    }

    @Test
    @DisplayName("测试 floor")
//    @RepeatedTest(10)
    void testFloor() {
        client.seeAFloor(BDUSS, "5983121478", "123315568179");
    }
}
