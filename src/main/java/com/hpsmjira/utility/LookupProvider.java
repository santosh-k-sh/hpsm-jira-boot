package com.hpsmjira.utility;

import com.hpsmjira.model.HPSMProject;
import com.hpsmjira.model.JIRAProject;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Santosh.Sharma on 9/29/2018.
 */

@Component
public class LookupProvider {

    private List<JIRAProject> jiraProjects;
    private List<HPSMProject> hpsmProjects;

    @PostConstruct
    public void load() {
        jiraProjects = loadJIRAProjects();
        hpsmProjects = loadHPSMProjects();
    }

    public List<JIRAProject> loadJIRAProjects() {
        List<JIRAProject> jiraProjects = new ArrayList<JIRAProject>();
        JIRAProject jiraProject1 = new JIRAProject("TVIS", "Vision");
        JIRAProject jiraProject2 = new JIRAProject("VFIS", "FIS");
        JIRAProject jiraProject3 = new JIRAProject("TARA", "ARA");
        JIRAProject jiraProject4 = new JIRAProject("TGATES", "Gates");
        JIRAProject jiraProject6 = new JIRAProject("TEDNRD", "eDNRD");
        JIRAProject jiraProject7 = new JIRAProject("TEFRM", "eForms");
        JIRAProject jiraProject8 = new JIRAProject("TMBL", "Mobile");
        JIRAProject jiraProject9 = new JIRAProject("TNOQ", "Noqodi (Legacy)");
        JIRAProject jiraProject10 = new JIRAProject("TEAP", "EAP");
        JIRAProject jiraProject11 = new JIRAProject("TIQC", "IQC");
        JIRAProject jiraProject12 = new JIRAProject("TNGE", "NGE");

        jiraProjects.add(jiraProject1);
        jiraProjects.add(jiraProject2);
        jiraProjects.add(jiraProject3);
        jiraProjects.add(jiraProject4);
        //jiraProjects.add(jiraProject5);
        jiraProjects.add(jiraProject6);
        jiraProjects.add(jiraProject7);
        jiraProjects.add(jiraProject8);
        jiraProjects.add(jiraProject9);
        jiraProjects.add(jiraProject10);
        jiraProjects.add(jiraProject11);
        jiraProjects.add(jiraProject12);

        return jiraProjects;
    }

    public List<HPSMProject> loadHPSMProjects() {
        List<HPSMProject>  hpsmProjects = new ArrayList<HPSMProject>();
        HPSMProject hpsmProject1 = new HPSMProject("TVIS", "Vision.Core");
        HPSMProject hpsmProject2 = new HPSMProject("TVIS", "Vision.BorderControl");
        HPSMProject hpsmProject3 = new HPSMProject("VFIS", "FIS.Backend");
        HPSMProject hpsmProject4 = new HPSMProject("TEDNRD", "LegacyeDNRD");
        HPSMProject hpsmProject5 = new HPSMProject("TEDNRD", "VisioneDNRD");
        HPSMProject hpsmProject6 = new HPSMProject("TEDNRD", "VisioneDNRD.Portal");
        HPSMProject hpsmProject7 = new HPSMProject("TEDNRD", "VisioneDNRD.Portal.ETA");
        HPSMProject hpsmProject8 = new HPSMProject("TEFRM", "LegacyeForm");
        HPSMProject hpsmProject9 = new HPSMProject("TEFRM", "LegacyeForm.Portal");
        HPSMProject hpsmProject10 = new HPSMProject("TEFRM", "VisioneForm");
        HPSMProject hpsmProject11 = new HPSMProject("TEFRM", "VisioneForm.ChannelPartners.Registration");
        HPSMProject hpsmProject12 = new HPSMProject("TNOQ", "Noqodi.Payment");
        HPSMProject hpsmProject13 = new HPSMProject("TNOQ", "Noqodi");
        HPSMProject hpsmProject14 = new HPSMProject("TUXD", "Noqodi.FrontOffice");
        HPSMProject hpsmProject15 = new HPSMProject("TNOQ", "Noqodi.Merchants");
        HPSMProject hpsmProject16 = new HPSMProject("TNOQ", "Noqodi.Payment");
        HPSMProject hpsmProject17 = new HPSMProject("TNOQ", "Noqodi.Reports");
        HPSMProject hpsmProject18 = new HPSMProject("TEAP", "Unified Channel");
        HPSMProject hpsmProject19 = new HPSMProject("TVIS", "Vision.BCLite");

        hpsmProjects.add(hpsmProject1);
        hpsmProjects.add(hpsmProject2);
        hpsmProjects.add(hpsmProject3);
        hpsmProjects.add(hpsmProject4);
        hpsmProjects.add(hpsmProject5);
        hpsmProjects.add(hpsmProject6);
        hpsmProjects.add(hpsmProject7);
        hpsmProjects.add(hpsmProject8);
        hpsmProjects.add(hpsmProject9);
        hpsmProjects.add(hpsmProject10);
        hpsmProjects.add(hpsmProject11);
        hpsmProjects.add(hpsmProject12);
        hpsmProjects.add(hpsmProject13);
        hpsmProjects.add(hpsmProject14);
        hpsmProjects.add(hpsmProject15);
        hpsmProjects.add(hpsmProject16);
        hpsmProjects.add(hpsmProject17);
        hpsmProjects.add(hpsmProject18);
        hpsmProjects.add(hpsmProject19);

        return hpsmProjects;
    }

    public List<JIRAProject> getJiraProjects() {
        return jiraProjects;
    }

    public void setJiraProjects(List<JIRAProject> jiraProjects) {
        this.jiraProjects = jiraProjects;
    }

    public List<HPSMProject> getHpsmProjects() {
        return hpsmProjects;
    }

    public void setHpsmProjects(List<HPSMProject> hpsmProjects) {
        this.hpsmProjects = hpsmProjects;
    }
}
