package io.github.rafaviv.yakubackend;

import io.cucumber.junit.platform.engine.Constants;
import org.junit.platform.suite.api.ConfigurationParameter;
import org.junit.platform.suite.api.IncludeEngines;
import org.junit.platform.suite.api.SelectClasspathResource;
import org.junit.platform.suite.api.Suite;

@Suite
@IncludeEngines("cucumber")
@SelectClasspathResource("features/telemetry")
@SelectClasspathResource("features/notification")
@SelectClasspathResource("features/equipment/equipment-registration.feature")
@SelectClasspathResource("features/subscription")
@ConfigurationParameter(key = Constants.GLUE_PROPERTY_NAME, value = "io.github.rafaviv.yakubackend.steps")
@ConfigurationParameter(key = Constants.PLUGIN_PROPERTY_NAME, value = "pretty")
public class CucumberRunnerTest {
}

