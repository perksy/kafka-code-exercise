package com.perksy.kafkacodeexercise

import com.perksy.kafkacodeexercise.model.OrderResult
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController

@RestController
class ResultController() {

    @GetMapping("/orderresult/{id}")
    fun one(@PathVariable id: String): OrderResult? {
        return null
    }
}