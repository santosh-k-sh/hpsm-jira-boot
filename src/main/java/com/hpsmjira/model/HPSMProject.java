package com.hpsmjira.model;

/**
 * Created by Santosh.Sharma on 9/28/2018.
 */
public class HPSMProject {
    private String projectId;
    private String projectName;

    public HPSMProject() {

    }

    public HPSMProject(String projectId, String projectName) {
        this.projectId = projectId;
        this.projectName = projectName;
    }

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }
}
