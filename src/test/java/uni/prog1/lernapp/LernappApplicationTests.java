package uni.prog1.lernapp;

import com.example.prog1learnapp.Prog1LearnApp;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = Prog1LearnApp.class)
class LernappApplicationTests {

	@Test
	void contextLoads() {
        System.out.println("lol");
	}

}
