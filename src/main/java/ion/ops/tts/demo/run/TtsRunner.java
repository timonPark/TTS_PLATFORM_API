package ion.ops.tts.demo.run;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class TtsRunner implements ApplicationRunner {

    @Autowired
    private Executor executor;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        //executor.run(args);
    }
}
