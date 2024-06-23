// Module in Java 9 is completely wrong and stupid design.

module moe.orangemc.osu.al1s {
    requires jdk.httpserver;
    requires moe.orangemc.osu.al1s.api;
    requires com.google.gson;
    requires org.apache.commons.lang3;
    requires org.objectweb.asm;

    exports moe.orangemc.osu.al1s;
    exports moe.orangemc.osu.al1s.auth;
    exports moe.orangemc.osu.al1s.auth.credential;
    exports moe.orangemc.osu.al1s.auth.token;
    exports moe.orangemc.osu.al1s.bot;
    exports moe.orangemc.osu.al1s.library;
    exports moe.orangemc.osu.al1s.spi;
    exports moe.orangemc.osu.al1s.util;
}
