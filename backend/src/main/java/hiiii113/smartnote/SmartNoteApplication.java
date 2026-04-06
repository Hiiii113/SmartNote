package hiiii113.smartnote;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("hiiii113.smartnote.mapper")
public class SmartNoteApplication
{

    public static void main(String[] args)
    {
        SpringApplication.run(SmartNoteApplication.class, args);
    }

}
