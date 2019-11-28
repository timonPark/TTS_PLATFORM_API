package ion.ops.tts.demo.service;

import com.google.api.gax.core.FixedCredentialsProvider;
import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.cloud.texttospeech.v1.*;
import com.google.protobuf.ByteString;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
@Component
public class GoogleTtsService implements TtsService {

    private SynthesizeSpeechResponse response;
    private Map<String, Object> parameterMap;

    @Autowired
    protected CommonService commonService;

    public GoogleTtsService() { }

    @Override
    public void run() throws IOException {
        ServiceAccountCredentials credentials = ServiceAccountCredentials
                .fromStream(Files.newInputStream(Paths.get(String.valueOf(parameterMap.get("apikey.path")) +
                        String.valueOf(parameterMap.get("apikey.google.filename")))));
        TextToSpeechSettings settings = TextToSpeechSettings.newBuilder()
                .setCredentialsProvider(FixedCredentialsProvider.create(credentials)).build();
        try (TextToSpeechClient textToSpeechClient = TextToSpeechClient.create(settings)) {
            // Set the text input to be synthesized
            SynthesisInput input = SynthesisInput.newBuilder().setText(
                    commonService.readTextFile(commonService.mkdirForderRetrunForderPath("textFile.path")+"upload.json")
            ).build();
            // Build the voice request
            VoiceSelectionParams voice =
                    VoiceSelectionParams.newBuilder()
                            .setLanguageCode("ko-KR") // languageCode = "en_us"
                            .setSsmlGender(SsmlVoiceGender.FEMALE) // ssmlVoiceGender = SsmlVoiceGender.FEMALE
                            .build();

            // Select the type of audio file you want returned
            AudioConfig audioConfig =
                    AudioConfig.newBuilder()
                            .setAudioEncoding(AudioEncoding.MP3) // MP3 audio.
                            .build();

            // Perform the text-to-speech request
            response = textToSpeechClient.synthesizeSpeech(input, voice, audioConfig);
        }
    }

    @Override
    public void ttsMp3Download( Map<String, Object> parameterMap) {
        commonService.googleCreateMp3File();
    }

    @Override
    public void setParameterMap(Map<String, Object> parameterMap) {
        this.parameterMap = parameterMap;
    }

    @Override
    public InputStream getTtsMp3InpuStream() {
        return null;
    }

    @Override
    public ByteString getBypeString() {
        return response.getAudioContent();
    }
}
