package com.hpsmjira.service;

import com.hp.schemas.sm._7.*;
import com.hp.schemas.sm._7.common.StringType;
import com.hpsmjira.model.ClosableProblem;
import com.hpsmjira.model.HPSMProblem;
import com.hpsmjira.model.JIRAIssue;
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
    private List<ClosableProblem> closableProblems = new ArrayList<ClosableProblem>();

    private ProblemManagement_Service problemManagementService;
    private ProblemManagement problemManagement;
    private String[] problemStatuses = {"Work In Progress", "Assign", "Categorize", "Accepted", "Pending", "Pending User", "Pending Vendor", "Resolved"};

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
            //url = new URL("http://localhost:8084/ProblemManagement?wsdl");
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

                    problemServiceNameMap.replace(problemNo, true); // changing value to TRUE, assuming that search in JIRA was made against this problem number

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

    public void closeProblem() {
        if(closableProblems != null && closableProblems.size() > 0) {
            System.out.println("closableProblems : "+closableProblems);

            for(ClosableProblem closableProblem : closableProblems) {
                System.out.println("Closing Problem No. "+closableProblem.getHpsmProblemId());

                CloseNEW9330035ProblemRequest closeNEW9330035ProblemRequest = new CloseNEW9330035ProblemRequest();
                NEW9330035ProblemModelType new9330035ProblemModelType = new NEW9330035ProblemModelType();

                NEW9330035ProblemKeysType problemKeysType = new NEW9330035ProblemKeysType();
                problemKeysType.setID(HPSMUtility.setProblemKeysTypeID(closableProblem.getHpsmProblemId()));
                new9330035ProblemModelType.setKeys(problemKeysType);

                NEW9330035ProblemInstanceType new9330035ProblemInstanceType = new NEW9330035ProblemInstanceType();
                new9330035ProblemInstanceType.setSubCategory(HPSMUtility.setSubCategory(closableProblem.getSubCategory()));
                new9330035ProblemInstanceType.setClosureCode(HPSMUtility.setClosureCode(closableProblem.getClosureCode()));

                new9330035ProblemInstanceType.setAssignee(HPSMUtility.setAssignee(closableProblem.getAssignee()));
                new9330035ProblemInstanceType.setAssignmentGroup(HPSMUtility.setAssignmentGroup(closableProblem.getAssigneeGroup()));

                NEW9330035ProblemInstanceType.Solution solution = HPSMUtility.createSolution();
                solution.setType(closableProblem.getSolution());
                new9330035ProblemInstanceType.setSolution(solution);

                new9330035ProblemModelType.setInstance(new9330035ProblemInstanceType);

                closeNEW9330035ProblemRequest.setModel(new9330035ProblemModelType);

                problemManagement.closeNEW9330035Problem(closeNEW9330035ProblemRequest);
            }

            System.out.println("Clearing closableProblems.");
            closableProblems.clear();

        }
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

    public List<ClosableProblem> getClosableProblems() {
        return closableProblems;
    }

    public void setClosableProblems(List<ClosableProblem> closableProblems) {
        this.closableProblems = closableProblems;
    }
}
