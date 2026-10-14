package com.example.ailab.week1.day5;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.actuate.info.Info;
import org.springframework.boot.actuate.info.InfoContributor;
import org.springframework.boot.info.BuildProperties;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class LabInfoContributor implements InfoContributor {

    private final ObjectProvider<BuildProperties> build;

    public LabInfoContributor(ObjectProvider<BuildProperties> build) {
        this.build = build;
    }

    @Override
    public void contribute(Info.Builder builder) {
        Map<String, Object> lab = new LinkedHashMap<>();
        lab.put("week", 1);
        lab.put("module", "IoC, profiles, auto-config, AOP, actuator");
        build.ifAvailable(properties -> {
            lab.put("artifact", properties.getArtifact());
            lab.put("version", properties.getVersion());
        });
        builder.withDetail("lab", lab);
    }
}
