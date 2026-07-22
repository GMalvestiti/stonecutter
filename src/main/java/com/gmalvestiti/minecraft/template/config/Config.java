package com.gmalvestiti.minecraft.template.config;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Config {

    @Expose
    @SerializedName("enabled")
    public boolean ENABLED = true;
}
