package ion.ops.tts.demo.run;

import ion.ops.tts.demo.service.*;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.core.env.Environment;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import javax.xml.ws.Response;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Component
public class Executor {

     @Autowired
     protected CommonService commonService;

     @Autowired
     private AwsTtsService awsTtsService;

     @Autowired
     private GoogleTtsService googleTtsService;

     @Autowired
     private NaverTtsService naverTtsService;

     private Map<String, Object> parameterMap;
     private TtsService ttsService;

     @Autowired
     Environment environment;

     public Executor() throws Exception {
          parameterMap = new HashMap<String, Object>();
     }

     public Map<String, Object> apiKeyStore(String id, String pw, String type, MultipartFile file) throws IOException {
          parameterSetting(type);
          commonService.setParameterMap(parameterMap);
          String state = commonService.keyStore(id, pw, type, file);
          parameterMap.clear();
          parameterMap.put("state", state);
          return parameterMap;
     }

     private void createTestTextFile(String type) throws IOException {
          parameterSetting(type);
          commonService.setParameterMap(parameterMap);
          commonService.createTextFile();
     }

     public void execute(String type) throws Exception {
          ttsService.run();
          ttsService.ttsMp3Download(parameterMap);
     }

     public Map<String, Object> responseExecute(String type, MultipartFile multipartFile) throws Exception {
          initSetting(type);
          if (commonService.isAttachFile(multipartFile)){
               commonService.attachFileExtractionToText(multipartFile);
               execute(type);
          } else {
               commonService.setNotExistFileSetMessage();
          }
          return commonService.getResultMap();

     }

     private void setTtsService(String type){
          if (type.equals("aws")) ttsService = awsTtsService;
          else if (type.equals("google")) ttsService = googleTtsService;
          else if (type.equals("naver")) ttsService = naverTtsService;
     }
     public void parameterSetting(String type) {
          parameterMap.put("apikey.path", environment.getProperty("apikey.path"));
          parameterMap.put("apikey." + type + ".filename", environment.getProperty("apikey." + type + ".filename"));
          parameterMap.put("textFile.path", environment.getProperty("textFile.path"));
          parameterMap.put("ttsFile.path", environment.getProperty("ttsFile.path"));
          parameterMap.put("ttsFile.info.manage.path", environment.getProperty("ttsFile.info.manage.path"));
          parameterMap.put("ttsforder.path", environment.getProperty("ttsforder.path"));
          parameterMap.put("type", type);
     }
     public void registerCommonServiceInTtsService(String type) {
          if (!type.equals("")){
               ttsService.setParameterMap(parameterMap);
               commonService.setTtsService(ttsService);
          }
          commonService.setParameterMap(parameterMap);

     }

     //@Override
     public void run(ApplicationArguments args) throws Exception {
          //execute("aws");
          //credentialInfoSave("naver");
          //createTestTextFile("naver");
          //credentialInfoSave("aws");
     }

     public Response run(String ttsType) throws Exception {
          execute(ttsType);
          return null;
     }

     public ResponseEntity<InputStreamResource> ttsFileDownload(String fileName) throws IOException, ParseException {
          initSetting("");
          return commonService.downloadTtsFile(fileName);
     }

     public void initSetting(String type){
          if (!type.equals("")) setTtsService(type);
          parameterSetting(type);
          registerCommonServiceInTtsService(type);
     }

     public Map<String, Object> responseTtsFileNameList() throws Exception {
          initSetting("");
          return commonService.ttsFileList();
     }
     public Map<String, Object> isExistApiKey(String ttsType) {
          initSetting(ttsType);
          return commonService.returnIsApikey(ttsType);
     }

     public Map<String, Object> ttsFileDelete(String fileName) {
          initSetting("");
          return commonService.ttsFileDelete(fileName);
     }
 }
