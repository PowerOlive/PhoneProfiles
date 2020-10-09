package com.thelittlefireman.appkillermanager.utils;

@SuppressWarnings("unused")
public enum Manufacturer {
    XIAOMI("xiaomi"),
    SAMSUNG("samsung"),
    OPPO("oppo"),
    HUAWEI("huawei"),
    MEIZU("meizu"),
    ONEPLUS("oneplus"),
    LETV("letv"),
    ASUS("asus"),
    HTC("htc"),
    ZTE("zte"),
    VIVO("vivo");

    @SuppressWarnings({"FieldCanBeLocal"})
    private final String name;

    Manufacturer(String device){
        name = device;
    }

    /*
    @Override
    public String toString() {
        return super.toString();
    }
    */
}
