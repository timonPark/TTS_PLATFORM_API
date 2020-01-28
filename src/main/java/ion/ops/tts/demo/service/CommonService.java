package ion.ops.tts.demo.service;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.FastDateFormat;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.net.HttpURLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@Component
public class CommonService {
    private Logger logger = LoggerFactory.getLogger(CommonService.class);

    private FileOutputStream outputStream;
    private Map<String, Object> parameterMap;
    private Map<String, Object> resultMap;
    private String createFilePath;
    private String createFileName;

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

    public Map<String, Object> getParameterMap() {
        return parameterMap;
    }

    public Map<String, Object> getResultMap() {
        return resultMap;
    }

    public void setCreateFilePath(){
        createFileName = "tts_"
                + parameterMap.get("type") + "_" + String.valueOf(new Date().getTime()) +".mp3";
        createFilePath = mkdirForderRetrunForderPath("ttsFile.path") + createFileName;
    }

    public void setResultMap() throws IOException, ParseException {
        if (isTtsFile()){
            final String crateSuccessMessage = "TTS File [" + createFilePath + "] making is Success";
            createTtsFileInfoStore();
            logger.info(crateSuccessMessage);
            resultMap.put("states", crateSuccessMessage);
            resultMap.put("ttsFileName", createFileName);
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
    public String keyStore(String id, String pw, String type, MultipartFile attachFile)  {
        String result = null;
        String fileNameKey = "apikey." + type +".filename";
        JSONObject jsonObject = new JSONObject();
        File forder = new File(String.valueOf(parameterMap.get("apikey.path")));
        if (!forder.exists()){
            try {
                forder.mkdir();
            } catch (Exception e){
                logger.error(e.getMessage());
            }
        }
        if (String.valueOf(parameterMap.get("type")).equals("aws") ||
                String.valueOf(parameterMap.get("type")).equals("naver")){
            try {
                if (StringUtils.isEmpty(id)) throw new NullPointerException();
                else if (StringUtils.isEmpty(pw)) throw new NullPointerException();
                else {}
                FileWriter file = new FileWriter(String.valueOf(parameterMap.get("apikey.path"))
                        + parameterMap.get(fileNameKey));
                if (type.equals("aws")){
                    jsonObject.put("accessKey", id);
                    jsonObject.put("secretKey", pw);
                } else if (type.equals("naver")){
                    jsonObject.put("clientId", id);
                    jsonObject.put("clientSecret", pw);
                }
                file.write(jsonObject.toJSONString());
                file.close();
                result = "success";
            } catch (IOException e) {
                logger.error(e.getMessage());
                logger.error("api 키 저장중 오류가 발생하였습니다");
                result = "fail";
            } catch (NullPointerException e) {
                logger.error(e.getMessage());
                logger.error("id 또는 pw가 비어 있습니다");
                result = "fail";
            }
        } else {
            if (type.equals("google")) {
                FileOutputStream fos = null;
                try {
                    if (attachFile == null) throw new NullPointerException();
                    byte[] bytes = attachFile.getBytes();
                    fos = new FileOutputStream(String.valueOf(parameterMap.get("apikey.path"))
                            + parameterMap.get(fileNameKey));
                    fos.write(bytes);
                } catch(IOException e) {
                    logger.error(e.getMessage());
                    logger.error("api 키 저장중 오류가 발생하였습니다");
                    result = "fail";
                } catch (NullPointerException e){
                    logger.error(e.getMessage());
                    logger.error("google api 인증파일이 첨부되지 않았습니다");
                    result = "fail";
                }finally {
                    if (fos != null) {
                        try {
                            fos.close();
                            result = "success";
                        } catch (Exception e) {
                            logger.error(e.getMessage());
                            logger.error("api 키 저장중 오류가 발생하였습니다");
                            result = "fail";
                        }
                    }
                }

            }
        }
        return result;
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

    public void attachFileExtractionToText(MultipartFile multipartFile) throws IOException {
        byte[] bytes = multipartFile.getBytes();
        final String textFilePath = mkdirForderRetrunForderPath("textFile.path") + multipartFile.getOriginalFilename();
        Path path = Paths.get(textFilePath);
        Files.write(path, bytes);
        parameterMap.put("attachFileInText",readTextFile(textFilePath));
    }

    public void setNotExistFileSetMessage(){
        resultMap.put("states", "attach file not exist");
    }

    // ttsFile.info.manage.path
    public void createTtsFileInfoStore() throws IOException, ParseException {
        File file = new File(String.valueOf(parameterMap.get("ttsFile.info.manage.path")));
        if (!file.exists()){
            FileWriter fileWriter = new FileWriter(String.valueOf(parameterMap.get("ttsFile.info.manage.path")));

            JSONObject jsonObject = new JSONObject();
            jsonObject.put(createFileName, createFilePath);

            fileWriter.write(jsonObject.toJSONString());
            fileWriter.close();
        } else {
            FileReader fileReader = new FileReader(String.valueOf(parameterMap.get("ttsFile.info.manage.path")));
            JSONParser parser = new JSONParser();
            JSONObject jsonObject = (JSONObject) parser.parse(fileReader);
            jsonObject.put(createFileName, createFilePath);
            fileReader.close();
            FileWriter fileWriter = new FileWriter(String.valueOf(parameterMap.get("ttsFile.info.manage.path")));
            fileWriter.write(jsonObject.toJSONString());
            fileWriter.close();
        }
    }

    public String ttsManageFileSearchByTtsFileName(String ttsFileName) throws IOException, ParseException {
        FileReader fileReader = new FileReader(String.valueOf(parameterMap.get("ttsFile.info.manage.path")));
        JSONParser parser = new JSONParser();
        JSONObject jsonObject = (JSONObject) parser.parse(fileReader);
        return String.valueOf(jsonObject.get(ttsFileName));
    }

    public Map<String, Object> ttsFileList() throws IOException, ParseException {
        File file = new File(String.valueOf(parameterMap.get("ttsFile.info.manage.path")));
        if (!file.exists()) {
            File ttsForder = new File(String.valueOf(parameterMap.get("ttsforder.path")));
            if (!ttsForder.exists()) ttsForder.mkdirs();
            file.createNewFile();
        } else {
            FileReader fileReader = new FileReader(String.valueOf(parameterMap.get("ttsFile.info.manage.path")));
            JSONParser parser = new JSONParser();
            JSONObject jsonObject = null;
            try {
                jsonObject = (JSONObject) parser.parse(fileReader);
                Iterator<String> jsonObjectKeySet = jsonObject.keySet().iterator();
                int count = 1;
                while (jsonObjectKeySet.hasNext()) {
                    resultMap.put("file_" + count, jsonObjectKeySet.next());
                    count++;
                }
            } catch (ParseException e) {
                logger.error(e.getMessage());
                logger.error("리스트가 존재하지 않습니다.");
                resultMap.put("error", "errorMessage: " +e.getMessage() + ", 리스트가 존재하지 않습니다.");
            }


        }
        return resultMap;
    }

    public ResponseEntity<InputStreamResource> downloadTtsFile(String ttsFileName) throws IOException, ParseException {
        File ttsFile = null;
        InputStreamResource resource = null;
        try {
            ttsFile = new File(ttsManageFileSearchByTtsFileName(ttsFileName));
            resource = new InputStreamResource(new FileInputStream(ttsFile));
        } catch (IOException e) {
            logger.error(e.getMessage());
        } catch (ParseException e2) {
            logger.error(e2.getMessage());
        }


        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=" + ttsFileName)
                .contentType(MediaType.parseMediaType("audio/mpeg"))
                .contentLength(ttsFile.length())
                .body(resource);
    }


}
