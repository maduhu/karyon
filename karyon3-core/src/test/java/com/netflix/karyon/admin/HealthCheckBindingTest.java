package com.netflix.karyon.admin;

import java.util.Map.Entry;

import javax.inject.Named;
import javax.inject.Singleton;

import junit.framework.Assert;

import org.junit.Test;

import com.google.inject.AbstractModule;
import com.google.inject.Binding;
import com.google.inject.Key;
import com.google.inject.Provides;
import com.google.inject.multibindings.Multibinder;
import com.netflix.governator.Governator;
import com.netflix.governator.LifecycleInjector;
import com.netflix.karyon.HealthCheck;
import com.netflix.karyon.HealthCheckStatus;
import com.netflix.karyon.HealthState;
import com.netflix.karyon.LifecycleState;
import com.netflix.karyon.healthcheck.HealthIndicator;
import com.netflix.karyon.healthcheck.HealthIndicatorRegistry;
import com.netflix.karyon.healthcheck.HealthIndicators;

public class HealthCheckBindingTest {
    @Test
    public void test() {
        LifecycleInjector injector = Governator.createInjector(
            new AbstractModule() {
                @Provides
                @Singleton
                @Named("hc1")
                public HealthIndicator getHealthCheck1() {
                    return HealthIndicators.alwaysHealthy("hc1"); 
                }
                
                @Override
                protected void configure() {
                    Multibinder.newSetBinder(binder(), HealthIndicator.class).addBinding().toInstance(HealthIndicators.alwaysUnhealthy("hc2"));
                    
                }
            });
        
        for (Entry<Key<?>, Binding<?>> binding : injector.getAllBindings().entrySet()) {
            System.out.println(binding.getKey());
        }
        
        HealthIndicatorRegistry registry = injector.getInstance(HealthIndicatorRegistry.class);
        Assert.assertEquals(2, registry.getHealthIndicators().size());
        HealthCheckResource res = injector.getInstance(HealthCheckResource.class);
        
        HealthCheckStatus status = injector.getInstance(HealthCheck.class).check().join();
        Assert.assertEquals(HealthState.Unhealthy, status.getState());
        Assert.assertEquals(2, status.getIndicators().size());
    }
}
