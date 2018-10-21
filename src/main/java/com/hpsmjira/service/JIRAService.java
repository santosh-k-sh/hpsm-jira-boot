package com.hpsmjira.service;

import com.atlassian.jira.rest.client.api.IssueRestClient;
import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.api.domain.*;
import com.atlassian.jira.rest.client.api.domain.input.FieldInput;
import com.atlassian.jira.rest.client.api.domain.input.IssueInput;
import com.atlassian.jira.rest.client.api.domain.input.IssueInputBuilder;
import com.atlassian.jira.rest.client.internal.async.AsynchronousJiraRestClientFactory;
import com.atlassian.util.concurrent.Promise;
import com.hpsmjira.model.HPSMProblem;
import com.hpsmjira.model.JIRAIssue;
import com.hpsmjira.model.Jira;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.util.*;
import java.util.concurrent.ExecutionException;

/**
 * Created by Santosh.Sharma on 9/27/2018.
 */
@Service
public class JIRAService {
    public static final String JIRA_TICKET_SEARCH_PATTERN_TEXT = "text ~";

    private JiraRestClient restClient;
    private Jira jira;

    @Autowired
    UserService userService;

    @Autowired
    HPSMService hpsmService;

    public JiraRestClient login(Jira jira) {
        this.jira = jira;

        return authenticate();

    }

    private JiraRestClient authenticate() {
        restClient = new AsynchronousJiraRestClientFactory()
                .createWithBasicHttpAuthentication(URI.create("https://emaratech.atlassian.net"), jira.getJiraUserName(), jira.getJiraPassword());

        if(restClient.getProjectClient().getProject("TVIS").claim() != null) {
            return restClient;
        }
        return null;
    }


    public List<JIRAIssue> searchJiraTicket(String problemNo) {
        StringBuffer searchJqlText = new StringBuffer(JIRA_TICKET_SEARCH_PATTERN_TEXT);
        searchJqlText.append(problemNo);
        List<JIRAIssue> jiraIssues = new ArrayList<JIRAIssue>();

        if(restClient != null) {
            System.out.println("Searching in JIRA for Problem No : " + problemNo);
            Promise<SearchResult> searchJqlPromise = restClient.getSearchClient().searchJql(searchJqlText.toString());

            if(searchJqlPromise != null && ((Collection) searchJqlPromise.claim().getIssues()).size()  > 0) {
                System.out.println("Problem No : " + problemNo + " found in JIRA");
                for (Issue resultIssue : searchJqlPromise.claim().getIssues()) {
                    JIRAIssue jiraIssue2 = new JIRAIssue();
                    jiraIssue2.setIssueKey(resultIssue.getKey());
                    jiraIssue2.setDescription(resultIssue.getDescription());
                    jiraIssue2.setSummary(resultIssue.getSummary());
                    jiraIssue2.setIssueStatus(resultIssue.getStatus().getName());
                    jiraIssue2.setCreatedDate(resultIssue.getCreationDate());

                    if(resultIssue.getFixVersions() != null && resultIssue.getStatus().getId() == 10001L) {
                        StringBuffer fixVersions = new StringBuffer();
                        for(Version version : resultIssue.getFixVersions()) {
                            fixVersions.append(version.getName());
                        }
                        jiraIssue2.setFixVersion(fixVersions.toString());
                    }

                    jiraIssues.add(jiraIssue2);
                }
            } else {
                System.out.println("============================================================================");
                System.out.println("Problem No : " + problemNo + " NOT found in JIRA");
                System.out.println("============================================================================");
            }
        }
        return jiraIssues;
    }

    public List<String> createJIRATicket() {
        System.out.println("createJIRATicket start...");
        IssueRestClient issueClient = restClient.getIssueClient();
        IssueType problemIssueType = null;
        List<String> newlyCreatedJIRAIds = new ArrayList<String>();

        for(Map.Entry<String, List<HPSMProblem>> hpsmProblemDetail : userService.getProblemToMigrate().entrySet()) {
            try {
                Project jiraProjectByProjectKeyId = restClient.getProjectClient().getProject(hpsmProblemDetail.getKey()).get();
                Iterator issueTypeIterator = jiraProjectByProjectKeyId.getIssueTypes().iterator();

                for (Iterator it = issueTypeIterator; it.hasNext(); ) {
                    IssueType issueType = (IssueType) it.next();
                    if (issueType.getName().equals("Problem")) {
                        problemIssueType = issueType;
                        break;
                    }
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }

            if(problemIssueType != null) {
                List<HPSMProblem> problemList = hpsmProblemDetail.getValue();
                for(HPSMProblem hpsmProblem : problemList) {
                    /* Custom fields settings
                    * - customfield_10106: HPSM ID */
                    FieldInput fieldInput = new FieldInput("customfield_10106", hpsmProblem.getProblemNo());


                    System.out.println("Trying to create JIRA against Problem - " + hpsmProblem.getProblemNo());
                    /*IssueInput newIssue = new IssueInputBuilder(hpsmProblemDetail.getKey(),
                            problemIssueType.getId(), hpsmProblem.getProblemTitle())
                            .setDescription(hpsmProblem.getProblemDescription())
                            .setPriorityId(Long.valueOf(hpsmProblem.getProblemPriority()))
                            .setIssueType(problemIssueType)
                            //.setIssueTypeId(hpsmProblemDet.getProjectKey().equals("problem")? 10900L:1090L) // TODO: CR to be handled here
                            //.setReporterName("Rana Khurram Shahzad")
                            .setFieldInput(fieldInput)

                            .build();

                    System.out.println("...");
                    String jiraID = issueClient.createIssue(newIssue).claim().getKey();
                    System.out.println("JIRA created succefully." + jiraID + " for problem " + hpsmProblem.getProblemNo());*/

                    newlyCreatedJIRAIds.add("");
                }
            }
        }

        System.out.println("newlyCreatedJIRAIds : " + newlyCreatedJIRAIds);
        return newlyCreatedJIRAIds;
    }

    public JiraRestClient getRestClient() {
        return restClient;
    }

    public void setRestClient(JiraRestClient restClient) {
        this.restClient = restClient;
    }

    public void setJira(Jira jira) {
        this.jira = jira;
    }
}
