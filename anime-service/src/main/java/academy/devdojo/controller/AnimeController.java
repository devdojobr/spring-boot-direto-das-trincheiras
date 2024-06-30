package academy.devdojo.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("v1/animes")
@Slf4j
public class AnimeController {

    @GetMapping
    public List<String> listAll() throws InterruptedException {
        log.info(Thread.currentThread().getName());
        TimeUnit.SECONDS.sleep(1);
        return List.of("Ninja Kamui", "Kaijuu-8gou");
    }
}
