package ion.ops.tts.demo.controller;

import ion.ops.tts.demo.run.Executor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.xml.ws.Response;
import java.util.Map;


@RestController(value="tts")
public class TtsController {

    @Autowired
    private Executor executor;

    @PostMapping("/create/{ttsType}")
    public Map<String, Object> createTTS(@PathVariable String ttsType, @RequestPart MultipartFile multipartFile) throws Exception {
        return executor.responseExecute(ttsType, multipartFile);
    }

    @GetMapping("/download/{fileName}")
    public Map<String, Object> downloadTTS(@PathVariable String fileName){
        return null;
    }
}
