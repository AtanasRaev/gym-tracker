package com.gymtracker.backend

import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest(properties = ["exercise-import.on-startup=false"])
class BackendApplicationTests {

    @Test
    fun contextLoads() {
    }

}
