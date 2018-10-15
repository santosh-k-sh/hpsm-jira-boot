package com.hpsmjira.service;

import com.atlassian.jira.rest.client.api.IssueRestClient;
import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.api.domain.Issue;
import com.atlassian.jira.rest.client.api.domain.IssueType;
import com.atlassian.jira.rest.client.api.domain.Project;
import com.atlassian.jira.rest.client.api.domain.SearchResult;
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
            Promise<SearchResult> searchJqlPromise = restClient.getSearchClient().searchJql(searchJqlText.toString());

            if(searchJqlPromise != null && ((Collection) searchJqlPromise.claim().getIssues()).size()  > 0) {
                for (Issue resultIssue : searchJqlPromise.claim().getIssues()) {
                    JIRAIssue jiraIssue2 = new JIRAIssue();
                    jiraIssue2.setIssueKey(resultIssue.getKey());
                    jiraIssue2.setDescription(resultIssue.getDescription());
                    jiraIssue2.setSummary(resultIssue.getSummary());
                    jiraIssue2.setIssueStatus(resultIssue.getStatus().getName());
                    jiraIssue2.setCreatedDate(resultIssue.getCreationDate());
                    jiraIssues.add(jiraIssue2);
                }
            }
        }
        return jiraIssues;
    }

    public List<String> createJIRATicket() {
        IssueRestClient issueClient = restClient.getIssueClient();
        IssueType problemIssueType = null;
        List<String> newlyCreatedJIRAIds = new ArrayList<String>();

        for(Map.Entry<String, HPSMProblem> hpsmProblemDetail : userService.getProblemToMigrate().entrySet()) {
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
                HPSMProblem hpsmProblemDet = hpsmProblemDetail.getValue();
                /* Custom fields settings
                * - customfield_10106: HPSM ID */
                FieldInput fieldInput = new FieldInput("customfield_10106", hpsmProblemDet.getProblemNo());


                /*IssueInput newIssue = new IssueInputBuilder(hpsmProblemDetail.getKey(),
                        problemIssueType.getId(), hpsmProblemDet.getProblemTitle())
                        .setDescription(hpsmProblemDet.getProblemDescription())
                        .setPriorityId(Long.valueOf(hpsmProblemDet.getProblemPriority()))
                        .setIssueType(problemIssueType)
                        //.setIssueTypeId(hpsmProblemDet.getProjectKey().equals("problem")? 10900L:1090L) // TODO: CR to be handled here
                        //.setReporterName("Rana Khurram Shahzad")
                        .setFieldInput(fieldInput)

                        .build();*/

                System.out.println("...");
                String jiraID = "111";//issueClient.createIssue(newIssue).claim().getKey();
                System.out.println("JIRA created succefully." + jiraID);

                newlyCreatedJIRAIds.add(jiraID);
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
