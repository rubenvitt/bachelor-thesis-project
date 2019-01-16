package de.rubeen.bsc.entities.web;

public class AppUserEntity {
    private Integer id;
    private String name;
    private String mail;
    private String avatar;
    private String position;

    public AppUserEntity() {
    }

    public AppUserEntity(Integer id, String name, String mail, String avatar, String position) {
        this.id = id;
        this.name = name;
        this.mail = mail;
        this.avatar = avatar;
        this.position = position;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMail() {
        return mail;
    }

    public void setMail(String mail) {
        this.mail = mail;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }
}
