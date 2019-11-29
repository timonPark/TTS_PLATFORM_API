package ion.ops.tts.demo.service;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.polly.AmazonPollyClient;
import com.amazonaws.services.polly.AmazonPollyClientBuilder;
import com.amazonaws.services.polly.model.*;
import com.google.protobuf.ByteString;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class AwsTtsService implements TtsService {

    @Autowired
    protected CommonService commonService;
    private Map<String, Object> parameterMap;
    private AmazonPollyClient polly;
    private Voice voice;
    private SynthesizeSpeechResult synthRes;

    public AwsTtsService() {}

    @Override
    public void run() throws IOException, ClassNotFoundException, ParseException {
        BasicAWSCredentials awsCredentials = new BasicAWSCredentials(String.valueOf(commonService.keyLoad().get("accessKey")),
                String.valueOf(commonService.keyLoad().get("secretKey")));
        polly = (AmazonPollyClient) AmazonPollyClientBuilder.standard().
                withCredentials(new AWSStaticCredentialsProvider(awsCredentials)).
                withRegion(Regions.AP_NORTHEAST_2).build();
        // Create describe voices request.
        DescribeVoicesRequest describeVoicesRequest = new DescribeVoicesRequest();

        // Synchronously ask Amazon Polly to describe available TTS voices.
        DescribeVoicesResult describeVoicesResult = polly.describeVoices(describeVoicesRequest);
        voice = describeVoicesResult.getVoices().get(0);
        SynthesizeSpeechRequest synthReq =
                new SynthesizeSpeechRequest().withText(
                        String.valueOf(commonService.getParameterMap().get("attachFileInText")))
                        .withVoiceId(VoiceId.Seoyeon)
                        .withOutputFormat(OutputFormat.Mp3);
        synthRes = polly.synthesizeSpeech(synthReq);
    }

    @Override
    public void ttsMp3Download(Map<String, Object> parameterMap) throws IOException, ParseException {
        commonService.setCreateFilePath();
        commonService.awsCreateMp3File();
        commonService.setResultMap();
    }

    @Override
    public void setParameterMap(Map<String, Object> parameterMap) { this.parameterMap = parameterMap; }

    @Override
    public InputStream getTtsMp3InpuStream() {
        return synthRes.getAudioStream();
    }

    @Override
    public ByteString getBypeString() {
        return null;
    }
}
