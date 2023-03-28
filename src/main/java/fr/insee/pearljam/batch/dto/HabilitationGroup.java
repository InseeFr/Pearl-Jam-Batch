package fr.insee.pearljam.batch.dto;

import java.util.List;

public class HabilitationGroup {

    // from Sugoi-Api domain
    // (https://github.com/InseeFr/sugoi-api/blob/b41da50388645a08d668fe9baf4b3e047c02ffda/sugoi-api-model/src/main/java/fr/insee/sugoi/model/Group.java)
    private String name;
    private String description;
    private String appName;
    private List<HabilitatedUser> users;

    public HabilitationGroup() {
        super();
    }

    public HabilitationGroup(List<HabilitatedUser> userList) {
        super();
        this.users = userList;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<HabilitatedUser> getUsers() {
        return users;
    }

    public void setUsers(List<HabilitatedUser> users) {
        this.users = users;
    }

}
