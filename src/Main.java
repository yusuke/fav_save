import twitter4j.*;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;


public class Main {


    public static void main(String[] args) throws TwitterException, IOException {

        //Twitter twitter = new TwitterFactory().getInstance();
        //User user = twitter.verifyCredentials();

        Twitter twitter = TwitterFactory.getSingleton();


        RequestToken requestToken = twitter.getOAuthRequestToken();
        AccessToken accessToken = null;
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        while (null == accessToken) {
            System.out.println("Open the following URL and grant access to your account:");
            System.out.println(requestToken.getAuthorizationURL());
            System.out.print("Enter the PIN(if aviailable) or just hit enter.[PIN]:");
            String pin = br.readLine();
            try {
                if (pin.length() > 0) {
                    accessToken = twitter.getOAuthAccessToken(requestToken, pin);
                } else {
                    accessToken = twitter.getOAuthAccessToken();
                }
            } catch (TwitterException te) {
                if (401 == te.getStatusCode()) {
                    System.out.println("Unable to get the access token.");
                } else {
                    te.printStackTrace();
                }
            }
        }


        User user = twitter.verifyCredentials();
        //ユーザ情報取得
        System.out.println("ログインしているユーザー情報");
        System.out.println("名前　　　　：" + user.getName());
        System.out.println("表示名　　　：" + user.getScreenName());
        System.out.println("フォロー数　：" + user.getFriendsCount());
        System.out.println("フォロワー数：" + user.getFollowersCount());
        System.out.println("--------------------------------------");

        //getfavmedia(user.getScreenName());
        getfavmedia("soroshi_1419");
    }


    private static String getfavmedia(String user) throws TwitterException {

        //生成
        Twitter twitter = TwitterFactory.getSingleton();

        //リストの初期化
        ResponseList<Status> fav = null;
        int page = 1;
        int total = 0;
        int serachpages = 16;//MAX16ページ　16*200=3200ツイートまで


        //ブラックリストの取得
        ArrayList<String> blacklist = new ArrayList<String>();
        try {
            blacklist = setBlacklist();
        } catch (FileNotFoundException e) {
            System.out.println("ブラックリストが見つかりません");
        }

        for (page = 1; page < serachpages; page++) {

            //何ツイートとるかの設定 後で設定できるようにするかも
            Paging paging = new Paging(page, 200);


            //fav取得
            fav = twitter.getFavorites(user, paging);
            for (Status status : fav) {


                MediaEntity[] exMedia = status.getExtendedMediaEntities(); //各ツイートのメディア欄を取得

                for (MediaEntity media : exMedia) {
                    if (!blacklist.contains(status.getUser().getScreenName())) {

                        //デバック用
                        //System.out.println(exMedia.length);
                        //System.out.println(media.getMediaURL());
                        try {

                            //画像が複数の場合
                            if (exMedia.length != 1) {
                                File newdir = new File("fav/" + status.getUser().getScreenName() + "_" + status.getId()); //ツイート主のID+ツイートidのディレクトリを作成
                                if (newdir.exists() == false) {
                                    newdir.mkdir();
                                }

                                for (int i = 0; i < exMedia.length; i++) {
                                    MediaEntity mediaEntity = exMedia[i];

                                    URL url = new URL(mediaEntity.getMediaURL());
                                    URLConnection urlConnection = url.openConnection();
                                    URLConnection urlcon = url.openConnection();

                                    InputStream fileIS = urlcon.getInputStream();
                                    File saveFile = new File("fav/" + status.getUser().getScreenName() + "_" + status.getId() + "/" + i + ".jpg");//添え字で保存
                                    FileOutputStream fileOS = new FileOutputStream(saveFile);
                                    int c;
                                    while ((c = fileIS.read()) != -1) fileOS.write((byte) c);
                                    fileOS.close();
                                    fileIS.close();
                                }
                            } else {
                                //画像が1枚の場合


                                URL url = new URL(media.getMediaURL());
                                URLConnection urlConnection = url.openConnection();
                                URLConnection urlcon = url.openConnection();

                                InputStream fileIS = urlcon.getInputStream();
                                File saveFile = new File("fav/" + status.getUser().getScreenName() + "_" + status.getId() + ".jpg"); //ツイート主のID+ツイートIDで保存
                                FileOutputStream fileOS = new FileOutputStream(saveFile);
                                int c;
                                while ((c = fileIS.read()) != -1) fileOS.write((byte) c);
                                fileOS.close();
                                fileIS.close();

                            }
                            total++;
                            System.out.println(total + "件目の画像を取得しています");

                        } catch (java.net.MalformedURLException e) {
                            e.printStackTrace();
                            System.out.print("無効なURL");
                        } catch (java.io.IOException e) {
                            e.printStackTrace();
                            System.out.print("入出力エラー");
                        }
                    }
                }
            }

        }
        return (total + "件の画像を保存しました");
    }


    private static ArrayList<String> setBlacklist() throws FileNotFoundException {
        ArrayList<String> blacklist = new ArrayList<>();
        FileReader fileReader = new FileReader("blacklist.txt");
        BufferedReader bufferedReader = new BufferedReader(fileReader);

        System.out.println("ブラックリストを読み込みます");
        try {
            //ファイル読み込み準備


            String line;
            while ((line = bufferedReader.readLine()) != null) {
                blacklist.add(line);
                System.out.println(line);
            }


        } catch (FileNotFoundException e) {
            System.out.println("ブラックリストファイルが見つかりません");
            System.out.println("使用する場合はblacklist.txtを作成してください。");
            return null;
        } catch (IOException e) {
            System.out.println("入出力エラー");
        }
        try {
            bufferedReader.close();
        } catch (IOException e) {
            System.out.println("入出力エラー");
        }
        System.out.println("ブラックリストを読み込みました");
        return blacklist;
    }

}