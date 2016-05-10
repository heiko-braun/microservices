package org.javaee7.wildfly.samples.services.consul;

import java.net.URL;
import java.util.List;

import com.google.common.net.HostAndPort;
import com.orbitz.consul.AgentClient;
import com.orbitz.consul.Consul;
import com.orbitz.consul.HealthClient;
import com.orbitz.consul.model.agent.ImmutableRegistration;
import com.orbitz.consul.model.agent.Registration;
import com.orbitz.consul.model.health.Service;
import com.orbitz.consul.model.health.ServiceHealth;
import org.javaee7.wildfly.samples.services.registration.ServiceRegistry;

/**
 * @author Heiko Braun
 * @since 10/05/16
 */
public class ConsulRegistry implements ServiceRegistry {
    @Override
    public void registerService(String serviceName, String uri) {
        try {

            URL url = new URL(uri);

            AgentClient client = getConsulClient();

            Registration consulReg = ImmutableRegistration.builder()
                    .address(url.getHost())
                    .port(url.getPort())
                    .id(serviceId(serviceName ,url.getHost(), url.getPort()))
                    .name(serviceName)
                    .check(com.orbitz.consul.model.agent.Registration.RegCheck.ttl(3L))
                    .build();

            client.register(consulReg);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private AgentClient getConsulClient() {
        String consulHost = System.getProperty("consul.host", "localhost");
        HostAndPort hostAndPort = HostAndPort.fromParts(consulHost, 8500);
        Consul consul = Consul.builder().withHostAndPort(hostAndPort).build();
        return consul.agentClient();
    }


    private String serviceId(String name, String address, int port) {
        return name + ":" + address + ":" + port;
    }

    @Override
    public void unregisterService(String name, String uri) {
        try {
            URL url = new URL(uri);
            AgentClient client = getConsulClient();
            client.deregister(serviceId(name, url.getHost(), url.getPort()));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String discoverServiceURI(String name) {

        Consul consul = Consul.builder().build(); // connect to Consul on localhost
        HealthClient healthClient = consul.healthClient();

        List<ServiceHealth> nodes = healthClient.getHealthyServiceInstances(name).getResponse();

        if(nodes.isEmpty()) {
            throw new RuntimeException("No healthy service found for "+name);
        }

        try {
            Service first = nodes.get(0).getService();
            URL url = new URL("http://"+first.getAddress()+":"+first.getPort());
            return url.toExternalForm();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
