package com.sdl.lt.lc.json.streaming.numbers.model;

import lombok.Data;

import java.util.List;

/**
 * @author anegruti
 * @since 10/31/2022
 */
@Data
public class MyNumbers {

    private String id;
    private NumbersOwner requester;
    private List<Integer> numbers;

}
