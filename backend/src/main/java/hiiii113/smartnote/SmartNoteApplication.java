package hiiii113.smartnote;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@MapperScan("hiiii113.smartnote.mapper")
@EnableRetry
@EnableScheduling
public class SmartNoteApplication
{

    public static void main(String[] args)
    {
        SpringApplication.run(SmartNoteApplication.class, args);
    }

}
