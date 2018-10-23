package com.hpsmjira.controller;

import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.hp.schemas.sm._7.RetrieveNEW9330035ProblemKeysListResponse;
import com.hp.schemas.sm._7.RetrieveProblemKeysListResponse;
import com.hpsmjira.model.*;
import com.hpsmjira.model.Project;
import com.hpsmjira.service.HPSMService;
import com.hpsmjira.service.JIRAService;
import com.hpsmjira.service.UserService;
import com.hpsmjira.utility.LookupProvider;
import com.hpsmjira.model.HPSMCredential;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;
import sun.net.www.protocol.http.AuthCacheImpl;
import sun.net.www.protocol.http.AuthCacheValue;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.springframework.web.bind.annotation.*;

/**
 * Created by Santosh.Sharma on 9/27/2018.
 */

@RestController
public class ConfigurationController {

    @Autowired
    JIRAService jiraService;

    @Autowired
    HPSMService hpsmService;

    @Autowired
    LookupProvider lookupProvider;

    @Autowired
    UserService userService;

    static ScheduledExecutorService executor;
    static Runnable runnable;
    static ScheduledFuture<?> future;

    @GetMapping("/allProjects")
    @CrossOrigin(origins = "http://localhost:3000")
    public List<com.hpsmjira.model.Project> allProjects() {
        List<com.hpsmjira.model.Project> projectList = new ArrayList<>();
        com.hpsmjira.model.Project project = new com.hpsmjira.model.Project();
        project.setProjectId("001");
        project.setProjectName("Test");
        projectList.add(project);

        return projectList;
    }

    @PostMapping(path = "/members", consumes = "application/json", produces = "application/json")
    public List<com.hpsmjira.model.Project> addMember(@RequestBody String hpsmUserName) throws JSONException {
        //code
        JSONObject responseJson = new JSONObject();
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.add("Content-Type", "application/json; charset=utf-8");

        List<com.hpsmjira.model.Project> projectList = new ArrayList<>();
        com.hpsmjira.model.Project project = new com.hpsmjira.model.Project();
        project.setProjectId("001");
        project.setProjectName("Test");

        responseJson.put("projectList", projectList);

        return projectList;
    }



    /*
    * REACT JS Changes
    *
    * */

    @RequestMapping(value = "/validatehpsm", method = RequestMethod.POST)
    public boolean validateHPSMCredential(@RequestBody HPSMCredential hpsmCredential) throws JSONException {
        JSONObject responseJson = new JSONObject();
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.add("Content-Type", "application/json; charset=utf-8");

        User user = new User();
        Hpsm  hpsmUser = new Hpsm();
        hpsmUser.setHpsmURL(hpsmCredential.getHpsmUrl());
        hpsmUser.setHpsmUserName(hpsmCredential.getHpsmUserName());
        hpsmUser.setHpsmPassword(hpsmCredential.getHpsmPassword());
        hpsmUser.setAuthenticated(false);
        user.setHpsmUser(hpsmUser);

        if(userService.getUser() != null) {
            userService.getUser().setHpsmUser(hpsmUser);
        }

        try {
            System.out.println("calling hpsm..");

            Map<String, RetrieveNEW9330035ProblemKeysListResponse> retrieveProblemKeysListResponse = hpsmService.auth(user.getHpsmUser().getHpsmURL(), user.getHpsmUser().getHpsmUserName(),user.getHpsmUser().getHpsmPassword());

            user.getHpsmUser().setAuthenticated(true);
            responseJson.put("AUTHENTICATED", true);

            AuthCacheValue.setAuthCache(new AuthCacheImpl());

            userService.setUser(user);

            if(userService.getUser().getHpsmUser() != null && userService.getUser().getHpsmUser().isAuthenticated() &&
                    userService.getUser().getJiraUser() != null && userService.getUser().getJiraUser().isAuthenticated()) {
                user.setUserAuthenticated(true);
                userService.getUser().setUserAuthenticated(true);
            }

        } catch (Exception e) {
            if(user.getHpsmUser() !=null) {
                user.getHpsmUser().setAuthenticated(false);
            }
            if(userService.getUser() != null) {
                userService.getUser().setUserAuthenticated(false);
            }

            responseJson.put("AUTHENTICATED", false);
        }

        if(userService.getUser() == null) {
            userService.setUser(user);
        } else {
            userService.getUser().setHpsmUser(user.getHpsmUser());
        }

        System.out.println("User Authenticated : "+user.getHpsmUser().isAuthenticated());
        return user.getHpsmUser().isAuthenticated();
    }


