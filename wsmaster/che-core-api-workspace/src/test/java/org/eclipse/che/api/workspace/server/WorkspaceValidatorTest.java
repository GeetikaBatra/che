/*
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.api.workspace.server;

import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static org.eclipse.che.dto.server.DtoFactory.newDto;

import com.google.common.collect.ImmutableMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.eclipse.che.api.core.ValidationException;
import org.eclipse.che.api.workspace.shared.dto.CommandDto;
import org.eclipse.che.api.workspace.shared.dto.EnvironmentDto;
import org.eclipse.che.api.workspace.shared.dto.MachineConfigDto;
import org.eclipse.che.api.workspace.shared.dto.RecipeDto;
import org.eclipse.che.api.workspace.shared.dto.ServerConfigDto;
import org.eclipse.che.api.workspace.shared.dto.WorkspaceConfigDto;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/**
 * Tests for {@link WorkspaceValidator}.
 *
 * @author Alexander Reshetnyak
 */
@Listeners(MockitoTestNGListener.class)
public class WorkspaceValidatorTest {
  @Mock private WorkspaceRuntimes workspaceRuntimes;

  @InjectMocks private WorkspaceValidator wsValidator;

  @Test
  public void shouldValidateCorrectWorkspace() throws Exception {
    final WorkspaceConfigDto config = createConfig();

    wsValidator.validateConfig(config);
  }

  @Test(
    expectedExceptions = ValidationException.class,
    expectedExceptionsMessageRegExp = "Workspace name required"
  )
  public void shouldFailValidationIfNameIsNull() throws Exception {
    final WorkspaceConfigDto config = createConfig();
    config.withName(null);

    wsValidator.validateConfig(config);
  }

  @Test(
    dataProvider = "invalidNameProvider",
    expectedExceptions = ValidationException.class,
    expectedExceptionsMessageRegExp =
        "Incorrect workspace name, it must be between 3 and 20 "
            + "characters and may contain digits, latin letters, underscores, dots, dashes and must "
            + "start and end only with digits, latin letters or underscores"
  )
  public void shouldFailValidationIfNameIsInvalid(String name) throws Exception {
    final WorkspaceConfigDto config = createConfig();
    config.withName(name);

    wsValidator.validateConfig(config);
  }

  @DataProvider(name = "invalidNameProvider")
  public static Object[][] invalidNameProvider() {
    return new Object[][] {
      {".name"},
      {"name."},
      {"-name"},
      {"name-"},
      {"long-name12345678901234567890"},
      {"_name"},
      {"name_"}
    };
  }

  @Test(dataProvider = "validNameProvider")
  public void shouldValidateCorrectWorkspaceName(String name) throws Exception {
    final WorkspaceConfigDto config = createConfig();
    config.withName(name);

    wsValidator.validateConfig(config);
  }

  @DataProvider(name = "validNameProvider")
  public static Object[][] validNameProvider() {
    return new Object[][] {
      {"name"},
      {"quiteLongName1234567"},
      {"name-with-dashes"},
      {"name.with.dots"},
      {"name0with1digits"},
      {"mixed-symbols.name12"},
      {"123456"},
      {"name_name"},
      {"123-456.78"}
    };
  }

  @Test(
    expectedExceptions = ValidationException.class,
    expectedExceptionsMessageRegExp = "Attribute name 'null' is not valid"
  )
  public void shouldFailValidationIfAttributeNameIsNull() throws Exception {
    Map<String, String> attributes = new HashMap<>();
    attributes.put(null, "value1");

    wsValidator.validateAttributes(attributes);
  }

  @Test(
    expectedExceptions = ValidationException.class,
    expectedExceptionsMessageRegExp = "Attribute name '' is not valid"
  )
  public void shouldFailValidationIfAttributeNameIsEmpty() throws Exception {
    wsValidator.validateAttributes(ImmutableMap.of("", "value1"));
  }

  @Test(
    expectedExceptions = ValidationException.class,
    expectedExceptionsMessageRegExp = "Attribute name 'codenvy_key' is not valid"
  )
  public void shouldFailValidationIfAttributeNameStartsWithWordCodenvy() throws Exception {
    wsValidator.validateAttributes(ImmutableMap.of("codenvy_key", "value1"));
  }

