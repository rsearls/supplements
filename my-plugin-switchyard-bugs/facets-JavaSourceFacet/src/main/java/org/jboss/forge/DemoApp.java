package org.jboss.forge;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.forge.addon.facets.FacetFactory;
import org.jboss.forge.addon.projects.Project;
import org.jboss.forge.addon.projects.facets.MetadataFacet;
import org.jboss.forge.addon.projects.facets.PackagingFacet;
import org.switchyard.tools.forge.bean.BeanFacet;
import org.switchyard.tools.forge.bean.BeanServiceConfigurator;
import org.switchyard.tools.forge.plugin.SwitchYardConfigurator;
import org.switchyard.tools.forge.plugin.SwitchYardFacet;

import java.io.File;

public class DemoApp {

    private static final Log LOG = LogFactory.getLog(DemoApp.class);

    private SwitchyardForgeRegistrar switchyardForgeRegistrar;

    public DemoApp(){}

    public void process() {

        File reportDirectory = new File("target/test/dummy/output");

        try {
            switchyardForgeRegistrar = new SwitchyardForgeRegistrar(reportDirectory);

            if (switchyardForgeRegistrar.createProject("myProject") != null) {
                processActions();
            }

        } catch (Throwable e) {
            e.printStackTrace();
        } finally {
            switchyardForgeRegistrar.stop();
            LOG.info("Furnace stopped.");
        }
    }


    public void processActions(){

        Project project = switchyardForgeRegistrar.getProject();
        if (project == null) {
            LOG.warn("No project found ");
        } else {

            MetadataFacet mdf = project.getFacet(MetadataFacet.class);
            mdf.setProjectName(project.getProjectRoot().getName());

            FacetFactory facetFactory = switchyardForgeRegistrar.getFacetFactory();
            SwitchYardFacet switchYardFacet = facetFactory.install(project, SwitchYardFacet.class);

            try {
                String className = "SimpleListener";
                String packageName = "jbossesb.services.service";
                createService(project, facetFactory, packageName, className);
            } catch (Exception ex) {
                LOG.error(ex);
            }
        }

    }


    private void createService(Project project, FacetFactory facetFactory,
                               String packageName, String className) {

        try {
            BeanFacet beanFacet = facetFactory.create(project, BeanFacet.class);
            beanFacet.install();
            BeanServiceConfigurator beanServiceConfigurator =
                new BeanServiceConfigurator();
            beanServiceConfigurator.newBean(project, packageName, className);

            //- Code throwing the exception HERE
             switchyardForgeRegistrar.getSwitchYardConfigurator().createServiceTest(
             project, className, packageName);


        } catch (Exception ex) {
            LOG.error(ex);
            ex.printStackTrace();
        }
    }

    public static void main( String[] args ) {
        DemoApp app = new DemoApp();
        app.process();
    }
}
