package ru.netology;

public enum Addresses {
    INDEX("index.html"),
    SPRINGS("/spring.svg"),
    SPRING("/spring.png"),
    STYLES("/styles.css"),
    APP("/app.js"),
    LINKS("/links.html"),
    FORMS("/forms.html"),
    EVENTS("/events.html"),
    EVENTS2("/events.js"),
    CLASSIC("/classic.html");

    private final String address;

    Addresses(String address) {
        this.address = address;
    }

    public String getAddress() {
        return address;
    }
}
