package org.javaee7.wildfly.samples.everest.catalog;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.ClassLoaderAsset;
import org.wildfly.swarm.Swarm;
import org.wildfly.swarm.jaxrs.JAXRSArchive;

/**
 * @author Heiko Braun
 * @since 15/05/16
 */
public class Main {
    public static void main(String[] args) throws Exception {

        Swarm swarm = new Swarm();
        swarm.start();

        JAXRSArchive archive = ShrinkWrap.create(JAXRSArchive.class);
        archive.addPackage(Main.class.getPackage());
        archive.addAsWebInfResource(new ClassLoaderAsset("META-INF/persistence.xml", Main.class.getClassLoader()), "classes/META-INF/persistence.xml");
        archive.addAsWebInfResource(new ClassLoaderAsset("META-INF/load.sql", Main.class.getClassLoader()), "classes/META-INF/load.sql");
        archive.addAllDependencies();

        swarm.deploy(archive);

    }
}
