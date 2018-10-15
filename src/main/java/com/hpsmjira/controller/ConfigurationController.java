package com.hpsmjira.controller;

import com.atlassian.jira.rest.client.api.JiraRestClient;
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
    public boolean validatehpsm(@RequestBody HPSMCredential hpsmCredential) throws JSONException {
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

            Map<String, RetrieveProblemKeysListResponse> retrieveProblemKeysListResponse = hpsmService.auth(user.getHpsmUser().getHpsmUserName(),user.getHpsmUser().getHpsmPassword());

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

        /*if(userService.getUser() == null) {
            userService.setUser(user);
        } else {
            userService.getUser().setJiraUser(user.getJiraUser());
        }*/

        if(userService.getUser().getHpsmUser() != null && userService.getUser().getHpsmUser().isAuthenticated() &&
                userService.getUser().getJiraUser() != null && userService.getUser().getJiraUser().isAuthenticated()) {
            userService.getUser().setUserAuthenticated(true);
            userService.getUser().setUserAuthenticated(true);
        }

        return userService.getUser().getJiraUser().isAuthenticated();
    }

    @CrossOrigin(origins = "http://localhost:3000")
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

        /*if(userService != null && userService.getUser() != null && userService.getUser().isUserAuthenticated()) {
            selectedProjects = userService.getSelectedProjects();
        }*/

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


    @RequestMapping(value = "/loadHPSMProblems")
    public List<HPSMProblem> loadHPSMProblems(@RequestParam String selectedJobHour) {
        List<HPSMProblem> hpsmProblems = new ArrayList<HPSMProblem>();
        /*loadHPSMProblemToProcess();

        if(userService.getProblemToMigrate() != null && userService.getProblemToMigrate().entrySet().size() > 0) {
            for(Map.Entry<String, HPSMProblem> hpsmProblemEntry : userService.getProblemToMigrate().entrySet()) {
                hpsmProblems.add(hpsmProblemEntry.getValue());
            }
        }*/

        loadHPSMProblemToProcess();

        if(userService.getProblemToMigrate() != null && userService.getProblemToMigrate().size() > 0) {
            System.out.println("Problems to migrate : " + userService.getProblemToMigrate().entrySet().size());
            jiraService.createJIRATicket();
        }

        initiateJob();
        changeDelay(Long.parseLong(selectedJobHour));

        return hpsmProblems;
    }

    private void loadHPSMProblemToProcess() {
        if(hpsmService.getProblemManagement() != null) {
            Map<String, RetrieveProblemKeysListResponse> retrieveProblemKeysListResponseMap = hpsmService.retrieveProblemKeysList(hpsmService.getProblemManagement());

            if(retrieveProblemKeysListResponseMap != null) {
                for (Map.Entry<String, RetrieveProblemKeysListResponse> retrieveProblemKeysListResponse : retrieveProblemKeysListResponseMap.entrySet()) {
                    String projectKey = retrieveProblemKeysListResponse.getKey().split(":")[0];
                    populateHPSMProblem(projectKey, retrieveProblemKeysListResponse.getValue());
                }
            }
        }
    }

    private void populateHPSMProblem(String projectKey, RetrieveProblemKeysListResponse retrieveProblemKeysListResponse) {
        Map<String, HPSMProblem> hpsmProblemMap = new HashMap<String, HPSMProblem>();
        hpsmProblemMap.put(null, new HPSMProblem());

        List<JIRAIssue> jiraIssues = new ArrayList<JIRAIssue>();
        jiraIssues.add(new JIRAIssue());

        if(userService != null && /*userService.getUser() != null && userService.getUser().isUserAuthenticated() &&*/
                hpsmService != null && hpsmService.getProblemManagementService() != null) {
            hpsmProblemMap.clear();
            hpsmProblemMap = hpsmService.loadProblemDetail(projectKey, retrieveProblemKeysListResponse);

            userService.setProblemToMigrate(hpsmProblemMap);
        }
    }

    public void initiateJob() {
        executor = Executors.newScheduledThreadPool(1);
        runnable = new Runnable() {
            @Override
            public void run() {
                System.out.println("Inside runnable");
                loadHPSMProblemToProcess();

                if(userService.getProblemToMigrate() != null && userService.getProblemToMigrate().size() > 0) {
                    System.out.println("Problems to migrate : " + userService.getProblemToMigrate().entrySet().size());
                    jiraService.createJIRATicket();
                }



            }
        };
        future = executor.scheduleWithFixedDelay(runnable, 0, 10, TimeUnit.MINUTES);

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

    /*
    * REACT JS Changes ends
    *
    * */

















    @RequestMapping(value = "/authenticatehpsm", method = RequestMethod.POST, produces = {"application/json"})
    public ResponseEntity<String> authenticateHPSMUser(@ModelAttribute(value = "loginFormData") User user, ModelMap modelMap) throws JSONException {

        JSONObject responseJson = new JSONObject();
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.add("Content-Type", "application/json; charset=utf-8");

        try {
            System.out.println("calling hpsm..");

            Map<String, RetrieveProblemKeysListResponse> retrieveProblemKeysListResponse = hpsmService.auth(user.getHpsmUser().getHpsmUserName(),user.getHpsmUser().getHpsmPassword());

            user.getHpsmUser().setAuthenticated(true);
            modelMap.put("AUTHENTICATED", "AUTHENTICATED");
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

        return new ResponseEntity<String>(responseJson.toString(), responseHeaders, HttpStatus.OK);
    }



    @RequestMapping(value = "/authenticatejira1", method = RequestMethod.POST, produces = {"application/json"})
    public ResponseEntity<String> authenticateJIRAUser(@ModelAttribute(value = "loginFormData") User user, ModelMap modelMap) throws JSONException {

        JSONObject responseJson = new JSONObject();
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.add("Content-Type", "application/json; charset=utf-8");

        try {
            jiraService.login(user.getJiraUser());
            user.getJiraUser().setAuthenticated(true);
            responseJson.put("AUTHENTICATED", true);
        } catch (Exception e) {
            responseJson.put("AUTHENTICATED", false);
            if(user.getJiraUser() != null) {
                user.getJiraUser().setAuthenticated(false);
            }

            if(userService.getUser() != null) {
                userService.getUser().setUserAuthenticated(false);
            }

        }

        if(userService.getUser() == null) {
            userService.setUser(user);
        } else {
            userService.getUser().setJiraUser(user.getJiraUser());
        }

        if(userService.getUser().getHpsmUser() != null && userService.getUser().getHpsmUser().isAuthenticated() &&
                userService.getUser().getJiraUser() != null && userService.getUser().getJiraUser().isAuthenticated()) {
            user.setUserAuthenticated(true);
            userService.getUser().setUserAuthenticated(true);
        }

        return new ResponseEntity<String>(responseJson.toString(), responseHeaders, HttpStatus.OK);
    }


    @RequestMapping(value = "/addBusinessMappings1", method = RequestMethod.GET, produces = {"application/json"})
    public String addBusinessMappings1(@ModelAttribute(value = "userObj") User user, Model model) {
        /*JSONObject responseJson = new JSONObject();
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.add("Content-Type", "application/json; charset=utf-8");
        initiateJob();*/

        List<com.hpsmjira.model.Project> selectedProjects = userService.getSelectedProjects();



        if(user.getSelectedProjectToRemove() == null) {
            /* TO add */
            com.hpsmjira.model.Project project = new com.hpsmjira.model.Project();
            project.setProjectId(user.getBusinessServiceMapping().getSelectedJIRAProjectId());
            project.setProjectName(user.getBusinessServiceMapping().getSelectedHPSMProjectName() + " [" + user.getBusinessServiceMapping().getSelectedJIRAProjectId() + "]");

            if (selectedProjects == null) {
                selectedProjects = new ArrayList<com.hpsmjira.model.Project>();
            }

            selectedProjects.add(project);
            userService.setSelectedProjects(selectedProjects);
        } else if(user.getSelectedProjectToRemove() != null) {
            for(com.hpsmjira.model.Project projectToRemove : selectedProjects) {
                System.out.println(projectToRemove);
                if(projectToRemove.getProjectName().equals(user.getSelectedProjectToRemove())) {
                    selectedProjects.remove(projectToRemove);
                    break;
                }
            }
        }

        user.setSelectedProjectToRemove(null);
        List<com.hpsmjira.model.Project> projectList = new ArrayList<>();
        model.addAttribute("projectMap", projectList);
        model.addAttribute("projectMap", projectList);

        model.addAttribute("testMsg", "hello");

        return "configure";
    }


    @RequestMapping(value = "/guests", method = RequestMethod.GET)
    public String showGuestList(Model model) {

        List<com.hpsmjira.model.Project> projectList = new ArrayList<>();
        com.hpsmjira.model.Project project = new com.hpsmjira.model.Project();
        project.setProjectId("T1");
        project.setProjectName("Time Project");
        projectList.add(project);

        model.addAttribute("guests", projectList);

        return "resultsList :: resultsList";
    }

    @RequestMapping(value = "/getBlacklistDetails")
    public String getBlacklistDetails(HttpServletRequest request, ModelMap modelMap) {

        List<com.hpsmjira.model.Project> projectList = new ArrayList<>();
        com.hpsmjira.model.Project project = new com.hpsmjira.model.Project();
        project.setProjectId("T1");
        project.setProjectName("Time Project");
        projectList.add(project);

        modelMap.addAttribute("project", project);

        return "templates/fragments/blacklist :: blacklist";
    }










    @PostMapping(value = "configureHPSM")
    public String configureHPSM(@ModelAttribute("hpsmCredential") Hpsm hpsmForm, HttpSession session, Model model) {
        User user = new User();

        session.setAttribute("hpsmCredentials", hpsmForm);

        if(session.getAttribute("USER") != null) {
            user = (User) session.getAttribute("USER");
        }
        user.setHpsmUser(hpsmForm);
        user.getHpsmUser().setAuthenticated(false);

        try {
            hpsmService.authenticate(hpsmForm.getHpsmUserName(), hpsmForm.getHpsmPassword());
            /*RetrieveProblemKeysListResponse retrieveProblemKeysListResponse = */hpsmService.retrieveProblemKeysList(hpsmService.getProblemManagement());

            user.getHpsmUser().setAuthenticated(true);

            model.addAttribute("hpsmAuthenticated", "TRUE");
        } catch (Exception e) {
            model.addAttribute("hpsmException", "HPSM Authentication Failed !!");
            model.addAttribute("hpsmAuthenticated", null);
            session.setAttribute("hpsmProblems", null);
        }

        if(user.getHpsmUser() != null && user.getHpsmUser().isAuthenticated() &&
                user.getJiraUser() != null && user.getJiraUser().isAuthenticated()) {
            user.setUserAuthenticated(true);
        }
        session.setAttribute("USER", user);
        userService.setUser(user);

        if(session.getAttribute("jiraCredentials") != null) {
            Jira jiraFormFromSession = (Jira) session.getAttribute("jiraCredentials");
            model.addAttribute("jiraCredential", jiraFormFromSession);
        } else {
            model.addAttribute("jiraCredential", new Jira());
        }

        if(session.getAttribute("hpsmCredential") != null) {
            Hpsm hpsmFormFromSession = (Hpsm) session.getAttribute("hpsmCredential");
            model.addAttribute("hpsmCredential", hpsmFormFromSession);
        } else {
            model.addAttribute("hpsmCredential", new Hpsm());
        }

        // JIRA Project setup
        model.addAttribute("jiraProjects", lookupProvider.getJiraProjects());

        // HPSM Project setup
        model.addAttribute("hpsmProjects", lookupProvider.getHpsmProjects());

        model.addAttribute("user", user);

        return "configure";
    }




    @PostMapping(value = "configureJIRA")
    public String configureJIRA(@ModelAttribute("jiraCredential") Jira jiraForm, HttpSession session, Model model) {
        User user = new User();

        List<HPSMProject> hpsmProjects1 = lookupProvider.loadHPSMProjects();

        session.setAttribute("jiraCredentials", jiraForm);

        if(session.getAttribute("USER") != null) {
            user = (User) session.getAttribute("USER");
        }
        user.setJiraUser(jiraForm);
        user.getJiraUser().setAuthenticated(false);

        JiraRestClient jiraRestClient = null;

        try {
            jiraRestClient = jiraService.login(jiraForm);
            model.addAttribute("jiraException", null);
            model.addAttribute("jiraAuthenticated", "TRUE");

            user.getJiraUser().setAuthenticated(true);

        } catch (Exception e) {
            model.addAttribute("jiraException", "JIRA Authentication Failed !!");
            model.addAttribute("jiraAuthenticated", null);
        }

        if(user.getHpsmUser() != null && user.getHpsmUser().isAuthenticated() &&
                user.getJiraUser() != null && user.getJiraUser().isAuthenticated()) {
            user.setUserAuthenticated(true);
        }

        session.setAttribute("USER", user);
        userService.setUser(user);

        model.addAttribute("jiraRestClient", jiraRestClient);

        if(session.getAttribute("jiraCredentials") != null) {
            Jira jiraFormFromSession = (Jira) session.getAttribute("jiraCredentials");
            model.addAttribute("jiraCredential", jiraFormFromSession);
        } else {
            model.addAttribute("jiraCredential", new Jira());
        }

        if(session.getAttribute("hpsmCredential") != null) {
            Hpsm hpsmFormFromSession = (Hpsm) session.getAttribute("hpsmCredential");
            model.addAttribute("hpsmCredential", hpsmFormFromSession);
        } else {
            model.addAttribute("hpsmCredential", new Hpsm());
        }

        // JIRA Project setup
        model.addAttribute("jiraProjects", lookupProvider.loadJIRAProjects());

        // HPSM Project setup
        model.addAttribute("hpsmProjects", lookupProvider.loadHPSMProjects());

        model.addAttribute("user", user);

        return "configure";
    }


    @RequestMapping(value = "configureHPSMJIRA")
    public String configure(@ModelAttribute("user") User user, HttpSession session, Model model) {
        User tmpUser = user;

        if(session.getAttribute("jiraCredentials") != null) {
            Jira jiraFormFromSession = (Jira) session.getAttribute("jiraCredentials");
            model.addAttribute("jiraCredential", jiraFormFromSession);
        } else {
            model.addAttribute("jiraCredential", new Jira());
        }

        if(session.getAttribute("hpsmCredential") != null) {
            Hpsm hpsmFormFromSession = (Hpsm) session.getAttribute("hpsmCredential");
            model.addAttribute("hpsmCredential", hpsmFormFromSession);
        } else {
            model.addAttribute("hpsmCredential", new Hpsm());
        }

        if(session.getAttribute("USER") != null) {
            User userTmp = (User) session.getAttribute("USER");
            userTmp.setBusinessServiceMapping(tmpUser.getBusinessServiceMapping());

            tmpUser = userTmp;
            session.setAttribute("USER", tmpUser);
            //userService.setUser(user);
        }

        model.addAttribute("user", tmpUser);


        // JIRA Project setup
        model.addAttribute("jiraProjects", lookupProvider.loadJIRAProjects());

        // HPSM Project setup
        List<HPSMProject> hpsmProjects = lookupProvider.loadHPSMProjects();
        model.addAttribute("hpsmProjects", hpsmProjects);

        List<JIRAProject> jiraProjects = new ArrayList<JIRAProject>();

        if(userService != null && userService.getUser() != null && userService.getUser().isUserAuthenticated()) {
            for(HPSMProject project:hpsmProjects) {
                if(project.getProjectName().equals(user.getBusinessServiceMapping().getHpsmProject().getProjectName())) {
                    user.getBusinessServiceMapping().getHpsmProject().setProjectId(project.getProjectId());
                    break;
                }
            }

            for (JIRAProject jiraProject : lookupProvider.loadJIRAProjects()) {
                if (jiraProject.getProjectId().equals(user.getBusinessServiceMapping().getHpsmProject().getProjectId())) {
                    // HPSM -> JIRA
                    //user.getBusinessServiceMapping().getJiraProject().setProjectId(user.getBusinessServiceMapping().getHpsmProject().getProjectId());
                    userService.getUser().getBusinessServiceMapping().getJiraProject().setProjectId(user.getBusinessServiceMapping().getHpsmProject().getProjectId());
                    break;
                }
            }
        }

        return "configure";
    }

    @PostMapping(value = "saveConfig")
    public String saveConfiguration(@ModelAttribute("util") User user, HttpSession session, Model model) {

        if(userService.getUser() != null && userService.getUser().isUserAuthenticated()) {

        }

        // JIRA Project setup
        model.addAttribute("jiraProjects", lookupProvider.loadJIRAProjects());

        // HPSM Project setup
        List<HPSMProject> hpsmProjects = lookupProvider.loadHPSMProjects();
        model.addAttribute("hpsmProjects", hpsmProjects);

        model.addAttribute("util", userService.getUser());
        model.addAttribute("userObj", userService.getUser() != null? userService.getUser():new User());

        return "configure";
    }

    @PostMapping(value = "addBusinessMapping")
    public String addBusinessMapping(@ModelAttribute("util") User user, HttpSession session, Model model) {
        List<com.hpsmjira.model.Project> selectedProjects = userService.getSelectedProjects(); //user.getSelectedProjects();

        // JIRA Project setup
        model.addAttribute("jiraProjects", lookupProvider.getJiraProjects());

        // HPSM Project setup
        model.addAttribute("hpsmProjects", lookupProvider.getHpsmProjects());
        model.addAttribute("userObj",new User());

        if(user.getSelectedProjectToRemove() == null) {
            /* TO add */
            com.hpsmjira.model.Project project = new com.hpsmjira.model.Project();
            project.setProjectId(user.getBusinessServiceMapping().getSelectedJIRAProjectId());
            project.setProjectName(user.getBusinessServiceMapping().getSelectedHPSMProjectName() + " [" + user.getBusinessServiceMapping().getSelectedJIRAProjectId() + "]");

            if (selectedProjects == null) {
                selectedProjects = new ArrayList<com.hpsmjira.model.Project>();
            }

            selectedProjects.add(project);
            userService.setSelectedProjects(selectedProjects);
        } else if(user.getSelectedProjectToRemove() != null) {
            for(Project projectToRemove : selectedProjects) {
                System.out.println(projectToRemove);
                if(projectToRemove.getProjectName().equals(user.getSelectedProjectToRemove())) {
                    selectedProjects.remove(projectToRemove);
                    break;
                }
            }
        }

        user.setSelectedProjectToRemove(null);

        model.addAttribute("projectMap", selectedProjects);

        return "configure";
    }

    private void setCommonModel(Model model) {

    }




}

