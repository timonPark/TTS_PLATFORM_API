package ion.ops.tts.demo.controller;

import ion.ops.tts.demo.run.Executor;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;


@RestController(value="/tts")
public class TtsController {

    @Autowired
    private Executor executor;

    //http://localhost:8080/create/aws
    @PostMapping("/create/{ttsType}")
    public Map<String, Object> createTTS(@PathVariable String ttsType, @RequestParam("file") MultipartFile file) throws Exception {
        return executor.responseExecute(ttsType, file);
    }

    @PostMapping("/register")
    public Map<String, Object> registerKey(@RequestParam(required = false) String id, @RequestParam(required = false) String pw,
                                           @RequestParam String type, @RequestParam(required = false) MultipartFile file) throws IOException {
        return executor.apiKeyStore(id, pw, type, file);
    }

    //http://localhost:8080/download/{filename}
    // filename is resultValue in createTTS CALL field "ttsFileName"
    @GetMapping("/download/{fileName}")
    public ResponseEntity<InputStreamResource> downloadTTS(@PathVariable String fileName) throws IOException, ParseException {
        return executor.ttsFileDownload(fileName);
    }

    @GetMapping("/list")
    public Map<String, Object> selectTTSFileList() throws Exception {
        return executor.responseTtsFileNameList();
    }

    @GetMapping("/isApiKey/{ttsType}")
    public Map<String, Object> isExistApiKey(@PathVariable String ttsType) {
        return executor.isExistApiKey(ttsType);
    }

    @GetMapping("/delete/{fileName}")
    public Map<String, Object> ttsFileDelete(@PathVariable String fileName) {
        return executor.ttsFileDelete(fileName);
    }

}
