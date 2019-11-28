package ion.ops.tts.demo.run;

import ion.ops.tts.demo.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Component
public class Executor implements ApplicationRunner {

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

     public void credentialInfoSave(String type) throws IOException {
          parameterSetting(type);
          commonService.setParameterMap(parameterMap);
          commonService.keyStore();
     }

     private void createTestTextFile(String type) throws IOException {
          parameterSetting(type);
          commonService.setParameterMap(parameterMap);
          commonService.createTextFile();
     }

     public void execute(String type) throws Exception {
          if (type.equals("aws")) ttsService = awsTtsService;
          else if (type.equals("google")) ttsService = googleTtsService;
          else if (type.equals("naver")) ttsService = naverTtsService;
          parameterSetting(type);
          registerCommonServiceInTtsService();

          ttsService.run();
          ttsService.ttsMp3Download(parameterMap);
     }
     public void parameterSetting(String type) {
          parameterMap.put("apikey.path", environment.getProperty("apikey.path"));
          parameterMap.put("apikey." + type + ".filename", environment.getProperty("apikey." + type + ".filename"));
          parameterMap.put("textFile.path", environment.getProperty("textFile.path"));
          parameterMap.put("ttsFile.path", environment.getProperty("ttsFile.path"));
          parameterMap.put("type", type);
     }
     public void registerCommonServiceInTtsService() {
          ttsService.setParameterMap(parameterMap);
          commonService.setTtsService(ttsService);
          commonService.setParameterMap(parameterMap);

     }

     @Override
     public void run(ApplicationArguments args) throws Exception {
          //execute("aws");
          //credentialInfoSave("naver");
          //createTestTextFile("naver");
          credentialInfoSave("aws");
     }
}
