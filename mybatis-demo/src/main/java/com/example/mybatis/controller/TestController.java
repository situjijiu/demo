package com.example.mybatis.controller;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author JJBond
 * @date 2025-10-30 21:59
 */
@RestController
@RequestMapping("/test")
@RequiredArgsConstructor
@Validated
public class TestController {

    @GetMapping("/{id}")
    public String getUserById(
            @Valid
            @Min(value = 1L, message = "id不能小于1")
            @PathVariable("id") String id) {
        System.out.println(id);
        return null;
    }

}