    @RequestMapping(value = "/authenticatejira", method = RequestMethod.POST)
    public boolean authenticatejira(@RequestBody Jira jiraCredential) throws JSONException {
        JSONObject responseJson = new JSONObject();
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.add("Content-Type", "application/json; charset=utf-8");

        if(userService.getUser() != null) {
            userService.getUser().setJiraUser(jiraCredential);
        } else if(userService.getUser() == null) {
            User user = new User();
            user.setJiraUser(jiraCredential);
            userService.setUser(user);
        }

        try {
            jiraService.login(jiraCredential);
            userService.getUser().getJiraUser().setAuthenticated(true);
            responseJson.put("AUTHENTICATED", true);
        } catch (Exception e) {
            responseJson.put("AUTHENTICATED", false);
            if(userService.getUser().getJiraUser() != null) {
                userService.getUser().getJiraUser().setAuthenticated(false);
            }

            if(userService.getUser() != null) {
                userService.getUser().setUserAuthenticated(false);
            }
        }

        if(userService.getUser().getHpsmUser() != null && userService.getUser().getHpsmUser().isAuthenticated() &&
                userService.getUser().getJiraUser() != null && userService.getUser().getJiraUser().isAuthenticated()) {
            userService.getUser().setUserAuthenticated(true);
            userService.getUser().setUserAuthenticated(true);
        }

        System.out.println("JIRA User Authenticated : "+userService.getUser().getJiraUser().isAuthenticated());
        return userService.getUser().getJiraUser().isAuthenticated();
    }

    /*@CrossOrigin(origins = "http://localhost:3000")*/
    @RequestMapping(value = "/hpsmProjects")
    public List<HPSMProject>  hpsmProjects() {
        return lookupProvider.loadHPSMProjects();
    }

    @RequestMapping(value = "/jiraProjects")
    public List<JIRAProject>  jiraProjects() {
        return lookupProvider.loadJIRAProjects();
    }

    @RequestMapping(value = "/addBusinessMappings")
    public List<Project> addBusinessMappings(@RequestParam String selectedHPSMProjectName, @RequestParam String selectedJIRAProjectName) {
        List<Project> selectedProjects = userService.getSelectedProjects();
        String selectedHPSMProjectId = null;
        String selectedJIRAProjectId = null;

        if(selectedProjects == null) {
            selectedProjects = new ArrayList<Project>();
        }

        if(Objects.equals(selectedHPSMProjectName, "") || Objects.equals(selectedJIRAProjectName, "")) {
            return selectedProjects;
        }

        for(HPSMProject project : lookupProvider.loadHPSMProjects()) {
            if(project.getProjectName().equals(selectedHPSMProjectName)) {
                selectedHPSMProjectId = project.getProjectId();
                break;
            }
        }

        if(selectedHPSMProjectId != null) {
            for(JIRAProject jiraProject : lookupProvider.loadJIRAProjects()) {
                if(selectedJIRAProjectName.equals(jiraProject.getProjectId())) {
                    selectedJIRAProjectId = jiraProject.getProjectId();
                    break;
                }
            }
        }

        Project project = new Project();
        project.setProjectId(selectedHPSMProjectId);
        project.setProjectName(selectedHPSMProjectName + " [" + selectedJIRAProjectId + "]");

        selectedProjects.add(project);
        userService.setSelectedProjects(selectedProjects);

        return selectedProjects;
    }

    @RequestMapping(value = "/removeBusinessMappings")
    public List<Project> removeBusinessMappings(@RequestParam String selectedProjectIndexToRemove) {
        List<Project> selectedProjects = userService.getSelectedProjects();

        if(Objects.equals(selectedProjectIndexToRemove, "")) {
            return selectedProjects;
        }

        selectedProjects.remove(Integer.parseInt(selectedProjectIndexToRemove));
        userService.setSelectedProjects(selectedProjects);

        return selectedProjects;
    }

    @RequestMapping(value = "/getSelectedProjectMappings")
    public List<Project> getSelectedProjectMappings() {
        return userService.getSelectedProjects();
    }


