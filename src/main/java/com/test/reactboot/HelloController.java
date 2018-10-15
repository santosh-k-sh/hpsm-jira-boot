package com.test.reactboot;


import org.codehaus.jettison.json.JSONException;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by santosh.sharma on 10/5/2018.
 */

@RestController
public class HelloController {
    @GetMapping("/api/hello")
    public String hello() {
        return "Hello, the time at the server is now " + new Date() + "\n";
    }

    @GetMapping("/allProjects-test")
    @CrossOrigin(origins = "http://localhost:3000")
    public List<Project> allProjects() {
        List<Project> projectList = new ArrayList<Project>();
        Project project = new Project();
        project.setProjectId("001");
        project.setProjectName("Test");
        projectList.add(project);

        return projectList;
    }

    @PostMapping("allProject")
    public List<Project> allProj(@RequestBody String hpsmUserName) {
        List<Project> projectList = new ArrayList<Project>();
        Project project = new Project();
        project.setProjectId("001");
        project.setProjectName(hpsmUserName);
        projectList.add(project);

        return projectList;
    }

    // http://localhost:8080/square/12
    @RequestMapping(value = "square/{number}", method = RequestMethod.POST)
    public int calculateSquare(@PathVariable int number) {
        return number * number;
    }


    @RequestMapping(value = "/sum1", method = RequestMethod.POST)
    public String calculateSum(@RequestParam("hpsmUrl") String hpsmUrl, @RequestParam("hpsmUserName") String hpsmUserName, @RequestParam("hpsmPassword") String hpsmPassword) {
        return "Success";
    }

    @RequestMapping(value = "/sum", method = RequestMethod.POST)
    public String calculateSum1(@RequestBody HPSMCredential hpsmCredential) {
        return "Success";
    }

    @RequestMapping(value = "/validatehpsm-test", method = RequestMethod.POST)
    public String validatehpsm(@RequestBody HPSMCredential hpsmCredential) throws JSONException {
        System.out.println(hpsmCredential);


        return "Success";
    }
}
