package com.zkkw.example.common.model;

import lombok.Data;

import java.io.Serializable;

@Data
public class User implements Serializable {

    private static final long serialVersionUID = -6184032636202519709L;

    private String name;
}
