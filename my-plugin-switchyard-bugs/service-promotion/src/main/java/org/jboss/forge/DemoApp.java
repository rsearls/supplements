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
    private SwitchYardFacet switchYardFacet;

    public DemoApp(){}

    public void process(String className) {

        File reportDirectory = new File("target/test/dummy/output");

        try {
            switchyardForgeRegistrar = new SwitchyardForgeRegistrar(reportDirectory);

            if (switchyardForgeRegistrar.createProject("myProject") != null) {
                processActions(className);
            }

        } catch (Throwable e) {
            e.printStackTrace();
        } finally {
            switchyardForgeRegistrar.stop();
            LOG.info("Furnace stopped.");
        }
    }


    public void processActions(String className){

        Project project = switchyardForgeRegistrar.getProject();
        if (project == null) {
            LOG.warn("No project found ");
        } else {

            MetadataFacet mdf = project.getFacet(MetadataFacet.class);
            mdf.setProjectName(project.getProjectRoot().getName());

            FacetFactory facetFactory = switchyardForgeRegistrar.getFacetFactory();
            switchYardFacet = facetFactory.install(project, SwitchYardFacet.class);

            try {
                String packageName = "jbossesb.services.service";

                BeanFacet beanFacet = facetFactory.create(project, BeanFacet.class);
                beanFacet.install();
                BeanServiceConfigurator beanServiceConfigurator =
                    new BeanServiceConfigurator();
                beanServiceConfigurator.newBean(project, packageName, className);

            } catch (Exception ex) {
                LOG.error(ex);
            }
        }

    }

    public void promote (String className){

        try {

            this.process(className);

            Project project = switchyardForgeRegistrar.getProject();
            System.out.println("## before compile");
            // compile required for service to be found and promotable
            project.getFacet(PackagingFacet.class).createBuilder().build();
            System.out.println("## after compile");
            // promote the service
            SwitchYardConfigurator switchYardfConfig =
                switchyardForgeRegistrar.getSwitchYardConfigurator();

            switchYardfConfig.promoteService(project, className);
            switchYardFacet.saveConfig();

        } catch (Exception ex) {
            LOG.error(ex);
        }

    }

    public static void main( String[] args ) {
        String className = "SimpleListener";
        DemoApp app = new DemoApp();

        if (args.length == 0){
            System.out.println("Creating service SimpleListener");
            app.process(className);
        } else {
            System.out.println("promote service SimpleListener");
            app.promote(className);
        }
    }
}
