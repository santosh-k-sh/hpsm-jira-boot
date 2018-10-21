package com.hpsmjira.service;

import com.hp.schemas.sm._7.*;
import com.hp.schemas.sm._7.common.StringType;
import com.hpsmjira.model.HPSMProblem;
import com.hpsmjira.utility.HPSMUtility;
import com.sun.org.apache.xpath.internal.operations.Bool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.xml.bind.JAXBElement;
import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

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

    private Map<String, Boolean> problemServiceNameMap = new HashMap<String, Boolean>();

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

    public Map<String, RetrieveNEW9330035ProblemKeysListResponse> auth(String hpsmURL, String userName, String password) throws Exception {
        if(hpsmURL != null && hpsmURL.length() > 0) {
            hpsmURL = hpsmURL+"/ProblemManagement?wsdl";
            URL url = new URL(hpsmURL);
            problemManagementService = new ProblemManagement_Service(url);
        } else {
            problemManagementService = new ProblemManagement_Service();
        }

        problemManagement = problemManagementService.getProblemManagement();
        Authenticator myAuth = new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(userName, password.toCharArray());
            }
        };

        Authenticator.setDefault(myAuth);

        Map<String, RetrieveNEW9330035ProblemKeysListResponse> retrieveProblemKeysListResponse = retrieveProblemKeysList(problemManagement);


        return retrieveProblemKeysListResponse;
    }

    public Map<String, List<HPSMProblem>> loadProblemDetail(String projectKey, RetrieveNEW9330035ProblemKeysListResponse retrieveProblemKeysListResponse) {
        /* Reading Problem List */
        Map<String, List<HPSMProblem>> hpsmProblemMap = new HashMap<String, List<HPSMProblem>>();
        if(retrieveProblemKeysListResponse != null && retrieveProblemKeysListResponse.getMessage().equalsIgnoreCase("Success")) {
            List<String> problemLists = null;

            List<NEW9330035ProblemKeysType> problemKeysTypes = retrieveProblemKeysListResponse.getKeys();
            if(problemKeysTypes != null && problemKeysTypes.size() > 0) {
                problemLists = new ArrayList<String>();
                for(NEW9330035ProblemKeysType problemKeysType : problemKeysTypes) {
                    JAXBElement<StringType> problem = problemKeysType.getID();
                    problemLists.add(problem.getValue().getValue());
                }
            }


            if(problemLists != null && problemLists.size() > 0) {
                /* Loads Problem Details */
                List<HPSMProblem> hpsmProblemsNotAvailableInJIRA = new ArrayList<HPSMProblem>();
                for(String problemNo : problemLists) {
                    // Check if JIRA search was already made for all open problems
                    boolean alreadyExistsInJIRA = false;
                    Map<String, Boolean> selectedProblem = null;
                    if(problemServiceNameMap.containsKey(problemNo)) {
                        selectedProblem = problemServiceNameMap.entrySet()
                                .stream()
                                .filter(i -> i.getKey().equals(problemNo))
                                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

                        alreadyExistsInJIRA = selectedProblem.get(problemNo);
                    }

                    problemServiceNameMap.replace(problemNo, true);

                    /*Checks if the HPSM Problem already exists in JIRA */
                    if(!alreadyExistsInJIRA && jiraService.searchJiraTicket(problemNo).size() == 0) {
                        hpsmProblemsNotAvailableInJIRA.add(loadProblemDetails(problemManagement, problemNo));
                        hpsmProblemMap.put(projectKey, hpsmProblemsNotAvailableInJIRA);
                    }
                }
            }
        }

        return hpsmProblemMap;
    }

    public Map<String, RetrieveNEW9330035ProblemKeysListResponse> retrieveProblemKeysList(ProblemManagement problemManagement) {
        Map<String, RetrieveNEW9330035ProblemKeysListResponse> retrieveProblemKeysListResponseMap = new HashMap<String, RetrieveNEW9330035ProblemKeysListResponse>();

        if(userService != null && userService.getSelectedProjects() != null && userService.getSelectedProjects().size() > 0) {
            for(com.hpsmjira.model.Project selectedProject : userService.getSelectedProjects()) {
                String hpsmServiceName = selectedProject.getProjectName().split(" ")[0];

                for(String status : problemStatuses) {
                    /*RetrieveProblemKeysListRequest retrieveProblemKeysListRequest = new RetrieveProblemKeysListRequest();
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

                    System.out.println("Retrieving HPSM Service Name : " + hpsmServiceName + " for status : " + status);

                    retrieveProblemKeysListResponseMap.put(selectedProject.getProjectId()+":"+hpsmServiceName, problemManagement.retrieveProblemKeysList(retrieveProblemKeysListRequest));

                    System.out.println("ProblemKeysListResponse : " + retrieveProblemKeysListResponseMap.size() + " for status : " + status);*/


                    // New Operation call
                    retrieveProblemKeysListResponseMap.put(selectedProject.getProjectId()+":"+hpsmServiceName,  getProblemIdResponse(hpsmServiceName, status, null));

                    if(retrieveProblemKeysListResponseMap.size() > 0) {
                        List<String> problems = new ArrayList<String>();
                        for (Map.Entry<String, RetrieveNEW9330035ProblemKeysListResponse> problemKeysListResponseEntry : retrieveProblemKeysListResponseMap.entrySet()) {
                            System.out.println("Open Problems for service, " + problemKeysListResponseEntry.getKey() + " - " + problemKeysListResponseEntry.getValue().getKeys().size());

                            for(NEW9330035ProblemKeysType problemKeysType : problemKeysListResponseEntry.getValue().getKeys()) {
                                problems.add(problemKeysType.getID().getValue().getValue());
                            }
                        }

                        System.out.println("Total Open Problems : " + problems.size());

                        for(String problem : problems) {
                            if(!problemServiceNameMap.entrySet().stream()
                                    .filter(i -> i.getKey().equals(problem))
                                    .findFirst()
                                    .isPresent()) {
                                problemServiceNameMap.put(problem, false);
                            }
                        }
                    }
                }
            }
        } else {
            /* Dummy call to validate hpsm credential, as that api does not provide auth service */
            retrieveProblemKeysListResponseMap.put("Vision.BorderControl:TVIS",  getProblemIdResponse("Vision.BorderControl", "Categorize", 1L));

            /*RetrieveProblemKeysListRequest retrieveProblemKeysListRequest = new RetrieveProblemKeysListRequest();
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

            retrieveProblemKeysListResponseMap.put("Vision.BorderControl", problemManagement.retrieveProblemKeysList(retrieveProblemKeysListRequest));*/

        }

        System.out.println("retrieveProblemKeysListResponseMap : " + retrieveProblemKeysListResponseMap.size());
        return retrieveProblemKeysListResponseMap;

    }

    private RetrieveNEW9330035ProblemKeysListResponse getProblemIdResponse(String hpsmServiceName, String status, Long count) {
        RetrieveNEW9330035ProblemKeysListRequest retrieveNEW9330035ProblemKeysListRequest = new RetrieveNEW9330035ProblemKeysListRequest();
        NEW9330035ProblemModelType new9330035ProblemModelType = new NEW9330035ProblemModelType();
        new9330035ProblemModelType.setKeys(new NEW9330035ProblemKeysType());

        NEW9330035ProblemInstanceType new9330035ProblemInstanceType = new NEW9330035ProblemInstanceType();
        new9330035ProblemInstanceType.setService(HPSMUtility.setServiceValue(hpsmServiceName));
        new9330035ProblemInstanceType.setStatus(HPSMUtility.setStatusValue(status));

        new9330035ProblemModelType.setInstance(new9330035ProblemInstanceType);
        retrieveNEW9330035ProblemKeysListRequest.setModel(new9330035ProblemModelType);

        if(count != null) {
            retrieveNEW9330035ProblemKeysListRequest.setCount(count);
        }

        RetrieveNEW9330035ProblemKeysListResponse retrieveNEW9330035ProblemKeysListResponse = problemManagement.retrieveNEW9330035ProblemKeysList(retrieveNEW9330035ProblemKeysListRequest);
        return retrieveNEW9330035ProblemKeysListResponse;
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
