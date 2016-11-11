package com.mobenga.hm.openbet.web.init;

import com.mobenga.hm.openbet.configuration.ApplicationConfiguration;
import com.mobenga.hm.openbet.configuration.WebMvcConfiguration;
import org.springframework.web.WebApplicationInitializer;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRegistration.Dynamic;

/**
 * Instead of use web.xml used this class
 */
public class ApplicationWebInitializer  implements WebApplicationInitializer {

    @Override
    public void onStartup(ServletContext servletContext) throws ServletException {
    
        AnnotationConfigWebApplicationContext ctx = new AnnotationConfigWebApplicationContext();  
        ctx.register(ApplicationConfiguration.class, WebMvcConfiguration.class);
          
        ctx.setServletContext(servletContext);    
          
        Dynamic servlet = servletContext.addServlet("dispatcher", new DispatcherServlet(ctx));  
        servlet.addMapping("/*");
        servlet.setLoadOnStartup(1);  
    }
}
