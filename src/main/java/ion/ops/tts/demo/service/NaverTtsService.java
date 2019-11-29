package ion.ops.tts.demo.service;

import com.google.protobuf.ByteString;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Map;

@Component
public class NaverTtsService implements TtsService {
    @Autowired
    protected CommonService commonService;
    private Map<String, Object> parameterMap;

    @Override
    public void ttsMp3Download(Map<String, Object> parameterMap) {
        commonService.setResultMap();
    }

    @Override
    public void setParameterMap(Map<String, Object> parameterMap) {

    }

    @Override
    public void run() throws IOException, ClassNotFoundException, ParseException {
        commonService.setCreateFilePath();
        String clientId = String.valueOf(commonService.keyLoad().get("clientId"));//애플리케이션 클라이언트 아이디값";
        String clientSecret = String.valueOf(commonService.keyLoad().get("clientSecret"));//애플리케이션 클라이언트 시크릿값";

        String text = URLEncoder.encode(
                commonService.readTextFile(commonService.mkdirForderRetrunForderPath("textFile.path")+"upload.json")
                , "UTF-8"); // 13자
        String apiURL = "https://naveropenapi.apigw.ntruss.com/voice/v1/tts";
        URL url = new URL(apiURL);
        HttpURLConnection con = (HttpURLConnection)url.openConnection();
        con.setRequestMethod("POST");
        con.setRequestProperty("X-NCP-APIGW-API-KEY-ID", clientId);
        con.setRequestProperty("X-NCP-APIGW-API-KEY", clientSecret);
        // post request
        String postParams = "speaker=mijin&speed=0&text=" + text;
        con.setDoOutput(true);
        commonService.naverCreateMp3File(con, postParams);
    }

    @Override
    public InputStream getTtsMp3InpuStream() {
        return null;
    }

    @Override
    public ByteString getBypeString() {
        return null;
    }
}
