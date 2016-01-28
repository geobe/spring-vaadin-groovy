package de.fh_zwickau.pti.geobe.component;

import de.fh_zwickau.pti.geobe.service.IStartupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

/**
 * @author georg beier
 */
@Component
public class ContextRefreshedListener implements ApplicationListener<ContextRefreshedEvent> {
    @Autowired
    private IStartupService startupService;

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        startupService.initApplicationData();
    }
}