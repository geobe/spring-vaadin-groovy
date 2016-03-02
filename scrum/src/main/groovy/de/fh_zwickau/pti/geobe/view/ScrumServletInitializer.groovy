package de.fh_zwickau.pti.geobe.view

import de.fh_zwickau.pti.geobe.GroovaaApplication
import org.springframework.boot.builder.SpringApplicationBuilder
import org.springframework.boot.context.web.SpringBootServletInitializer

/**
 * Created by georg beier on 02.02.2016.
 */
class ScrumServletInitializer extends  SpringBootServletInitializer {

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
        return builder.sources(GroovaaApplication.class)
    }
}
