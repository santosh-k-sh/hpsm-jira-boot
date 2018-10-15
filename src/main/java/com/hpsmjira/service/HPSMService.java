package com.hpsmjira.service;

import com.hp.schemas.sm._7.*;
import com.hp.schemas.sm._7.common.StringType;
import com.hpsmjira.model.HPSMProblem;
import com.hpsmjira.utility.HPSMUtility;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.xml.bind.JAXBElement;
import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.util.*;

import static com.hpsmjira.utility.HPSMUtility.loadProblemDetails;

/**
 * Created by Santosh.Sharma on 9/27/2018.
 */
@Service
public class HPSMService {

    @Autowired
    JIRAService jiraService;

    @Autowired
    UserService userService;

    private ProblemManagement_Service problemManagementService;
    private ProblemManagement problemManagement;
    private String[] problemStatuses = {"Work In Progress", "Assign", "Categorize"};

    public void authenticate(String userName, String password) throws Exception {
        problemManagementService = new ProblemManagement_Service();

        problemManagement = problemManagementService.getProblemManagement();
        Authenticator myAuth = new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(userName, password.toCharArray());
            }
        };

        Authenticator.setDefault(myAuth);
    }

    public Map<String, RetrieveProblemKeysListResponse> auth(String userName, String password) throws Exception {
        problemManagementService = new ProblemManagement_Service();

        problemManagement = problemManagementService.getProblemManagement();
        Authenticator myAuth = new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(userName, password.toCharArray());
            }
        };

        Authenticator.setDefault(myAuth);

        Map<String, RetrieveProblemKeysListResponse> retrieveProblemKeysListResponse = retrieveProblemKeysList(problemManagement);


        return retrieveProblemKeysListResponse;
    }

    public Map<String, HPSMProblem> loadProblemDetail(String projectKey, RetrieveProblemKeysListResponse retrieveProblemKeysListResponse) {
        /* Reading Problem List */
        Map<String, HPSMProblem> hpsmProblemMap = new HashMap<String, HPSMProblem>();
        if(retrieveProblemKeysListResponse != null && retrieveProblemKeysListResponse.getMessage().equalsIgnoreCase("Success")) {
            List<String> problemLists = null;

            List<ProblemKeysType> problemKeysTypes = retrieveProblemKeysListResponse.getKeys();
            if(problemKeysTypes != null && problemKeysTypes.size() > 0) {
                problemLists = new ArrayList<String>();
                for(ProblemKeysType problemKeysType : problemKeysTypes) {
                    JAXBElement<StringType> problem = problemKeysType.getID();
                    problemLists.add(problem.getValue().getValue());
                }
            }


            if(problemLists != null && problemLists.size() > 0) {
                /* Loads Problem Details */
                for(String problemNo : problemLists) {
                    /*Checks if the HPSM Problem already exists in JIRA */
                    if(jiraService.searchJiraTicket(problemNo).size() == 0) {
                        hpsmProblemMap.put(projectKey, loadProblemDetails(problemManagement, problemNo));
                    }
                }
            }
        }

        return hpsmProblemMap;
    }

    public Map<String, RetrieveProblemKeysListResponse> retrieveProblemKeysList(ProblemManagement problemManagement) {
        Map<String, RetrieveProblemKeysListResponse> retrieveProblemKeysListResponseMap = new HashMap<String, RetrieveProblemKeysListResponse>();

        if(userService != null && userService.getSelectedProjects() != null && userService.getSelectedProjects().size() > 0) {
            for(com.hpsmjira.model.Project selectedProject : userService.getSelectedProjects()) {
                String hpsmServiceName = selectedProject.getProjectName().split(" ")[0];

                for(String status : problemStatuses) {
                    RetrieveProblemKeysListRequest retrieveProblemKeysListRequest = new RetrieveProblemKeysListRequest();
                    ProblemModelType model = new ProblemModelType();
                    ProblemKeysType key = new ProblemKeysType();

                    model.setKeys(key);

                    ProblemInstanceType instanceType = new ProblemInstanceType();
                    //instanceType.setImpact(HPSMUtility.setImpactValue("4"));

                    instanceType.setService(HPSMUtility.setServiceValue(hpsmServiceName));

                    instanceType.setStatus(HPSMUtility.setStatusValue(status));
                    model.setInstance(instanceType);

                    retrieveProblemKeysListRequest.setModel(model);
                    //retrieveProblemKeysListRequest.setCount(5L);

                    retrieveProblemKeysListResponseMap.put(selectedProject.getProjectId()+":"+hpsmServiceName, problemManagement.retrieveProblemKeysList(retrieveProblemKeysListRequest));
                }

            }
        } else {
            /* Dummy call to validate hpsm credential, as that api does not provide auth service */
            RetrieveProblemKeysListRequest retrieveProblemKeysListRequest = new RetrieveProblemKeysListRequest();
            ProblemModelType model = new ProblemModelType();
            ProblemKeysType key = new ProblemKeysType();

            model.setKeys(key);

            ProblemInstanceType instanceType = new ProblemInstanceType();
            //instanceType.setImpact(HPSMUtility.setImpactValue("4"));

            instanceType.setService(HPSMUtility.setServiceValue("Vision.BorderControl"));
            instanceType.setStatus(HPSMUtility.setStatusValue("Categorize"));
            model.setInstance(instanceType);

            retrieveProblemKeysListRequest.setModel(model);
            retrieveProblemKeysListRequest.setCount(1L);

            //
            /*ProblemTaskInstanceType problemTaskInstanceType = new ProblemTaskInstanceType();
            problemTaskInstanceType.setOpenTime(HPSMUtility.setOpenTime(new Date()));
            model.setInstance(problemTaskInstanceType);*/
            //

            retrieveProblemKeysListResponseMap.put("Vision.BorderControl", problemManagement.retrieveProblemKeysList(retrieveProblemKeysListRequest));



            //
            /*ProblemTaskInstanceType problemTaskInstanceType = new ProblemTaskInstanceType();
            problemTaskInstanceType.setOpenTime(HPSMUtility.setOpenTime(new Date()));
            model.setInstance(problemTaskInstanceType);*/
            //



            //retrieveProblemKeysListResponseMap.put("Vision.BorderControl", problemManagement.retrieveProblemKeysList(retrieveProblemTaskKeysListRequest));


        }



/*
        if(userService != null && userService.getSelectedProjects() != null && userService.getSelectedProjects().size() > 0) {
            for(com.hpsmjira.model.Project selectedProject : userService.getSelectedProjects()) {
                String hpsmServiceName = selectedProject.getProjectName().split(" ")[0];

                instanceType.setService(HPSMUtility.setServiceValue(hpsmServiceName));
            }
        } else {
            instanceType.setService(HPSMUtility.setServiceValue("Vision.BorderControl"));
        }*/

        /*if(userService.getUser() != null && userService.getUser().getBusinessServiceMapping() != null &&
                userService.getUser().getBusinessServiceMapping().getHpsmProject() != null) {
            instanceType.setService(HPSMUtility.setServiceValue(userService.getUser().getBusinessServiceMapping().getHpsmProject().getProjectName()));
        } else {
            instanceType.setService(HPSMUtility.setServiceValue("TVIS"));
        }*/



        return retrieveProblemKeysListResponseMap;

    }

    public ProblemManagement_Service getProblemManagementService() {
        return problemManagementService;
    }

    public void setProblemManagementService(ProblemManagement_Service problemManagementService) {
        this.problemManagementService = problemManagementService;
    }

    public ProblemManagement getProblemManagement() {
        return problemManagement;
    }

    public void setProblemManagement(ProblemManagement problemManagement) {
        this.problemManagement = problemManagement;
    }
}
