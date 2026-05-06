package com.demo26.entity;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

//@AllArgsConstructor
@Data
public class Device {
    String name;
    Set<String> ips = new HashSet<>();
    Map<String, Integer> services = new HashMap<>();
}