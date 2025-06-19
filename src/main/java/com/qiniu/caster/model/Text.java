package com.qiniu.caster.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Text {
    private String content;
    private String fontcolor;
    private String bgcolor;
    private int opacity;
    private int size;
    private int w;
    private int h;
    private int x;
    private int y;
    private int z;
    private int offsetx;
    private int offsety;
    private int speedx;
    private int speedy;
}