  @Test(
    expectedExceptions = ValidationException.class,
    expectedExceptionsMessageRegExp = "Workspace default environment name required"
  )
  public void shouldFailValidationIfDefaultEnvNameIsNull() throws Exception {
    final WorkspaceConfigDto config = createConfig();
    config.setDefaultEnv(null);

    wsValidator.validateConfig(config);
  }

  @Test(
    expectedExceptions = ValidationException.class,
    expectedExceptionsMessageRegExp = "Workspace default environment name required"
  )
  public void shouldFailValidationIfDefaultEnvNameIsEmpty() throws Exception {
    final WorkspaceConfigDto config = createConfig();
    config.setDefaultEnv("");

    wsValidator.validateConfig(config);
  }

  @Test(
    expectedExceptions = ValidationException.class,
    expectedExceptionsMessageRegExp = "Workspace default environment configuration required"
  )
  public void shouldFailValidationIfEnvWithDefaultEnvNameIsNull() throws Exception {
    final WorkspaceConfigDto config = createConfig();
    config.setEnvironments(null);

    wsValidator.validateConfig(config);
  }

  @Test(
    expectedExceptions = ValidationException.class,
    expectedExceptionsMessageRegExp = "Workspace ws-name contains command with null or empty name"
  )
  public void shouldFailValidationIfCommandNameIsNull() throws Exception {
    final WorkspaceConfigDto config = createConfig();
    config.getCommands().get(0).withName(null);

    wsValidator.validateConfig(config);
  }

  @Test(
    expectedExceptions = ValidationException.class,
    expectedExceptionsMessageRegExp = "Workspace ws-name contains command with null or empty name"
  )
  public void shouldFailValidationIfCommandNameIsEmpty() throws Exception {
    final WorkspaceConfigDto config = createConfig();
    config.getCommands().get(0).withName(null);

    wsValidator.validateConfig(config);
  }

  @Test(
    expectedExceptions = ValidationException.class,
    expectedExceptionsMessageRegExp = "Command line required for command '.*'"
  )
  public void shouldFailValidationIfCommandLineIsNull() throws Exception {
    final WorkspaceConfigDto config = createConfig();
    config.getCommands().get(0).withCommandLine(null);

    wsValidator.validateConfig(config);
  }

  @Test(
    expectedExceptions = ValidationException.class,
    expectedExceptionsMessageRegExp = "Command line required for command '.*'"
  )
  public void shouldFailValidationIfCommandLineIsEmpty() throws Exception {
    final WorkspaceConfigDto config = createConfig();
    config.getCommands().get(0).withCommandLine("");

    wsValidator.validateConfig(config);
  }

  private static WorkspaceConfigDto createConfig() {
    final WorkspaceConfigDto workspaceConfigDto =
        newDto(WorkspaceConfigDto.class).withName("ws-name").withDefaultEnv("dev-env");

    MachineConfigDto extendedMachine =
        newDto(MachineConfigDto.class)
            .withInstallers(singletonList("org.eclipse.che.ws-agent"))
            .withServers(
                singletonMap(
                    "ref1",
                    newDto(ServerConfigDto.class).withPort("8080/tcp").withProtocol("https")))
            .withAttributes(singletonMap("memoryLimitBytes", "1000000"));
    EnvironmentDto env =
        newDto(EnvironmentDto.class)
            .withMachines(singletonMap("devmachine1", extendedMachine))
            .withRecipe(
                newDto(RecipeDto.class)
                    .withType("type")
                    .withContent("content")
                    .withContentType("content type"));
    workspaceConfigDto.setEnvironments(singletonMap("dev-env", env));

    List<CommandDto> commandDtos = new ArrayList<>();
    commandDtos.add(
        newDto(CommandDto.class)
            .withName("command_name")
            .withType("maven")
            .withCommandLine("mvn clean install")
            .withAttributes(
                new HashMap<>(singletonMap("cmd-attribute-name", "cmd-attribute-value"))));
    workspaceConfigDto.setCommands(commandDtos);

    return workspaceConfigDto;
  }
}
