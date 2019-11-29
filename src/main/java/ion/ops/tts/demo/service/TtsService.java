package ion.ops.tts.demo.service;
import com.google.protobuf.ByteString;
import org.json.simple.parser.ParseException;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

@Component
public interface TtsService {
    public void ttsMp3Download(Map<String, Object> parameterMap) throws IOException, ParseException;
    public void setParameterMap(Map<String, Object> parameterMap);
    public void run() throws IOException, ClassNotFoundException, ParseException;
    public InputStream getTtsMp3InpuStream();
    public ByteString getBypeString();
}
