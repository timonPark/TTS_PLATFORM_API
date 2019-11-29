package ion.ops.tts.demo.service;

import com.google.gson.JsonObject;
import org.apache.commons.lang3.time.FastDateFormat;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;


import java.io.*;
import java.net.HttpURLConnection;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class CommonService {
    private Logger logger = LoggerFactory.getLogger(CommonService.class);

    private FileOutputStream outputStream;
    private Map<String, Object> parameterMap;
    private Map<String, Object> resultMap;
    private String createFilePath;

    TtsService ttsService;
    public static final String SAMPLE = "나도 알아 나의 문제가 무엇인지\n";
    public CommonService(){
        resultMap = new HashMap<String, Object>();
    }

    public void setTtsService(TtsService ttsService) {
        this.ttsService = ttsService;
    }


    public void setParameterMap(Map<String, Object> parameterMap) {
        this.parameterMap = parameterMap;
    }

    public Map<String, Object> getResultMap() {
        return resultMap;
    }

    public void setCreateFilePath(){
        createFilePath = mkdirForderRetrunForderPath("ttsFile.path") + "tts_"
                + parameterMap.get("type") + "_" + String.valueOf(new Date().getTime()) +".mp3";
    }

    public void setResultMap(){
        if (isTtsFile()){
            final String crateSuccessMessage = "TTS File [" + createFilePath + "] making is Success";
            logger.info(crateSuccessMessage);
            resultMap.put("states", crateSuccessMessage);
            resultMap.put("ttsFileName", createFilePath);
        } else {
            resultMap.put("states", "TTS File [" + createFilePath + "] making is Fail");
        }

    }

    public void awsCreateMp3File(){
        try {
            outputStream = new FileOutputStream(new File(createFilePath));
            byte[] buffer = new byte[2 * 1024];
            int readBytes;

            try (InputStream in = ttsService.getTtsMp3InpuStream()){
                while ((readBytes = in.read(buffer)) >0 ) {
                    outputStream.write(buffer, 0, readBytes);
                }
            }


        } catch (Exception e) {
            logger.error("Create TTS File is Fail. Fail Message: " + e.getMessage());
        } finally {
            try {
                createMp3FileEnd();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void googleCreateMp3File(){
        try {
            outputStream = new FileOutputStream(new File(createFilePath));
            outputStream.write(ttsService.getBypeString().toByteArray());
            logger.info("TTS File [" + createFilePath + "] making is Success");
        } catch (FileNotFoundException e) {
            logger.error("Create TTS File is Fail. Fail Message: " + e.getMessage());
        } catch (IOException e) {
            logger.error("Create TTS File is Fail. Fail Message: " + e.getMessage());
        } finally {
            try {
                createMp3FileEnd();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void naverCreateMp3File(HttpURLConnection con, String postParams){
        try {
            DataOutputStream wr = new DataOutputStream(con.getOutputStream());
            wr.writeBytes(postParams);
            wr.flush();
            wr.close();
            int responseCode = con.getResponseCode();
            BufferedReader br;
            if(responseCode==200) { // 정상 호출
                InputStream is = con.getInputStream();
                int read = 0;
                byte[] bytes = new byte[1024];
                // 랜덤한 이름으로 mp3 파일 생성
                File f = new File(createFilePath);
                f.createNewFile();
                OutputStream outputStream = new FileOutputStream(f);
                while ((read =is.read(bytes)) != -1) {
                    outputStream.write(bytes, 0, read);
                }
                is.close();
                logger.info("TTS File [" + createFilePath + "] making is Success");
            } else {  // 에러 발생
                br = new BufferedReader(new InputStreamReader(con.getErrorStream()));
                String inputLine;
                StringBuffer response = new StringBuffer();
                while ((inputLine = br.readLine()) != null) {
                    response.append(inputLine);
                }
                br.close();
                logger.error("Create TTS File is Fail. Fail Message: " + response.toString());
            }
        } catch (Exception e) {
            logger.error("Create TTS File is Fail. Fail Message: " + e.getMessage());
        }

    }

    public void createMp3FileEnd() throws IOException {
        outputStream.close();
    }
    public void keyStore() throws IOException {
        if (String.valueOf(parameterMap.get("type")).equals("google")) return;
        String fileNameKey = "apikey." + String.valueOf(parameterMap.get("type")) +".filename";
        FileWriter file = new FileWriter(String.valueOf(parameterMap.get("apikey.path"))
                + parameterMap.get(fileNameKey));

        JSONObject jsonObject = new JSONObject();
        if (String.valueOf(parameterMap.get("type")).equals("aws")){
            jsonObject.put("accessKey", "");
            jsonObject.put("secretKey", "");
        } else if (String.valueOf(parameterMap.get("type")).equals("naver")){
            jsonObject.put("clientId", "");
            jsonObject.put("clientSecret", "");
        }

        file.write(jsonObject.toJSONString());
        file.close();
    }

    public JSONObject keyLoad() throws IOException, ClassNotFoundException, ParseException {
        if (String.valueOf(parameterMap.get("type")).equals("google")) return null;
        String fileNameKey = "apikey." + String.valueOf(parameterMap.get("type")) +".filename";
        FileReader fileReader = new FileReader(String.valueOf(parameterMap.get("apikey.path"))
                + parameterMap.get(fileNameKey));
        JSONParser parser = new JSONParser();
        JSONObject jsonObject = (JSONObject) parser.parse(fileReader);

        return jsonObject;
    }

    public String mkdirForderRetrunForderPath(String filePath){
        String year = FastDateFormat.getInstance("yyyyMMdd").format(Calendar.getInstance().getTime()).substring(0,4);
        String month = FastDateFormat.getInstance("yyyyMMdd").format(Calendar.getInstance().getTime()).substring(4,6);
        String day = FastDateFormat.getInstance("yyyyMMdd").format(Calendar.getInstance().getTime()).substring(6);
        parameterMap.put("year", year);
        parameterMap.put("month", month);
        parameterMap.put("day", day);
        String textFilePath = parameterMap.get(filePath) + year + "/" + month + "/" + day + "/";
        File folder = new File(textFilePath);
        if (!folder.exists()) {
            try{
                folder.mkdirs();
            }catch(Exception e){

            }
        }
        return textFilePath;

    }

    public void createTextFile() throws IOException {
        StringBuffer sbf = new StringBuffer();
        sbf.append("나도 알아 나의 문제가 무엇인지\n");
        sbf.append("난 못났고 별볼일 없지\n");
        BufferedWriter bufferedWriter
                = new BufferedWriter(new FileWriter(new File(mkdirForderRetrunForderPath("textFile.path")+"upload.txt")));
        bufferedWriter.write(sbf.toString());
        bufferedWriter.flush();
        bufferedWriter.close();
    }

    public String readTextFile(String filePath) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader (filePath));
        String         line = null;
        StringBuilder  stringBuilder = new StringBuilder();
        String         ls = System.getProperty("line.separator");

        try {
            while((line = reader.readLine()) != null) {
                stringBuilder.append(line);
                stringBuilder.append(ls);
            }

            return stringBuilder.toString();
        } finally {
            reader.close();
        }
    }

    public boolean isTtsFile(){
        if (new File(createFilePath).exists()) return true;
        else return false;
    }

    public boolean isAttachFile(MultipartFile multipartFile){
        return !multipartFile.isEmpty();
    }

    public void setNotExistFileSetMessage(){
        resultMap.put("states", "attach file not exist");
    }


}
