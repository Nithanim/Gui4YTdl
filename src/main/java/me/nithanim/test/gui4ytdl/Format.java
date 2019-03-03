package me.nithanim.test.gui4ytdl;

import lombok.Value;

@Value
public class Format {
    String code;
    String extension;
    String resolution;
    String note;

    boolean audioOnly;
    boolean videoOnly;
    boolean best;
}