    @RequestMapping(value = "/loadHPSMProblems")
    public List<HPSMProblem> loadHPSMProblems(@RequestParam String selectedJobHour) {
        List<HPSMProblem> hpsmProblems = new ArrayList<HPSMProblem>();
        /*loadHPSMProblemToProcess();

        if(userService.getProblemToMigrate() != null && userService.getProblemToMigrate().entrySet().size() > 0) {
            for(Map.Entry<String, HPSMProblem> hpsmProblemEntry : userService.getProblemToMigrate().entrySet()) {
                hpsmProblems.add(hpsmProblemEntry.getValue());
            }
        }*/

        /*loadHPSMProblemToProcess();

        if(userService.getProblemToMigrate() != null && userService.getProblemToMigrate().size() > 0) {
            System.out.println("Problems to migrate : " + userService.getProblemToMigrate().entrySet().size());
            jiraService.createJIRATicket();
        }*/

        if(selectedJobHour != null && selectedJobHour.length() > 0) {
            //changeDelay(Long.parseLong(selectedJobHour));
            initiateJob(Long.parseLong(selectedJobHour));
        }

        if(userService.getProblemToMigrate() != null && userService.getProblemToMigrate().entrySet().size() > 0) {
            for(Map.Entry<String, List<HPSMProblem>> hpsmProblemDetail : userService.getProblemToMigrate().entrySet()) {
                for(HPSMProblem hpsmProblem : hpsmProblemDetail.getValue()) {
                    hpsmProblems.add(hpsmProblem);
                }
            }
        }

        return hpsmProblems;
    }

    private void loadHPSMProblemToProcess() {
        if(hpsmService.getProblemManagement() != null) {
            Map<String, RetrieveNEW9330035ProblemKeysListResponse> retrieveProblemKeysListResponseMap = hpsmService.retrieveProblemKeysList(hpsmService.getProblemManagement());

            if(retrieveProblemKeysListResponseMap != null) {
                for (Map.Entry<String, RetrieveNEW9330035ProblemKeysListResponse> retrieveProblemKeysListResponse : retrieveProblemKeysListResponseMap.entrySet()) {
                    String projectKey = retrieveProblemKeysListResponse.getKey().split(":")[0];
                    populateHPSMProblem(projectKey, retrieveProblemKeysListResponse.getValue());
                }
            }
        }
    }

    private void populateHPSMProblem(String projectKey, RetrieveNEW9330035ProblemKeysListResponse retrieveProblemKeysListResponse) {
        Map<String, List<HPSMProblem>> hpsmProblemMap = new HashMap<String, List<HPSMProblem>>();
        hpsmProblemMap.put(null, new ArrayList<HPSMProblem>());

        List<JIRAIssue> jiraIssues = new ArrayList<JIRAIssue>();
        jiraIssues.add(new JIRAIssue());

        if(userService != null && /*userService.getUser() != null && userService.getUser().isUserAuthenticated() &&*/
                hpsmService != null && hpsmService.getProblemManagementService() != null) {
            hpsmProblemMap.clear();
            hpsmProblemMap = hpsmService.loadProblemDetail(projectKey, retrieveProblemKeysListResponse);

            if(userService.getProblemToMigrate() != null && userService.getProblemToMigrate().entrySet().size() > 0) {
                userService.getProblemToMigrate().putAll(hpsmProblemMap);
            } else if (hpsmProblemMap.size() > 0){
                userService.setProblemToMigrate(hpsmProblemMap);
            }
        }
    }

    public void initiateJob(long scheduleHourlyDelay) {
        executor = Executors.newScheduledThreadPool(1);
        runnable = new Runnable() {
            @Override
            public void run() {
                System.out.println("Job initiated at..."+ new Date());
                if(userService.getUser().isUserAuthenticated()) {
                    loadHPSMProblemToProcess();
                }

                if(userService.getProblemToMigrate() != null && userService.getProblemToMigrate().size() > 0) {
                    System.out.println("============================================================================");
                    System.out.println("Problems to migrate : " + userService.getProblemToMigrate().entrySet().size());
                    jiraService.createJIRATicket();
                }

                // Close Problems
                if(hpsmService.getClosableProblems() != null && hpsmService.getClosableProblems().size() > 0) {
                    hpsmService.closeProblem();
                }
            }
        };
        future = executor.scheduleWithFixedDelay(runnable, 0, scheduleHourlyDelay, TimeUnit.MINUTES);

        try {
            Thread.sleep(20000l);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void changeDelay(long scheduleHourlyDelay) {
        boolean res = future.cancel(false);
        System.out.println("Previous task canceled: " + res);

        future = executor.scheduleWithFixedDelay(runnable, 0, scheduleHourlyDelay, TimeUnit.HOURS);
    }

}

