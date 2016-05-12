package org.javaee7.wildfly.samples.everest.order;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.ClassLoaderAsset;
import org.wildfly.swarm.Swarm;
import org.wildfly.swarm.jaxrs.JAXRSArchive;

/**
 * @author Heiko Braun
 * @since 12/05/16
 */
public class Main {

    public static void main(String[] args) throws Exception {

        Swarm swarm = new Swarm();
        swarm.start();

        JAXRSArchive archive = ShrinkWrap.create(JAXRSArchive.class);
        archive.addPackage(Main.class.getPackage());
        archive.addAsWebInfResource(new ClassLoaderAsset("META-INF/persistence.xml", Main.class.getClassLoader()), "classes/META-INF/persistence.xml");
        archive.addAllDependencies();

        swarm.deploy(archive);

    }
}
